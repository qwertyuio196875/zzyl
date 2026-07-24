package com.zzyl.nursing.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;

/**
 * 来访记录对象 visit_record
 * 
 * @author ruoyi
 */
public class VisitRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 访客姓名 */
    @Excel(name = "访客姓名")
    private String visitorName;

    /** 访客电话 */
    @Excel(name = "访客电话")
    private String visitorPhone;

    /** 访客身份证 */
    @Excel(name = "访客身份证")
    private String visitorIdCard;

    /** 被访人姓名 */
    @Excel(name = "被访人姓名")
    private String visitedName;

    /** 被访人部门 */
    @Excel(name = "被访人部门")
    private String visitedDept;

    /** 来访事由 */
    @Excel(name = "来访事由")
    private String visitReason;

    /** 预约来访日期 */
    @Excel(name = "预约来访日期")
    private String visitDate;

    /** 预约来访时间 */
    @Excel(name = "预约来访时间")
    private String visitTime;

    /** 实际来访时间 */
    @Excel(name = "实际来访时间")
    private Date actualVisitTime;

    /** 离开时间 */
    @Excel(name = "离开时间")
    private Date leaveTime;

    /** 来访状态（0:待审核, 1:已预约, 2:已签到, 3:已离开, 4:已取消） */
    @Excel(name = "来访状态", readConverterExp = "0=待审核,1=已预约,2=已签到,3=已离开,4=已取消")
    private Long status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setVisitorName(String visitorName) 
    {
        this.visitorName = visitorName;
    }

    public String getVisitorName() 
    {
        return visitorName;
    }

    public void setVisitorPhone(String visitorPhone) 
    {
        this.visitorPhone = visitorPhone;
    }

    public String getVisitorPhone() 
    {
        return visitorPhone;
    }

    public void setVisitorIdCard(String visitorIdCard) 
    {
        this.visitorIdCard = visitorIdCard;
    }

    public String getVisitorIdCard() 
    {
        return visitorIdCard;
    }

    public void setVisitedName(String visitedName) 
    {
        this.visitedName = visitedName;
    }

    public String getVisitedName() 
    {
        return visitedName;
    }

    public void setVisitedDept(String visitedDept) 
    {
        this.visitedDept = visitedDept;
    }

    public String getVisitedDept() 
    {
        return visitedDept;
    }

    public void setVisitReason(String visitReason) 
    {
        this.visitReason = visitReason;
    }

    public String getVisitReason() 
    {
        return visitReason;
    }

    public void setVisitDate(String visitDate) 
    {
        this.visitDate = visitDate;
    }

    public String getVisitDate() 
    {
        return visitDate;
    }

    public void setVisitTime(String visitTime) 
    {
        this.visitTime = visitTime;
    }

    public String getVisitTime() 
    {
        return visitTime;
    }

    public void setActualVisitTime(Date actualVisitTime) 
    {
        this.actualVisitTime = actualVisitTime;
    }

    public Date getActualVisitTime() 
    {
        return actualVisitTime;
    }

    public void setLeaveTime(Date leaveTime) 
    {
        this.leaveTime = leaveTime;
    }

    public Date getLeaveTime() 
    {
        return leaveTime;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("visitorName", getVisitorName())
            .append("visitorPhone", getVisitorPhone())
            .append("visitorIdCard", getVisitorIdCard())
            .append("visitedName", getVisitedName())
            .append("visitedDept", getVisitedDept())
            .append("visitReason", getVisitReason())
            .append("visitDate", getVisitDate())
            .append("visitTime", getVisitTime())
            .append("actualVisitTime", getActualVisitTime())
            .append("leaveTime", getLeaveTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
