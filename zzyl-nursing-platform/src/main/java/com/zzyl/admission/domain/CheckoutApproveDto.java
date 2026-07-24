package com.zzyl.admission.domain;

/**
 * 退住审核参数
 */
public class CheckoutApproveDto
{
    private Long id;
    private Boolean approved;
    private String approveRemark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getApproveRemark() { return approveRemark; }
    public void setApproveRemark(String approveRemark) { this.approveRemark = approveRemark; }
}
