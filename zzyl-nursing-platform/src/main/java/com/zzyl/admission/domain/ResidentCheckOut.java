package com.zzyl.admission.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 退住管理对象 resident_check_out
 */
public class ResidentCheckOut extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "退住编号")
    private String checkOutNo;

    private Long checkInId;

    @Excel(name = "老人姓名")
    private String elderName;

    @Excel(name = "身份证号")
    private String idCard;

    @Excel(name = "床位号")
    private String bedNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计退住日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expectedCheckOutDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际退住日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualCheckOutDate;

    @Excel(name = "退住原因")
    private String reason;

    @Excel(name = "退住说明")
    private String reasonDetail;

    @Excel(name = "状态", readConverterExp = "0=待审核,1=审核通过,2=审核驳回,3=已退住")
    private String status;

    @Excel(name = "审批人")
    private String approver;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    @Excel(name = "审批意见")
    private String approveRemark;

    @Excel(name = "结算金额")
    private BigDecimal settlementAmount;

    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCheckOutNo() { return checkOutNo; }
    public void setCheckOutNo(String checkOutNo) { this.checkOutNo = checkOutNo; }
    public Long getCheckInId() { return checkInId; }
    public void setCheckInId(Long checkInId) { this.checkInId = checkInId; }
    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    public Date getApplyDate() { return applyDate; }
    public void setApplyDate(Date applyDate) { this.applyDate = applyDate; }
    public Date getExpectedCheckOutDate() { return expectedCheckOutDate; }
    public void setExpectedCheckOutDate(Date expectedCheckOutDate) { this.expectedCheckOutDate = expectedCheckOutDate; }
    public Date getActualCheckOutDate() { return actualCheckOutDate; }
    public void setActualCheckOutDate(Date actualCheckOutDate) { this.actualCheckOutDate = actualCheckOutDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReasonDetail() { return reasonDetail; }
    public void setReasonDetail(String reasonDetail) { this.reasonDetail = reasonDetail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getApproveRemark() { return approveRemark; }
    public void setApproveRemark(String approveRemark) { this.approveRemark = approveRemark; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("checkOutNo", getCheckOutNo())
            .append("elderName", getElderName())
            .append("status", getStatus())
            .toString();
    }
}
