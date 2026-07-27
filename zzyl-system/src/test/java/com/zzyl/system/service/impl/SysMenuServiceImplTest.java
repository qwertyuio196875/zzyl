package com.zzyl.system.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.zzyl.common.core.domain.entity.SysMenu;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.system.mapper.SysMenuMapper;
import com.zzyl.system.mapper.SysRoleMapper;
import com.zzyl.system.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * SysMenuServiceImpl.updateMenuSort 单元测试（P2-8 事务边界修复 A1）
 * <p>
 * 验证：
 * 1) N 个 menuId + N 个 orderNum → 调 N 次 menuMapper.updateMenuSort，参数一一对应
 * 2) 单条失败时异常被原样抛出（事务边界已消失，不再回滚整批）
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P2-8</a>
 */
@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest
{
    @Mock private SysMenuMapper menuMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysRoleMenuMapper roleMenuMapper;

    @InjectMocks private SysMenuServiceImpl service;

    @Test
    void updateMenuSort_callsMapperForEachItem()
    {
        String[] menuIds = new String[]{"101", "102", "103"};
        String[] orderNums = new String[]{"1", "2", "3"};

        service.updateMenuSort(menuIds, orderNums);

        // 关键: 调 N 次 updateMenuSort
        ArgumentCaptor<SysMenu> captor = ArgumentCaptor.forClass(SysMenu.class);
        verify(menuMapper, times(3)).updateMenuSort(captor.capture());

        // 验证参数一一对应
        List<SysMenu> captured = captor.getAllValues();
        assertEquals(101L, captured.get(0).getMenuId().longValue());
        assertEquals(1, captured.get(0).getOrderNum().intValue());
        assertEquals(102L, captured.get(1).getMenuId().longValue());
        assertEquals(2, captured.get(1).getOrderNum().intValue());
        assertEquals(103L, captured.get(2).getMenuId().longValue());
        assertEquals(3, captured.get(2).getOrderNum().intValue());
    }

    @Test
    void updateMenuSort_singleItemFailure_throwsWithoutRollback()
    {
        // P2-8 A1: 改造后方法不再被 @Transactional 包裹，第 2 条失败时异常直接抛出
        // 不再回滚（这正是 A1 设计的"事务消失"行为——单条独立提交，单条失败不影响其他）
        String[] menuIds = new String[]{"101", "102", "103"};
        String[] orderNums = new String[]{"1", "2", "3"};
        AtomicInteger cnt = new AtomicInteger(0);
        doAnswer(inv -> {
            if (cnt.incrementAndGet() == 2)
            {
                throw new RuntimeException("mock update failure");
            }
            return 0;
        }).when(menuMapper).updateMenuSort(any(SysMenu.class));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateMenuSort(menuIds, orderNums));
        assertTrue(ex.getMessage().contains("保存排序异常"));
    }
}
