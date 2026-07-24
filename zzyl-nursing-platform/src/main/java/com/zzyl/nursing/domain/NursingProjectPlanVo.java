package com.zzyl.nursing.domain;

public class NursingProjectPlanVo {

    /** 项目id */
    private String projectId;

    /** $column.columnComment */
    private Long id;

    /** 计划id */
    private Long planId;

    /** 计划执行时间 */
    private String executeTime;

    /** 执行周期 0 天 1 周 2月 */
    private String executeCycle;

    /** 执行频次 */
    private Long executeFrequency;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public String getExecuteCycle() {
        return executeCycle;
    }

    public void setExecuteCycle(String executeCycle) {
        this.executeCycle = executeCycle;
    }

    public Long getExecuteFrequency() {
        return executeFrequency;
    }

    public void setExecuteFrequency(Long executeFrequency) {
        this.executeFrequency = executeFrequency;
    }
}