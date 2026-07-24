package com.zzyl.system.service.impl;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zzyl.common.core.domain.entity.SysUser;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.system.mapper.SysPostMapper;
import com.zzyl.system.mapper.SysRoleMapper;
import com.zzyl.system.mapper.SysUserMapper;
import com.zzyl.system.mapper.SysUserPostMapper;
import com.zzyl.system.mapper.SysUserRoleMapper;
import com.zzyl.system.service.ISysConfigService;
import com.zzyl.system.service.ISysDeptService;

import jakarta.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SysUserServiceImpl.importUser 单元测试（P0-2 N+1 修复）
 * <p>
 * 验证：
 * 1) 空列表入口校验
 * 2) 全部新用户 → 仅调 batchInsertUser，不调逐条 selectUserByUserName
 * 3) 已存在 + isUpdateSupport=true → 仅调 batchUpdateUser
 * 4) 已存在 + isUpdateSupport=false → 抛 ServiceException 且不发任何 insert/update
 * 5) 批量 update 异常 → 自动降级逐条 updateUser，最终返回成功
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-2</a>
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest
{
    @Mock private SysUserMapper userMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysPostMapper postMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysUserPostMapper userPostMapper;
    @Mock private ISysConfigService configService;
    @Mock private ISysDeptService deptService;
    @Mock private Validator validator;

    @InjectMocks private SysUserServiceImpl service;

    private static SysUser newUser(String userName)
    {
        SysUser u = new SysUser();
        u.setUserName(userName);
        u.setDeptId(1L);
        u.setEmail(userName + "@example.com");
        u.setPhonenumber("13800000000");
        return u;
    }

    private static SysUser existingUser(String userName, Long userId)
    {
        SysUser u = new SysUser();
        u.setUserName(userName);
        u.setUserId(userId);
        u.setDeptId(1L);
        return u;
    }

    /**
     * 在更新分支走 checkUser* 路径时，SecurityUtils.isAdmin 静态调用会触发
     * SecurityContextHolder.getContext()（无 Spring Context 时抛"获取用户ID异常"）。
     * 调用方式（务必在 try-with-resources 内使用，块结束自动 close）：
     * <pre>
     * try (MockedStatic&lt;SecurityUtils&gt; mocked = openSecurityUtilsStaticStub()) {
     *     // 走到 checkUserAllowed / checkUserDataScope 的代码
     * }
     * </pre>
     */
    private static MockedStatic<SecurityUtils> openSecurityUtilsStaticStub()
    {
        MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class);
        // isAdmin()（无参） → true：让 checkUserDataScope 短路放行
        mocked.when(SecurityUtils::isAdmin).thenReturn(true);
        // isAdmin(Long)   → false：让 checkUserAllowed 不抛"不允许操作超级管理员"
        mocked.when(() -> SecurityUtils.isAdmin(any(Long.class))).thenReturn(false);
        return mocked;
    }

    @Test
    void importUser_emptyList_throwsServiceException()
    {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.importUser(Collections.emptyList(), Boolean.FALSE, "admin"));
        assertTrue(ex.getMessage().contains("导入用户数据不能为空"), "应包含固定错误文案");

        // 不应触发任何 mapper 调用
        verify(userMapper, never()).selectUsersByUserNames(anyList());
        verify(userMapper, never()).batchInsertUser(anyList());
        verify(userMapper, never()).batchUpdateUser(anyList());
    }

    @Test
    void importUser_allNew_callsBatchInsertOnly()
    {
        SysUser u = newUser("alice");
        when(configService.selectConfigByKey("sys.user.initPassword")).thenReturn("Pwd@123");
        when(userMapper.selectUsersByUserNames(anyList())).thenReturn(Collections.emptyList());

        String result = service.importUser(Collections.singletonList(u), Boolean.FALSE, "admin");

        assertNotNull(result);
        assertTrue(result.contains("共 1 条"), "应有成功汇总文案");
        assertTrue(result.contains("alice"), "应包含用户名");

        // 关键：仅 1 次批量查询 + 1 次批量 insert；没有逐条查、没有 update
        verify(userMapper, times(1)).selectUsersByUserNames(anyList());
        verify(userMapper, times(1)).batchInsertUser(anyList());
        verify(userMapper, never()).selectUserByUserName(any());
        verify(userMapper, never()).batchUpdateUser(anyList());
        verify(userMapper, never()).insertUser(any(SysUser.class));
        verify(userMapper, never()).updateUser(any(SysUser.class));

        // 入参 user 应被填充了 password 和 createBy
        ArgumentCaptor<List<SysUser>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMapper).batchInsertUser(captor.capture());
        SysUser inserted = captor.getValue().get(0);
        assertNotNull(inserted.getPassword(), "新增用户应填充加密密码");
        assertEquals("admin", inserted.getCreateBy(), "应记录 operName 到 createBy");
    }

    @Test
    void importUser_existingWithUpdateSupport_callsBatchUpdateOnly()
    {
        SysUser incoming = newUser("bob");
        SysUser existing = existingUser("bob", 99L);
        when(userMapper.selectUsersByUserNames(anyList())).thenReturn(Collections.singletonList(existing));

        String result;
        try (MockedStatic<SecurityUtils> mocked = openSecurityUtilsStaticStub())
        {
            result = service.importUser(Collections.singletonList(incoming), Boolean.TRUE, "admin");
        }

        assertTrue(result.contains("更新成功"), "应走更新分支");
        verify(userMapper, never()).batchInsertUser(anyList());
        verify(userMapper, never()).insertUser(any(SysUser.class));
        verify(userMapper, times(1)).batchUpdateUser(anyList());

        // 更新列表应已被赋值 userId/deptId/updateBy
        ArgumentCaptor<List<SysUser>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMapper).batchUpdateUser(captor.capture());
        SysUser updated = captor.getValue().get(0);
        assertEquals(99L, updated.getUserId(), "应从已有记录拷贝 userId");
        assertEquals("admin", updated.getUpdateBy(), "应记录 operName 到 updateBy");
    }

    @Test
    void importUser_existingWithoutUpdateSupport_throwsWithExistedMessage()
    {
        SysUser incoming = newUser("cathy");
        SysUser existing = existingUser("cathy", 100L);
        when(userMapper.selectUsersByUserNames(anyList())).thenReturn(Collections.singletonList(existing));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.importUser(Collections.singletonList(incoming), Boolean.FALSE, "admin"));

        // 文案模板与原版保持一致
        assertTrue(ex.getMessage().contains("已存在"), "应包含「已存在」失败原因");
        assertTrue(ex.getMessage().contains("cathy"), "应包含用户名");
        assertTrue(ex.getMessage().startsWith("很抱歉"), "应以失败汇总头开始");

        // 关键：不应有任何 insert / update 落库
        verify(userMapper, never()).batchInsertUser(anyList());
        verify(userMapper, never()).batchUpdateUser(anyList());
        verify(userMapper, never()).insertUser(any(SysUser.class));
        verify(userMapper, never()).updateUser(any(SysUser.class));
    }

    @Test
    void importUser_batchUpdateThrows_degradesToSingleUpdateUser()
    {
        SysUser incoming = newUser("david");
        SysUser existing = existingUser("david", 101L);
        when(userMapper.selectUsersByUserNames(anyList())).thenReturn(Collections.singletonList(existing));
        // 模拟批量 SQL 整体异常，强制走降级逐条路径
        doThrow(new RuntimeException("mock batch update failure"))
                .when(userMapper).batchUpdateUser(anyList());

        String result;
        try (MockedStatic<SecurityUtils> mocked = openSecurityUtilsStaticStub())
        {
            result = service.importUser(Collections.singletonList(incoming), Boolean.TRUE, "admin");
        }

        // 降级后仍应能产出"更新成功"消息（与原版逐条同义）
        assertTrue(result.contains("更新成功"), "降级路径应仍产成功消息");
        verify(userMapper, times(1)).batchUpdateUser(anyList());
        verify(userMapper, atLeastOnce()).updateUser(any(SysUser.class));
    }
}
