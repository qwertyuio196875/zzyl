package com.zzyl.nursing.domain;


import java.util.List;

// 护理计划
public class NursingPlanVo {

    /**
     * 护理计划id
     */
    private Long id;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 护理计划名称
     */
    private String planName;

    /**
     * 状态 0禁用 1启用
     */
    private Integer status;

    /**
     * 护理计划项目列表
     */
    List<NursingProjectPlanVo> projectPlans;

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

    public List<NursingProjectPlanVo> getProjectPlans() {
        return projectPlans;
    }

    public void setProjectPlans(List<NursingProjectPlanVo> projectPlans) {
        this.projectPlans = projectPlans;
    }
}