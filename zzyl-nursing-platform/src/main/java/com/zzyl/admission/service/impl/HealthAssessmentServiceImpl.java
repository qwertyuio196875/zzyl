package com.zzyl.admission.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.admission.domain.HealthAssessment;
import com.zzyl.admission.mapper.HealthAssessmentMapper;
import com.zzyl.admission.service.IHealthAssessmentService;
import com.zzyl.admission.util.AdmissionNoUtils;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.StringUtils;

@Service
public class HealthAssessmentServiceImpl implements IHealthAssessmentService
{
    @Autowired
    private HealthAssessmentMapper healthAssessmentMapper;

    @Override
    public HealthAssessment selectHealthAssessmentById(Long id)
    {
        return healthAssessmentMapper.selectHealthAssessmentById(id);
    }

    @Override
    public List<HealthAssessment> selectHealthAssessmentList(HealthAssessment healthAssessment)
    {
        return healthAssessmentMapper.selectHealthAssessmentList(healthAssessment);
    }

    @Override
    public int insertHealthAssessment(HealthAssessment healthAssessment)
    {
        if (StringUtils.isEmpty(healthAssessment.getElderName()))
        {
            throw new ServiceException("老人姓名不能为空");
        }
        healthAssessment.setAssessmentNo(AdmissionNoUtils.assessmentNo());
        if (StringUtils.isEmpty(healthAssessment.getStatus()))
        {
            healthAssessment.setStatus("0");
        }
        healthAssessment.setCreateTime(DateUtils.getNowDate());
        return healthAssessmentMapper.insertHealthAssessment(healthAssessment);
    }

    @Override
    public int updateHealthAssessment(HealthAssessment healthAssessment)
    {
        HealthAssessment existing = healthAssessmentMapper.selectHealthAssessmentById(healthAssessment.getId());
        if (existing == null)
        {
            throw new ServiceException("健康评估记录不存在");
        }
        if ("1".equals(existing.getStatus()))
        {
            throw new ServiceException("已完成的评估不允许修改");
        }
        healthAssessment.setUpdateTime(DateUtils.getNowDate());
        return healthAssessmentMapper.updateHealthAssessment(healthAssessment);
    }

    @Override
    public int deleteHealthAssessmentByIds(Long[] ids)
    {
        return healthAssessmentMapper.deleteHealthAssessmentByIds(ids);
    }

    @Override
    public int completeAssessment(Long id)
    {
        HealthAssessment assessment = healthAssessmentMapper.selectHealthAssessmentById(id);
        if (assessment == null)
        {
            throw new ServiceException("健康评估记录不存在");
        }
        if ("1".equals(assessment.getStatus()))
        {
            throw new ServiceException("评估已完成");
        }
        HealthAssessment update = new HealthAssessment();
        update.setId(id);
        update.setStatus("1");
        update.setUpdateTime(DateUtils.getNowDate());
        return healthAssessmentMapper.updateHealthAssessment(update);
    }
}
