package com.zzyl.admission.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.mapper.NursingPlanMapper;

@ExtendWith(MockitoExtension.class)
class ResidentCheckInServiceImplTest
{
    @Mock
    private ResidentCheckInMapper residentCheckInMapper;

    @Mock
    private HealthAssessmentMapper healthAssessmentMapper;

    @Mock
    private NursingLevelMapper nursingLevelMapper;

    @Mock
    private NursingPlanMapper nursingPlanMapper;

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
    void insertShouldFillRedundantFieldsFromSources()
    {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("1");
        assessment.setElderName("权威-张三");
        assessment.setIdCard("110101199001011234");
        assessment.setGender("1");
        assessment.setAge(80);
        assessment.setPhone("13800000000");
        NursingLevel level = new NursingLevel();
        level.setId(20L);
        level.setName("特护级");
        NursingPlan plan = new NursingPlan();
        plan.setId(30L);
        plan.setPlanName("术后康复计划");

        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);
        when(nursingLevelMapper.selectNursingLevelById(20L)).thenReturn(level);
        when(nursingPlanMapper.selectNursingPlanById(30L)).thenReturn(plan);
        when(residentCheckInMapper.insertResidentCheckIn(any())).thenReturn(1);

        ResidentCheckIn input = new ResidentCheckIn();
        // 前端任意值应该被覆盖
        input.setElderName("前端-脏值");
        input.setIdCard("前端-身份证");
        input.setGender("0");
        input.setAge(0);
        input.setPhone("前端-电话");
        input.setNursingLevelName("前端-等级");
        input.setNursingPlanName("前端-计划");
        input.setAssessmentId(10L);
        input.setNursingLevelId(20L);
        input.setNursingPlanId(30L);

        int rows = residentCheckInService.insertResidentCheckIn(input);

        assertEquals(1, rows);
        assertNotNull(input.getCheckInNo());
        // 单源回填覆盖前端任意值
        assertEquals("权威-张三", input.getElderName());
        assertEquals("110101199001011234", input.getIdCard());
        assertEquals("1", input.getGender());
        assertEquals(80, input.getAge());
        assertEquals("13800000000", input.getPhone());
        assertEquals("特护级", input.getNursingLevelName());
        assertEquals("术后康复计划", input.getNursingPlanName());
    }

    @Test
    void insertShouldSucceedWithCompletedAssessment()
    {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("1");
        assessment.setElderName("张三");
        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);
        when(residentCheckInMapper.insertResidentCheckIn(any())).thenReturn(1);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setElderName("李四");
        input.setAssessmentId(10L);

        int rows = residentCheckInService.insertResidentCheckIn(input);

        assertEquals(1, rows);
        assertNotNull(input.getCheckInNo());
        // 同样被覆盖为权威值
        assertEquals("张三", input.getElderName());
    }

    @Test
    void insertShouldThrowWhenAssessmentMissingElderName()
    {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("1");
        // 权威表也没有 elderName
        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setElderName("李四");
        input.setAssessmentId(10L);

        assertThrows(ServiceException.class, () -> residentCheckInService.insertResidentCheckIn(input));
    }

    @Test
    void updateShouldRejectCheckedInRecord()
    {
        ResidentCheckIn existing = new ResidentCheckIn();
        existing.setId(1L);
        existing.setStatus("2");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(existing);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setId(1L);

        assertThrows(ServiceException.class, () -> residentCheckInService.updateResidentCheckIn(input));
    }

    @Test
    void updateShouldRefillRedundantFields()
    {
        ResidentCheckIn existing = new ResidentCheckIn();
        existing.setId(1L);
        existing.setStatus("1");

        HealthAssessment assessment = new HealthAssessment();
        assessment.setId(10L);
        assessment.setStatus("1");
        assessment.setElderName("权威-更新后");
        assessment.setIdCard("NEW-ID");

        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(existing);
        when(healthAssessmentMapper.selectHealthAssessmentById(10L)).thenReturn(assessment);
        when(residentCheckInMapper.updateResidentCheckIn(any())).thenReturn(1);

        ResidentCheckIn input = new ResidentCheckIn();
        input.setId(1L);
        input.setAssessmentId(10L);
        input.setElderName("前端脏值");

        int rows = residentCheckInService.updateResidentCheckIn(input);

        assertEquals(1, rows);
        // 权威覆盖前端脏值
        assertEquals("权威-更新后", input.getElderName());
        assertEquals("NEW-ID", input.getIdCard());
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

    @Test
    void cancelShouldAcceptPendingRecord()
    {
        ResidentCheckIn existing = new ResidentCheckIn();
        existing.setId(1L);
        existing.setStatus("0");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(existing);
        when(residentCheckInMapper.updateResidentCheckIn(any())).thenReturn(1);

        int rows = residentCheckInService.cancelCheckIn(1L);

        assertEquals(1, rows);
        verify(residentCheckInMapper).updateResidentCheckIn(any());
        verifyNoInteractions(healthAssessmentMapper, nursingLevelMapper, nursingPlanMapper);
    }
}