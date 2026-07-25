-- =====================================================================
-- 业务表索引补全 (P1-4)
-- 适用: zzyl-springboot
-- 日期: 2026-07-24
-- 关联文档: DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-4 / §4.1
--
-- 范围 (11 个索引):
--   Batch A: nursing_* 小表, 4 索引, 立即执行
--     - nursing_project_plan.plan_id  / project_id
--     - nursing_project.status
--     - nursing_level.plan_id
--   Batch B: admission 大表, 7 索引, 低峰或 pt-osc 执行
--     - health_assessment.elder_name / assessment_date
--     - resident_check_in.elder_name / nursing_level_id / status
--     - resident_check_out.elder_name / status
--
-- 命名规范: idx_<table>_<column>
-- 不动: 老索引 (uk_*/idx_assessment_id/idx_check_in_id), 避免破坏现有命名一致性
-- 不动: nursing_plan (文档未列, 当前无 WHERE 过滤场景)
--
-- 执行约束:
--   1) mysqldump 备份 7 张业务表
--   2) Batch A 立即; Batch B 建议低峰或 pt-online-schema-change / gh-ost
--   3) InnoDB online DDL 可缓解锁表, 但大表仍可能短时阻塞写入
--   4) 执行后人工 SHOW INDEX + EXPLAIN 验证
-- =====================================================================

-- [0] 备份 (DBA 手工 mysqldump):
-- mysqldump -h <host> -u<user> -p<pass> zzyl \
--   nursing_level nursing_project nursing_project_plan \
--   health_assessment resident_check_in resident_check_out \
--   > business_tables_bak_20260724.sql

-- 抽样先看现有索引
-- SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX
--   FROM INFORMATION_SCHEMA.STATISTICS
--  WHERE TABLE_SCHEMA = 'zzyl'
--    AND TABLE_NAME IN (
--      'health_assessment','resident_check_in','resident_check_out',
--      'nursing_level','nursing_project','nursing_project_plan'
--    )
--  ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- =====================================================================
-- [Batch A] nursing_* 小表 (立即执行, 秒级完成)
-- =====================================================================

-- nursing_project_plan: 关联表, plan_id / project_id 都被 WHERE 过滤
-- 已应用: NursingProjectPlanMapper.xml:28-29 selectByPlanId/ProjectId
-- 已应用: NursingProjectPlanMapper.java:87   deleteByPlanId
CREATE INDEX idx_nursing_project_plan_plan_id    ON nursing_project_plan (plan_id);
CREATE INDEX idx_nursing_project_plan_project_id ON nursing_project_plan (project_id);

-- nursing_project: status 字段 (0禁用/1启用) 常用于列表筛选
CREATE INDEX idx_nursing_project_status          ON nursing_project (status);

-- nursing_level: plan_id 字段 (外键) 用于按计划查等级
CREATE INDEX idx_nursing_level_plan_id           ON nursing_level (plan_id);

-- =====================================================================
-- [Batch B] admission 大表 (建议低峰或 pt-osc)
-- =====================================================================

-- health_assessment
--   elder_name     全模糊 LIKE 当前业务用不到索引, 加索引仅为后续改造预留
--   assessment_date 日期范围筛选可直接命中
CREATE INDEX idx_health_assessment_elder_name        ON health_assessment (elder_name);
CREATE INDEX idx_health_assessment_assessment_date   ON health_assessment (assessment_date);

-- resident_check_in
--   elder_name        同上, 业务当前为全模糊, 索引预留
--   nursing_level_id  外键关联 nursing_level, 频繁关联查询
--   status            列表筛选 (0待办理/1办理中/2已入住/3已取消/4已退住)
CREATE INDEX idx_resident_check_in_elder_name        ON resident_check_in (elder_name);
CREATE INDEX idx_resident_check_in_nursing_level_id  ON resident_check_in (nursing_level_id);
CREATE INDEX idx_resident_check_in_status            ON resident_check_in (status);

-- resident_check_out
--   elder_name        同上, 索引预留
--   status            列表筛选 (0待审核/1审核通过/2审核驳回/3已退住)
CREATE INDEX idx_resident_check_out_elder_name       ON resident_check_out (elder_name);
CREATE INDEX idx_resident_check_out_status           ON resident_check_out (status);

-- =====================================================================
-- [验证] 执行后人工跑 (与脚本分开, 避免 DDL 失败连带)
-- =====================================================================

-- 表结构与索引
-- SHOW INDEX FROM nursing_project_plan;
-- SHOW INDEX FROM nursing_project;
-- SHOW INDEX FROM nursing_level;
-- SHOW INDEX FROM health_assessment;
-- SHOW INDEX FROM resident_check_in;
-- SHOW INDEX FROM resident_check_out;

-- EXPLAIN 精确匹配场景
-- EXPLAIN SELECT * FROM nursing_project_plan WHERE plan_id = 100;
--   期望: type=ref, key=idx_nursing_project_plan_plan_id, rows 显著下降
--
-- EXPLAIN SELECT * FROM health_assessment
--   WHERE assessment_date BETWEEN '2026-07-01' AND '2026-07-31';
--   期望: type=range, key=idx_health_assessment_assessment_date
--
-- EXPLAIN SELECT * FROM resident_check_in WHERE status = '2';
--   期望: type=ref, key=idx_resident_check_in_status

-- EXPLAIN 模糊匹配场景 (当前业务现状)
-- EXPLAIN SELECT * FROM resident_check_in WHERE elder_name LIKE '%张三%';
--   现实: type=ALL, 因为左模糊不走 B-tree; 索引已建好等业务侧改造