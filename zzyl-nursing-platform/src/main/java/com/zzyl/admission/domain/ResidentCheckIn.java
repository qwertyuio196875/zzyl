package com.zzyl.admission.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 入住办理对象 resident_check_in
 */
public class ResidentCheckIn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "入住编号")
    private String checkInNo;

    private Long assessmentId;

    @Excel(name = "老人姓名")
    private String elderName;

    @Excel(name = "身份证号")
    private String idCard;

    @Excel(name = "性别", readConverterExp = "0=男,1=女")
    private String gender;

    @Excel(name = "年龄")
    private Integer age;

    @Excel(name = "联系电话")
    private String phone;

    private Long nursingLevelId;

    @Excel(name = "护理等级")
    private String nursingLevelName;

    private Long nursingPlanId;

    @Excel(name = "护理计划")
    private String nursingPlanName;

    @Excel(name = "楼栋")
    private String buildingName;

    @Excel(name = "房间号")
    private String roomNo;

    @Excel(name = "床位号")
    private String bedNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入住日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractEndDate;

    @Excel(name = "紧急联系人")
    private String emergencyContact;

    @Excel(name = "紧急联系电话")
    private String emergencyPhone;

    @Excel(name = "与老人关系")
    private String relationship;

    @Excel(name = "押金")
    private BigDecimal deposit;

    @Excel(name = "月费用")
    private BigDecimal monthlyFee;

    @Excel(name = "办理人")
    private String handler;

    @Excel(name = "状态", readConverterExp = "0=待办理,1=办理中,2=已入住,3=已取消,4=已退住")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCheckInNo() { return checkInNo; }
    public void setCheckInNo(String checkInNo) { this.checkInNo = checkInNo; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getNursingLevelId() { return nursingLevelId; }
    public void setNursingLevelId(Long nursingLevelId) { this.nursingLevelId = nursingLevelId; }
    public String getNursingLevelName() { return nursingLevelName; }
    public void setNursingLevelName(String nursingLevelName) { this.nursingLevelName = nursingLevelName; }
    public Long getNursingPlanId() { return nursingPlanId; }
    public void setNursingPlanId(Long nursingPlanId) { this.nursingPlanId = nursingPlanId; }
    public String getNursingPlanName() { return nursingPlanName; }
    public void setNursingPlanName(String nursingPlanName) { this.nursingPlanName = nursingPlanName; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    public Date getCheckInDate() { return checkInDate; }
    public void setCheckInDate(Date checkInDate) { this.checkInDate = checkInDate; }
    public Date getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(Date contractStartDate) { this.contractStartDate = contractStartDate; }
    public Date getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(Date contractEndDate) { this.contractEndDate = contractEndDate; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("checkInNo", getCheckInNo())
            .append("elderName", getElderName())
            .append("status", getStatus())
            .toString();
    }
}
