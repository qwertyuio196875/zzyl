package com.zzyl.nursing.service.impl;

import java.util.Date;
import java.util.List;
import com.zzyl.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.VisitRecordMapper;
import com.zzyl.nursing.domain.VisitRecord;
import com.zzyl.nursing.service.IVisitRecordService;

/**
 * 来访记录Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class VisitRecordServiceImpl implements IVisitRecordService 
{

    @Autowired
    private VisitRecordMapper visitRecordMapper;

    /**
     * 查询来访记录
     * 
     * @param id 来访记录主键
     * @return 来访记录
     */
    @Override
    public VisitRecord selectVisitRecordById(Long id)
    {
        return visitRecordMapper.selectVisitRecordById(id);
    }

    /**
     * 查询来访记录列表
     * 
     * @param visitRecord 来访记录
     * @return 来访记录
     */
    @Override
    public List<VisitRecord> selectVisitRecordList(VisitRecord visitRecord)
    {
        return visitRecordMapper.selectVisitRecordList(visitRecord);
    }

    /**
     * 新增来访记录
     * 
     * @param visitRecord 来访记录
     * @return 结果
     */
    @Override
    public int insertVisitRecord(VisitRecord visitRecord)
    {
        visitRecord.setCreateTime(DateUtils.getNowDate());
        return visitRecordMapper.insertVisitRecord(visitRecord);
    }

    /**
     * 修改来访记录
     * 
     * @param visitRecord 来访记录
     * @return 结果
     */
    @Override
    public int updateVisitRecord(VisitRecord visitRecord)
    {
        visitRecord.setUpdateTime(DateUtils.getNowDate());
        return visitRecordMapper.updateVisitRecord(visitRecord);
    }

    /**
     * 批量删除来访记录
     * 
     * @param ids 需要删除的来访记录主键
     * @return 结果
     */
    @Override
    public int deleteVisitRecordByIds(Long[] ids)
    {
        return visitRecordMapper.deleteVisitRecordByIds(ids);
    }

    /**
     * 删除来访记录信息
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    @Override
    public int deleteVisitRecordById(Long id)
    {
        return visitRecordMapper.deleteVisitRecordById(id);
    }

    /**
     * 审核来访记录
     */
    @Override
    public int approveVisitRecord(Long id)
    {
        VisitRecord visitRecord = new VisitRecord();
        visitRecord.setId(id);
        visitRecord.setStatus(1L); // 已预约
        visitRecord.setUpdateTime(DateUtils.getNowDate());
        return visitRecordMapper.updateVisitRecord(visitRecord);
    }

    /**
     * 取消来访记录
     */
    @Override
    public int cancelVisitRecord(Long id)
    {
        VisitRecord visitRecord = new VisitRecord();
        visitRecord.setId(id);
        visitRecord.setStatus(4L); // 已取消
        visitRecord.setUpdateTime(DateUtils.getNowDate());
        return visitRecordMapper.updateVisitRecord(visitRecord);
    }

    /**
     * 签到来访记录
     */
    @Override
    public int signInVisitRecord(Long id)
    {
        VisitRecord visitRecord = new VisitRecord();
        visitRecord.setId(id);
        visitRecord.setStatus(2L); // 已签到
        visitRecord.setActualVisitTime(new Date());
        visitRecord.setUpdateTime(DateUtils.getNowDate());
        return visitRecordMapper.updateVisitRecord(visitRecord);
    }

    /**
     * 离开登记
     */
    @Override
    public int leaveVisitRecord(Long id)
    {
        VisitRecord visitRecord = new VisitRecord();
        visitRecord.setId(id);
        visitRecord.setStatus(3L); // 已离开
        visitRecord.setLeaveTime(new Date());
        visitRecord.setUpdateTime(DateUtils.getNowDate());
        return visitRecordMapper.updateVisitRecord(visitRecord);
    }
}
