package com.zzyl.admission.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zzyl.admission.domain.HealthAssessment;
import com.zzyl.admission.domain.ResidentCheckIn;
import com.zzyl.admission.mapper.HealthAssessmentMapper;
import com.zzyl.admission.mapper.ResidentCheckInMapper;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class ResidentCheckInServiceImplTest
{
    @Mock
    private ResidentCheckInMapper residentCheckInMapper;

    @Mock
    private HealthAssessmentMapper healthAssessmentMapper;

    @InjectMocks
    private ResidentCheckInServiceImpl residentCheckInService;

    @Test
    void insertShouldRejectUnfinishedAssessment()
    {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("0");
        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setElderName("李四");
        input.setAssessmentId(10L);

        assertThrows(ServiceException.class, () -> residentCheckInService.insertResidentCheckIn(input));
    }

    @Test
    void insertShouldSucceedWithCompletedAssessment()
    {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("1");
        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);
        when(residentCheckInMapper.insertResidentCheckIn(any())).thenReturn(1);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setElderName("李四");
        input.setAssessmentId(10L);

        int rows = residentCheckInService.insertResidentCheckIn(input);

        assertEquals(1, rows);
        assertNotNull(input.getCheckInNo());
    }

    @Test
    void confirmShouldSetCheckedInStatus()
    {
        ResidentCheckIn existing = new ResidentCheckIn();
        existing.setId(1L);
        existing.setStatus("1");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(existing);
        when(residentCheckInMapper.updateResidentCheckIn(any())).thenReturn(1);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class))
        {
            securityUtils.when(SecurityUtils::getUsername).thenReturn("admin");
            int rows = residentCheckInService.confirmCheckIn(1L);
            assertEquals(1, rows);
        }
    }

    @Test
    void cancelShouldRejectCheckedInRecord()
    {
        ResidentCheckIn existing = new ResidentCheckIn();
        existing.setId(1L);
        existing.setStatus("2");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(existing);

        assertThrows(ServiceException.class, () -> residentCheckInService.cancelCheckIn(1L));
    }
}
