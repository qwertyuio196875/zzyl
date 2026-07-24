package com.zzyl.admission.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 退住确认参数
 */
public class CheckoutCompleteDto
{
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualCheckOutDate;

    private BigDecimal settlementAmount;
    private BigDecimal refundAmount;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getActualCheckOutDate() { return actualCheckOutDate; }
    public void setActualCheckOutDate(Date actualCheckOutDate) { this.actualCheckOutDate = actualCheckOutDate; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
