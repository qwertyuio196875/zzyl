package com.zzyl.nursing.domain;



import java.util.List;

public class NursingPlanDto {


    private Long id;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 护理计划关联项目列表
     */
    List<NursingProjectPlan> projectPlans;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<NursingProjectPlan> getProjectPlans() {
        return projectPlans;
    }

    public void setProjectPlans(List<NursingProjectPlan> projectPlans) {
        this.projectPlans = projectPlans;
    }
}