package com.zzyl.nursing.service;

import java.util.List;
import com.zzyl.nursing.domain.VisitRecord;

/**
 * 来访记录Service接口
 * 
 * @author ruoyi
 */
public interface IVisitRecordService 
{
    /**
     * 查询来访记录
     * 
     * @param id 来访记录主键
     * @return 来访记录
     */
    public VisitRecord selectVisitRecordById(Long id);

    /**
     * 查询来访记录列表
     * 
     * @param visitRecord 来访记录
     * @return 来访记录集合
     */
    public List<VisitRecord> selectVisitRecordList(VisitRecord visitRecord);

    /**
     * 新增来访记录
     * 
     * @param visitRecord 来访记录
     * @return 结果
     */
    public int insertVisitRecord(VisitRecord visitRecord);

    /**
     * 修改来访记录
     * 
     * @param visitRecord 来访记录
     * @return 结果
     */
    public int updateVisitRecord(VisitRecord visitRecord);

    /**
     * 批量删除来访记录
     * 
     * @param ids 需要删除的来访记录主键集合
     * @return 结果
     */
    public int deleteVisitRecordByIds(Long[] ids);

    /**
     * 删除来访记录信息
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    public int deleteVisitRecordById(Long id);

    /**
     * 审核来访记录
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    public int approveVisitRecord(Long id);

    /**
     * 取消来访记录
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    public int cancelVisitRecord(Long id);

    /**
     * 签到来访记录
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    public int signInVisitRecord(Long id);

    /**
     * 离开登记
     * 
     * @param id 来访记录主键
     * @return 结果
     */
    public int leaveVisitRecord(Long id);
}
