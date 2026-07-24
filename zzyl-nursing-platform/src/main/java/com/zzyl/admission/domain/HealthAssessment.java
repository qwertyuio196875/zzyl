package com.zzyl.admission.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzyl.common.annotation.Excel;
import com.zzyl.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 健康评估对象 health_assessment
 */
public class HealthAssessment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "评估编号")
    private String assessmentNo;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "评估日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date assessmentDate;

    @Excel(name = "评估人")
    private String assessor;

    @Excel(name = "健康等级", readConverterExp = "1=自理,2=半自理,3=不能自理,4=特护")
    private String healthLevel;

    @Excel(name = "认知等级", readConverterExp = "1=正常,2=轻度,3=中度,4=重度")
    private String cognitiveLevel;

    @Excel(name = "行动能力", readConverterExp = "1=正常,2=辅助,3=轮椅,4=卧床")
    private String mobilityLevel;

    @Excel(name = "自理能力", readConverterExp = "1=完全,2=部分,3=不能")
    private String selfCareLevel;

    @Excel(name = "既往病史")
    private String medicalHistory;

    @Excel(name = "过敏史")
    private String allergyHistory;

    @Excel(name = "评估结论")
    private String assessmentConclusion;

    @Excel(name = "状态", readConverterExp = "0=待评估,1=已完成")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssessmentNo() { return assessmentNo; }
    public void setAssessmentNo(String assessmentNo) { this.assessmentNo = assessmentNo; }
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
    public Date getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(Date assessmentDate) { this.assessmentDate = assessmentDate; }
    public String getAssessor() { return assessor; }
    public void setAssessor(String assessor) { this.assessor = assessor; }
    public String getHealthLevel() { return healthLevel; }
    public void setHealthLevel(String healthLevel) { this.healthLevel = healthLevel; }
    public String getCognitiveLevel() { return cognitiveLevel; }
    public void setCognitiveLevel(String cognitiveLevel) { this.cognitiveLevel = cognitiveLevel; }
    public String getMobilityLevel() { return mobilityLevel; }
    public void setMobilityLevel(String mobilityLevel) { this.mobilityLevel = mobilityLevel; }
    public String getSelfCareLevel() { return selfCareLevel; }
    public void setSelfCareLevel(String selfCareLevel) { this.selfCareLevel = selfCareLevel; }
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public String getAllergyHistory() { return allergyHistory; }
    public void setAllergyHistory(String allergyHistory) { this.allergyHistory = allergyHistory; }
    public String getAssessmentConclusion() { return assessmentConclusion; }
    public void setAssessmentConclusion(String assessmentConclusion) { this.assessmentConclusion = assessmentConclusion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("assessmentNo", getAssessmentNo())
            .append("elderName", getElderName())
            .append("status", getStatus())
            .toString();
    }
}
