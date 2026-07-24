package com.zzyl.admission.service;

import java.util.List;
import com.zzyl.admission.domain.HealthAssessment;

public interface IHealthAssessmentService
{
    HealthAssessment selectHealthAssessmentById(Long id);

    List<HealthAssessment> selectHealthAssessmentList(HealthAssessment healthAssessment);

    int insertHealthAssessment(HealthAssessment healthAssessment);

    int updateHealthAssessment(HealthAssessment healthAssessment);

    int deleteHealthAssessmentByIds(Long[] ids);

    int completeAssessment(Long id);
}
