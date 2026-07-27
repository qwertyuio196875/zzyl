package com.zzyl.system.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.zzyl.common.core.domain.entity.SysDept;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.system.mapper.SysDeptMapper;
import com.zzyl.system.mapper.SysRoleMapper;
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
 * SysDeptServiceImpl.updateDeptSort 单元测试（P2-8 事务边界修复 A1）
 * <p>
 * 验证：
 * 1) N 个 deptId + N 个 orderNum → 调 N 次 deptMapper.updateDeptSort，参数一一对应
 * 2) 单条失败时异常被原样抛出（事务边界已消失）
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P2-8</a>
 */
@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest
{
    @Mock private SysDeptMapper deptMapper;
    @Mock private SysRoleMapper roleMapper;

    @InjectMocks private SysDeptServiceImpl service;

    @Test
    void updateDeptSort_callsMapperForEachItem()
    {
        String[] deptIds = new String[]{"201", "202", "203"};
        String[] orderNums = new String[]{"10", "20", "30"};

        service.updateDeptSort(deptIds, orderNums);

        ArgumentCaptor<SysDept> captor = ArgumentCaptor.forClass(SysDept.class);
        verify(deptMapper, times(3)).updateDeptSort(captor.capture());

        List<SysDept> captured = captor.getAllValues();
        assertEquals(201L, captured.get(0).getDeptId().longValue());
        assertEquals(10, captured.get(0).getOrderNum().intValue());
        assertEquals(202L, captured.get(1).getDeptId().longValue());
        assertEquals(20, captured.get(1).getOrderNum().intValue());
        assertEquals(203L, captured.get(2).getDeptId().longValue());
        assertEquals(30, captured.get(2).getOrderNum().intValue());
    }

    @Test
    void updateDeptSort_singleItemFailure_throwsWithoutRollback()
    {
        // P2-8 A1: 事务消失，第 2 条失败时异常直接抛出
        String[] deptIds = new String[]{"201", "202", "203"};
        String[] orderNums = new String[]{"10", "20", "30"};
        AtomicInteger cnt = new AtomicInteger(0);
        doAnswer(inv -> {
            if (cnt.incrementAndGet() == 2)
            {
                throw new RuntimeException("mock update failure");
            }
            return 0;
        }).when(deptMapper).updateDeptSort(any(SysDept.class));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateDeptSort(deptIds, orderNums));
        assertTrue(ex.getMessage().contains("保存排序异常"));
    }
}
