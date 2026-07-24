package com.zzyl.admission.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zzyl.admission.domain.CheckoutApproveDto;
import com.zzyl.admission.domain.CheckoutCompleteDto;
import com.zzyl.admission.domain.ResidentCheckIn;
import com.zzyl.admission.domain.ResidentCheckOut;
import com.zzyl.admission.mapper.ResidentCheckInMapper;
import com.zzyl.admission.mapper.ResidentCheckOutMapper;
import com.zzyl.admission.service.IResidentCheckOutService;
import com.zzyl.admission.util.AdmissionNoUtils;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.StringUtils;

@Service
public class ResidentCheckOutServiceImpl implements IResidentCheckOutService
{
    @Autowired
    private ResidentCheckOutMapper residentCheckOutMapper;

    @Autowired
    private ResidentCheckInMapper residentCheckInMapper;

    @Override
    public ResidentCheckOut selectResidentCheckOutById(Long id)
    {
        return residentCheckOutMapper.selectResidentCheckOutById(id);
    }

    @Override
    public List<ResidentCheckOut> selectResidentCheckOutList(ResidentCheckOut residentCheckOut)
    {
        return residentCheckOutMapper.selectResidentCheckOutList(residentCheckOut);
    }

    @Override
    public int insertResidentCheckOut(ResidentCheckOut residentCheckOut)
    {
        if (residentCheckOut.getCheckInId() == null)
        {
            throw new ServiceException("请选择入住记录");
        }
        ResidentCheckIn checkIn = residentCheckInMapper.selectResidentCheckInById(residentCheckOut.getCheckInId());
        if (checkIn == null)
        {
            throw new ServiceException("入住记录不存在");
        }
        if (!"2".equals(checkIn.getStatus()))
        {
            throw new ServiceException("仅已入住老人可退住申请");
        }
        ResidentCheckOut query = new ResidentCheckOut();
        query.setCheckInId(residentCheckOut.getCheckInId());
        List<ResidentCheckOut> pending = residentCheckOutMapper.selectResidentCheckOutList(query);
        for (ResidentCheckOut item : pending)
        {
            if ("0".equals(item.getStatus()) || "1".equals(item.getStatus()))
            {
                throw new ServiceException("已有进行中或审核通过的退住申请");
            }
        }
        residentCheckOut.setCheckOutNo(AdmissionNoUtils.checkOutNo());
        residentCheckOut.setElderName(checkIn.getElderName());
        residentCheckOut.setIdCard(checkIn.getIdCard());
        residentCheckOut.setBedNo(checkIn.getBedNo());
        if (StringUtils.isEmpty(residentCheckOut.getStatus()))
        {
            residentCheckOut.setStatus("0");
        }
        if (residentCheckOut.getApplyDate() == null)
        {
            residentCheckOut.setApplyDate(DateUtils.getNowDate());
        }
        residentCheckOut.setCreateTime(DateUtils.getNowDate());
        return residentCheckOutMapper.insertResidentCheckOut(residentCheckOut);
    }

    @Override
    public int updateResidentCheckOut(ResidentCheckOut residentCheckOut)
    {
        ResidentCheckOut existing = residentCheckOutMapper.selectResidentCheckOutById(residentCheckOut.getId());
        if (existing == null)
        {
            throw new ServiceException("退住申请不存在");
        }
        if (!"0".equals(existing.getStatus()) && !"2".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改");
        }
        residentCheckOut.setUpdateTime(DateUtils.getNowDate());
        return residentCheckOutMapper.updateResidentCheckOut(residentCheckOut);
    }

    @Override
    public int deleteResidentCheckOutByIds(Long[] ids)
    {
        return residentCheckOutMapper.deleteResidentCheckOutByIds(ids);
    }

    @Override
    public int approveCheckout(CheckoutApproveDto dto)
    {
        if (dto.getId() == null || dto.getApproved() == null)
        {
            throw new ServiceException("审核参数不完整");
        }
        ResidentCheckOut existing = residentCheckOutMapper.selectResidentCheckOutById(dto.getId());
        if (existing == null)
        {
            throw new ServiceException("退住申请不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("仅待审核的申请可审批");
        }
        ResidentCheckOut update = new ResidentCheckOut();
        update.setId(dto.getId());
        update.setStatus(Boolean.TRUE.equals(dto.getApproved()) ? "1" : "2");
        update.setApprover(SecurityUtils.getUsername());
        update.setApproveTime(DateUtils.getNowDate());
        update.setApproveRemark(dto.getApproveRemark());
        update.setUpdateTime(DateUtils.getNowDate());
        return residentCheckOutMapper.updateResidentCheckOut(update);
    }

    @Override
    @Transactional
    public int completeCheckout(CheckoutCompleteDto dto)
    {
        if (dto.getId() == null)
        {
            throw new ServiceException("退住申请ID不能为空");
        }
        ResidentCheckOut existing = residentCheckOutMapper.selectResidentCheckOutById(dto.getId());
        if (existing == null)
        {
            throw new ServiceException("退住申请不存在");
        }
        if (!"1".equals(existing.getStatus()))
        {
            throw new ServiceException("仅审核通过的申请可确认退住");
        }
        ResidentCheckOut update = new ResidentCheckOut();
        update.setId(dto.getId());
        update.setStatus("3");
        update.setActualCheckOutDate(dto.getActualCheckOutDate() != null ? dto.getActualCheckOutDate() : DateUtils.getNowDate());
        update.setSettlementAmount(dto.getSettlementAmount());
        update.setRefundAmount(dto.getRefundAmount());
        if (StringUtils.isNotEmpty(dto.getRemark()))
        {
            update.setRemark(dto.getRemark());
        }
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = residentCheckOutMapper.updateResidentCheckOut(update);

        ResidentCheckIn checkInUpdate = new ResidentCheckIn();
        checkInUpdate.setId(existing.getCheckInId());
        checkInUpdate.setStatus("4");
        checkInUpdate.setUpdateTime(DateUtils.getNowDate());
        residentCheckInMapper.updateResidentCheckIn(checkInUpdate);
        return rows;
    }
}
