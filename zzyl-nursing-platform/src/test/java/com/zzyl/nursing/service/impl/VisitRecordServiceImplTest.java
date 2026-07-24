package com.zzyl.nursing.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zzyl.common.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zzyl.nursing.domain.VisitRecord;
import com.zzyl.nursing.mapper.VisitRecordMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VisitRecordServiceImpl 单元测试（P0-3 visit_record 类型修复配套）
 * <p>
 * 注：visitDate / visitTime 在 Java 端保留 String，由 MyBatis TypeHandler
 * （com.zzyl.framework.handler.StringToDateTypeHandler /
 * StringToTimeTypeHandler）在 JDBC 边界负责转换。
 * 单测聚焦：Service 层透传字段、日期范围过滤、状态切换等行为。
 *
 * @see <a href="DATABASE_OPTIMIZATION_RECOMMENDATIONS.md">DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-3</a>
 */
@ExtendWith(MockitoExtension.class)
class VisitRecordServiceImplTest
{
    @Mock private VisitRecordMapper visitRecordMapper;
    @InjectMocks private VisitRecordServiceImpl service;

    private static VisitRecord newVisitRecord()
    {
        VisitRecord r = new VisitRecord();
        r.setVisitorName("张三");
        r.setVisitedName("李四");
        r.setVisitDate("2026-07-24");
        r.setVisitTime("14:30:00");
        r.setStatus(0L); // 待审核
        return r;
    }

    @Test
    void insertVisitRecord_keepsStringDatesAndCallsMapper()
    {
        VisitRecord input = newVisitRecord();

        when(visitRecordMapper.insertVisitRecord(any(VisitRecord.class))).thenReturn(1);

        int rows = service.insertVisitRecord(input);

        assertEquals(1, rows, "mapper 返回值应透传");
        ArgumentCaptor<VisitRecord> captor = ArgumentCaptor.forClass(VisitRecord.class);
        verify(visitRecordMapper, times(1)).insertVisitRecord(captor.capture());
        VisitRecord passed = captor.getValue();
        assertNotNull(passed.getCreateTime(), "Service 应填充 createTime");
        assertEquals("2026-07-24", passed.getVisitDate(), "visitDate 应保持 String 而非 Date");
        assertEquals("14:30:00", passed.getVisitTime(), "visitTime 应保持 String");
    }

    @Test
    void selectVisitRecordList_passesFiltersToMapper()
    {
        VisitRecord query = new VisitRecord();
        Map<String, Object> params = new HashMap<>();
        params.put("beginVisitDate", "2026-07-01");
        params.put("endVisitDate", "2026-07-31");
        query.setParams(params);
        query.setStatus(1L);
        when(visitRecordMapper.selectVisitRecordList(any(VisitRecord.class)))
                .thenReturn(Collections.singletonList(newVisitRecord()));

        List<VisitRecord> out = service.selectVisitRecordList(query);

        assertEquals(1, out.size());
        verify(visitRecordMapper, times(1)).selectVisitRecordList(query);
        // params 应原样透传，下游 SQL 形如 visit_date >= #{params.beginVisitDate}
        assertEquals("2026-07-01", query.getParams().get("beginVisitDate"));
        assertEquals("2026-07-31", query.getParams().get("endVisitDate"));
    }

    @Test
    void signInVisitRecord_setsActualVisitTimeAndStatusAndCallsMapper()
    {
        when(visitRecordMapper.updateVisitRecord(any(VisitRecord.class))).thenReturn(1);

        service.signInVisitRecord(1001L);

        ArgumentCaptor<VisitRecord> captor = ArgumentCaptor.forClass(VisitRecord.class);
        verify(visitRecordMapper, times(1)).updateVisitRecord(captor.capture());
        VisitRecord sent = captor.getValue();
        assertEquals(Long.valueOf(1001L), sent.getId(), "应传主键");
        assertEquals(Long.valueOf(2L), sent.getStatus(), "签到后状态应为 2（已签到）");
        assertNotNull(sent.getActualVisitTime(), "应写入实际签到时间");
        assertNotNull(sent.getUpdateTime(), "应刷新 updateTime");
    }

    @Test
    void leaveVisitRecord_setsLeaveTimeAndStatusAndCallsMapper()
    {
        when(visitRecordMapper.updateVisitRecord(any(VisitRecord.class))).thenReturn(1);

        service.leaveVisitRecord(2002L);

        ArgumentCaptor<VisitRecord> captor = ArgumentCaptor.forClass(VisitRecord.class);
        verify(visitRecordMapper, atLeastOnce()).updateVisitRecord(captor.capture());
        VisitRecord sent = captor.getValue();
        assertEquals(Long.valueOf(2002L), sent.getId());
        assertEquals(Long.valueOf(3L), sent.getStatus(), "离开后状态应为 3（已离开）");
        assertNotNull(sent.getLeaveTime(), "应写入离开时间");
    }
}
