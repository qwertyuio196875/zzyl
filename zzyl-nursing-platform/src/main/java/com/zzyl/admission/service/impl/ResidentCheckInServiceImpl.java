package com.zzyl.admission.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.admission.domain.HealthAssessment;
import com.zzyl.admission.domain.ResidentCheckIn;
import com.zzyl.admission.mapper.HealthAssessmentMapper;
import com.zzyl.admission.mapper.ResidentCheckInMapper;
import com.zzyl.admission.service.IResidentCheckInService;
import com.zzyl.admission.util.AdmissionNoUtils;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.mapper.NursingPlanMapper;

/**
 * 入住办理 Service。
 *
 * P1-7 修复：冗余字段（elderName / idCard / gender / age / phone / nursingLevelName / nursingPlanName）
 * 不再依赖前端任意传入，统一在 Service 层从权威表回填，避免多源真相。
 * Mapper XML 保留冗余列的写是为了兼容历史数据展示与导出，下一阶段可清理。
 */
@Service
public class ResidentCheckInServiceImpl implements IResidentCheckInService
{
    @Autowired
    private ResidentCheckInMapper residentCheckInMapper;

    @Autowired
    private HealthAssessmentMapper healthAssessmentMapper;

    @Autowired
    private NursingLevelMapper nursingLevelMapper;

    @Autowired
    private NursingPlanMapper nursingPlanMapper;

    @Override
    public ResidentCheckIn selectResidentCheckInById(Long id)
    {
        return residentCheckInMapper.selectResidentCheckInById(id);
    }

    @Override
    public List<ResidentCheckIn> selectResidentCheckInList(ResidentCheckIn residentCheckIn)
    {
        return residentCheckInMapper.selectResidentCheckInList(residentCheckIn);
    }

    @Override
    public List<ResidentCheckIn> selectCheckedInList()
    {
        return residentCheckInMapper.selectCheckedInList();
    }

    @Override
    public int insertResidentCheckIn(ResidentCheckIn residentCheckIn)
    {
        validateAssessment(residentCheckIn.getAssessmentId());
        // 单源回填：从 health_assessment / nursing_level / nursing_plan 写入冗余字段
        fillRedundantFieldsFromSources(residentCheckIn);
        if (StringUtils.isEmpty(residentCheckIn.getElderName()))
        {
            throw new ServiceException("老人姓名不能为空");
        }
        residentCheckIn.setCheckInNo(AdmissionNoUtils.checkInNo());
        if (StringUtils.isEmpty(residentCheckIn.getStatus()))
        {
            residentCheckIn.setStatus("0");
        }
        residentCheckIn.setCreateTime(DateUtils.getNowDate());
        return residentCheckInMapper.insertResidentCheckIn(residentCheckIn);
    }

    @Override
    public int updateResidentCheckIn(ResidentCheckIn residentCheckIn)
    {
        ResidentCheckIn existing = residentCheckInMapper.selectResidentCheckInById(residentCheckIn.getId());
        if (existing == null)
        {
            throw new ServiceException("入住记录不存在");
        }
        if ("2".equals(existing.getStatus()) || "3".equals(existing.getStatus()) || "4".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改");
        }
        validateAssessment(residentCheckIn.getAssessmentId());
        // 单源回填：每次更新都重新从权威表覆盖冗余字段，防止前端旧值污染
        fillRedundantFieldsFromSources(residentCheckIn);
        residentCheckIn.setUpdateTime(DateUtils.getNowDate());
        return residentCheckInMapper.updateResidentCheckIn(residentCheckIn);
    }

    @Override
    public int deleteResidentCheckInByIds(Long[] ids)
    {
        return residentCheckInMapper.deleteResidentCheckInByIds(ids);
    }

    @Override
    public int confirmCheckIn(Long id)
    {
        ResidentCheckIn checkIn = residentCheckInMapper.selectResidentCheckInById(id);
        if (checkIn == null)
        {
            throw new ServiceException("入住记录不存在");
        }
        if (!"0".equals(checkIn.getStatus()) && !"1".equals(checkIn.getStatus()))
        {
            throw new ServiceException("仅待办理或办理中的记录可确认入住");
        }
        ResidentCheckIn update = new ResidentCheckIn();
        update.setId(id);
        update.setStatus("2");
        update.setHandler(SecurityUtils.getUsername());
        if (checkIn.getCheckInDate() == null)
        {
            update.setCheckInDate(DateUtils.getNowDate());
        }
        update.setUpdateTime(DateUtils.getNowDate());
        return residentCheckInMapper.updateResidentCheckIn(update);
    }

    @Override
    public int cancelCheckIn(Long id)
    {
        ResidentCheckIn checkIn = residentCheckInMapper.selectResidentCheckInById(id);
        if (checkIn == null)
        {
            throw new ServiceException("入住记录不存在");
        }
        if ("2".equals(checkIn.getStatus()) || "4".equals(checkIn.getStatus()))
        {
            throw new ServiceException("已入住或已退住的记录不能取消");
        }
        ResidentCheckIn update = new ResidentCheckIn();
        update.setId(id);
        update.setStatus("3");
        update.setUpdateTime(DateUtils.getNowDate());
        return residentCheckInMapper.updateResidentCheckIn(update);
    }

    private void validateAssessment(Long assessmentId)
    {
        if (assessmentId == null)
        {
            return;
        }
        HealthAssessment assessment = healthAssessmentMapper.selectHealthAssessmentById(assessmentId);
        if (assessment == null)
        {
            throw new ServiceException("关联的健康评估不存在");
        }
        if (!"1".equals(assessment.getStatus()))
        {
            throw new ServiceException("须先完成健康评估再办理入住");
        }
    }

    /**
     * 单源回填冗余字段：覆盖前端任意传入值，确保 resident_check_in 与
     * health_assessment / nursing_level / nursing_plan 保持一致。
     */
    private void fillRedundantFieldsFromSources(ResidentCheckIn target)
    {
        if (target.getAssessmentId() != null)
        {
            HealthAssessment assessment = healthAssessmentMapper.selectHealthAssessmentById(target.getAssessmentId());
            if (assessment != null)
            {
                target.setElderName(assessment.getElderName());
                target.setIdCard(assessment.getIdCard());
                target.setGender(assessment.getGender());
                target.setAge(assessment.getAge());
                target.setPhone(assessment.getPhone());
            }
        }
        if (target.getNursingLevelId() != null)
        {
            NursingLevel level = nursingLevelMapper.selectNursingLevelById(target.getNursingLevelId());
            if (level != null)
            {
                target.setNursingLevelName(level.getName());
            }
        }
        if (target.getNursingPlanId() != null)
        {
            NursingPlan plan = nursingPlanMapper.selectNursingPlanById(target.getNursingPlanId());
            if (plan != null)
            {
                target.setNursingPlanName(plan.getPlanName());
            }
        }
    }
}