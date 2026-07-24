package com.zzyl.admission.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.zzyl.admission.domain.HealthAssessment;

@Mapper
public interface HealthAssessmentMapper
{
    HealthAssessment selectHealthAssessmentById(Long id);

    List<HealthAssessment> selectHealthAssessmentList(HealthAssessment healthAssessment);

    int insertHealthAssessment(HealthAssessment healthAssessment);

    int updateHealthAssessment(HealthAssessment healthAssessment);

    int deleteHealthAssessmentById(Long id);

    int deleteHealthAssessmentByIds(Long[] ids);
}
