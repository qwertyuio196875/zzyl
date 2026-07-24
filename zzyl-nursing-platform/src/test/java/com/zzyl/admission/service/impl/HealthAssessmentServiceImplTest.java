package com.zzyl.admission.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zzyl.admission.domain.HealthAssessment;
import com.zzyl.admission.mapper.HealthAssessmentMapper;
import com.zzyl.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class HealthAssessmentServiceImplTest
{
    @Mock
    private HealthAssessmentMapper healthAssessmentMapper;

    @InjectMocks
    private HealthAssessmentServiceImpl healthAssessmentService;

    @Test
    void insertShouldGenerateNoAndDefaultStatus()
    {
        HealthAssessment input = new HealthAssessment();
        input.setElderName("张三");
        when(healthAssessmentMapper.insertHealthAssessment(any())).thenReturn(1);

        int rows = healthAssessmentService.insertHealthAssessment(input);

        assertEquals(1, rows);
        assertNotNull(input.getAssessmentNo());
        assertEquals("0", input.getStatus());
    }

    @Test
    void insertShouldRejectBlankName()
    {
        assertThrows(ServiceException.class, () -> healthAssessmentService.insertHealthAssessment(new HealthAssessment()));
    }

    @Test
    void updateShouldRejectCompletedRecord()
    {
        HealthAssessment existing = new HealthAssessment();
        existing.setId(1L);
        existing.setStatus("1");
        when(healthAssessmentMapper.selectHealthAssessmentById(1L)).thenReturn(existing);

        HealthAssessment update = new HealthAssessment();
        update.setId(1L);
        assertThrows(ServiceException.class, () -> healthAssessmentService.updateHealthAssessment(update));
    }

    @Test
    void completeShouldSetStatusDone()
    {
        HealthAssessment existing = new HealthAssessment();
        existing.setId(1L);
        existing.setStatus("0");
        when(healthAssessmentMapper.selectHealthAssessmentById(1L)).thenReturn(existing);
        when(healthAssessmentMapper.updateHealthAssessment(any())).thenReturn(1);

        int rows = healthAssessmentService.completeAssessment(1L);

        assertEquals(1, rows);
        verify(healthAssessmentMapper).updateHealthAssessment(any());
    }
}
