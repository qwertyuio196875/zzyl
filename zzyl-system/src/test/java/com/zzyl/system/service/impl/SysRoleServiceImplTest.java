package com.zzyl.system.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zzyl.common.core.domain.entity.SysRole;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.spring.SpringUtils;
import com.zzyl.system.mapper.SysRoleDeptMapper;
import com.zzyl.system.mapper.SysRoleMapper;
import com.zzyl.system.mapper.SysRoleMenuMapper;
import com.zzyl.system.mapper.SysUserRoleMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SysRoleServiceImpl 单元测试（P1-5 N+1 修复）
 * <p>
 * 验证：
 * 1) deleteRoleByIds 调用 1 次 selectRolesByIds + 1 次 selectRoleList（AOP dataScope）+ 1 次 countUserRoleByRoleIds
 *    不再逐条 selectRoleById / countUserRoleByRoleId
 * 2) checkRoleDataScope(Long...) 走 IN 一次性查，1 次 selectRoleList
 * 3) admin 短路时 checkRoleDataScope 不触发 selectRoleList
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-5</a>
 */
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest
{
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysRoleMenuMapper roleMenuMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysRoleDeptMapper roleDeptMapper;

    @InjectMocks private SysRoleServiceImpl service;

    private static SysRole newRole(Long roleId, String name)
    {
        SysRole r = new SysRole();
        r.setRoleId(roleId);
        r.setRoleName(name);
        return r;
    }

    private static Map<String, Object> countRow(Long roleId, long cnt)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("role_id", roleId);
        row.put("cnt", cnt);
        return row;
    }

    /**
     * mockStatic SpringUtils.getAopProxy 直接返回 invoker 本身，等价于 "AOP 没拦到"
     * 让被测代码路径走通（无 Spring Context 时 AopContext.currentProxy() 会抛错）。
     */
    private static MockedStatic<SpringUtils> openSpringUtilsStub()
    {
        MockedStatic<SpringUtils> mocked = Mockito.mockStatic(SpringUtils.class);
        mocked.when(() -> SpringUtils.getAopProxy(any())).thenAnswer(inv -> inv.getArgument(0));
        return mocked;
    }

    // =====================================================================
    // P1-5: deleteRoleByIds N+1 修复
    // =====================================================================

    @Test
    void deleteRoleByIds_callsBatchSelectsOnce()
    {
        SysRole r1 = newRole(201L, "管理员");
        SysRole r2 = newRole(202L, "操作员");
        when(roleMapper.selectRolesByIds(anyList())).thenReturn(Arrays.asList(r1, r2));
        when(roleMapper.selectRoleList(any(SysRole.class))).thenReturn(Arrays.asList(r1, r2));
        // 没有任何已分配用户
        when(userRoleMapper.countUserRoleByRoleIds(anyList())).thenReturn(Collections.emptyList());
        when(roleMapper.deleteRoleByIds(any(Long[].class))).thenReturn(2);

        int rows;
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class);
             MockedStatic<SpringUtils> spr = openSpringUtilsStub())
        {
            // isAdmin() = false: 让 checkRoleDataScope 真正走 selectRoleList
            sec.when(SecurityUtils::isAdmin).thenReturn(false);
            // isAdmin(Long) = false: 让 checkRoleAllowed 不抛"超级管理员"异常
            sec.when(() -> SecurityUtils.isAdmin(any(Long.class))).thenReturn(false);
            rows = service.deleteRoleByIds(new Long[]{201L, 202L});
        }

        assertEquals(2, rows);
        // 关键: 各 1 次批量调用，不再逐条
        verify(roleMapper, times(1)).selectRolesByIds(anyList());
        verify(roleMapper, times(1)).selectRoleList(any(SysRole.class));
        verify(userRoleMapper, times(1)).countUserRoleByRoleIds(anyList());
        verify(roleMapper, never()).selectRoleById(any(Long.class));
        verify(userRoleMapper, never()).countUserRoleByRoleId(any(Long.class));
        verify(roleMapper, times(1)).deleteRoleByIds(any(Long[].class));
    }

    @Test
    void checkRoleDataScope_filtersInOnce()
    {
        SysRole r1 = newRole(201L, "管理员");
        SysRole r2 = newRole(202L, "操作员");
        SysRole r3 = newRole(203L, "审计员");
        // 与传入 roleIds 一一对应, 避免触发"无权限"抛错
        when(roleMapper.selectRoleList(any(SysRole.class))).thenReturn(Arrays.asList(r1, r2, r3));

        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class);
             MockedStatic<SpringUtils> spr = openSpringUtilsStub())
        {
            sec.when(SecurityUtils::isAdmin).thenReturn(false);
            // 批量传入多个 roleId, 期望只走 1 次 selectRoleList (IN 查询)
            service.checkRoleDataScope(201L, 202L, 203L);
        }

        verify(roleMapper, times(1)).selectRoleList(any(SysRole.class));
    }

    @Test
    void deleteRoleByIds_adminShortCircuitsDataScopeCheck()
    {
        SysRole r = newRole(201L, "管理员");
        when(roleMapper.selectRolesByIds(anyList())).thenReturn(Collections.singletonList(r));
        when(userRoleMapper.countUserRoleByRoleIds(anyList())).thenReturn(Collections.emptyList());
        when(roleMapper.deleteRoleByIds(any(Long[].class))).thenReturn(1);

        int rows;
        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class);
             MockedStatic<SpringUtils> spr = openSpringUtilsStub())
        {
            // isAdmin() = true: 短路 checkRoleDataScope, 不调 selectRoleList
            sec.when(SecurityUtils::isAdmin).thenReturn(true);
            rows = service.deleteRoleByIds(new Long[]{201L});
        }

        assertEquals(1, rows);
        verify(roleMapper, times(1)).selectRolesByIds(anyList());
        verify(roleMapper, never()).selectRoleList(any(SysRole.class));
        verify(roleMapper, times(1)).deleteRoleByIds(any(Long[].class));
    }
}