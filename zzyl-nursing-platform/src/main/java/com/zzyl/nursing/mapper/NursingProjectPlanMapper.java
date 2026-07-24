package com.zzyl.nursing.mapper;

import com.zzyl.nursing.domain.NursingProjectPlan;
import com.zzyl.nursing.domain.NursingProjectPlanVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 护理计划和项目关联Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-23
 */
@Mapper
public interface NursingProjectPlanMapper 
{
    /**
     * 查询护理计划和项目关联
     * 
     * @param id 护理计划和项目关联主键
     * @return 护理计划和项目关联
     */
    public NursingProjectPlan selectNursingProjectPlanById(Long id);

    /**
     * 查询护理计划和项目关联列表
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 护理计划和项目关联集合
     */
    public List<NursingProjectPlan> selectNursingProjectPlanList(NursingProjectPlan nursingProjectPlan);

    /**
     * 新增护理计划和项目关联
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 结果
     */
    public int insertNursingProjectPlan(NursingProjectPlan nursingProjectPlan);

    /**
     * 修改护理计划和项目关联
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 结果
     */
    public int updateNursingProjectPlan(NursingProjectPlan nursingProjectPlan);

    /**
     * 删除护理计划和项目关联
     * 
     * @param id 护理计划和项目关联主键
     * @return 结果
     */
    public int deleteNursingProjectPlanById(Long id);

    /**
     * 批量删除护理计划和项目关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNursingProjectPlanByIds(Long[] ids);

    /**
     * 批量添加护理计划护理项目关联
     *
     */
    int batchInsert(@Param("list") List<NursingProjectPlan> projectPlans, @Param("planId") Long planId);

    /**
     * 根据计划id查询项目计划
     * @param id
     * @return
     */
    List<NursingProjectPlanVo> selectByPlanId(Long id);

    /**
     * 根据计划删除项目计划
     * @param planid
     * @return
     */
    @Delete("delete from nursing_project_plan where plan_id = #{planId}")
    void deleteByPlanId(Long planId);

}
