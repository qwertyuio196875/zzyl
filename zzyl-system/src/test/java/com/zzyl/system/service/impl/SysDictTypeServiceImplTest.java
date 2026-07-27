package com.zzyl.system.service.impl;

import java.util.Collections;

import com.zzyl.common.core.domain.entity.SysDictType;
import com.zzyl.common.utils.DictUtils;
import com.zzyl.system.mapper.SysDictDataMapper;
import com.zzyl.system.mapper.SysDictTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SysDictTypeServiceImpl.updateDictType 单元测试（P2-8 事务边界修复 B1）
 * <p>
 * 验证：
 * 1) 缓存写成功 → 正常返回 row
 * 2) B1 核心: 缓存写异常时仍返回 row 值（异常被 catch，DB 事务不被回滚）
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P2-8</a>
 */
@ExtendWith(MockitoExtension.class)
class SysDictTypeServiceImplTest
{
    @Mock private SysDictTypeMapper dictTypeMapper;
    @Mock private SysDictDataMapper dictDataMapper;

    @InjectMocks private SysDictTypeServiceImpl service;

    @Test
    void updateDictType_cacheWriteFails_dbStillSucceeds()
    {
        SysDictType oldDict = new SysDictType();
        oldDict.setDictId(1L);
        oldDict.setDictType("user_status");
        SysDictType newDict = new SysDictType();
        newDict.setDictId(1L);
        newDict.setDictType("user_status_new");

        when(dictTypeMapper.selectDictTypeById(1L)).thenReturn(oldDict);
        when(dictDataMapper.updateDictDataType(any(), any())).thenReturn(1);
        when(dictTypeMapper.updateDictType(newDict)).thenReturn(1);
        when(dictDataMapper.selectDictDataByType("user_status_new")).thenReturn(Collections.emptyList());

        int row;
        try (MockedStatic<DictUtils> mocked = Mockito.mockStatic(DictUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            // 模拟 Redis 写入失败
            mocked.when(() -> DictUtils.setDictCache(eq("user_status_new"), anyList()))
                    .thenThrow(new RuntimeException("mock redis failure"));
            row = service.updateDictType(newDict);
        }

        // 关键: DB update 成功，row 应返回 1；缓存异常被 catch，未触发事务回滚
        assertEquals(1, row);
        verify(dictTypeMapper, times(1)).updateDictType(newDict);
    }

    @Test
    void updateDictType_cacheWriteSucceeds_normalPath()
    {
        SysDictType oldDict = new SysDictType();
        oldDict.setDictId(2L);
        oldDict.setDictType("sex");
        SysDictType newDict = new SysDictType();
        newDict.setDictId(2L);
        newDict.setDictType("sex");

        when(dictTypeMapper.selectDictTypeById(2L)).thenReturn(oldDict);
        when(dictDataMapper.updateDictDataType(any(), any())).thenReturn(0);
        when(dictTypeMapper.updateDictType(newDict)).thenReturn(1);
        when(dictDataMapper.selectDictDataByType("sex")).thenReturn(Collections.emptyList());

        int row;
        try (MockedStatic<DictUtils> mocked = Mockito.mockStatic(DictUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            row = service.updateDictType(newDict);
        }

        assertEquals(1, row);
        verify(dictTypeMapper, times(1)).updateDictType(newDict);
    }
}
