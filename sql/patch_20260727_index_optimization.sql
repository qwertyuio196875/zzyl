-- =====================================================================
-- 索引设计与 SQL 模式优化补丁
-- 适用: zzyl-springboot
-- 日期: 2026-07-27
-- 关联文档: DATABASE_OPTIMIZATION_RECOMMENDATIONS.md
-- 前置条件: 已执行 patch_20260724_business_index_audit.sql（基础单列索引）
-- 范围: 复合索引 / 系统表索引 / update_time 索引 / 字段类型精简
-- =====================================================================

-- [0] 备份建议（DBA 手工执行，DBA 自行 mysqldump）

-- =====================================================================
-- [1] 复合索引（在单列基础上增强）
-- =====================================================================

-- nursing_project_plan：业务高频 where plan_id=? order by project_id
-- 单列索引 (plan_id) / (project_id) 已存在；新增联合索引覆盖"按计划查项目排序"场景
CREATE INDEX idx_npp_plan_project ON nursing_project_plan(plan_id, project_id);

-- resident_check_in："在住老人"列表(status='2')按入住日期倒序
CREATE INDEX idx_rci_status_checkin_date ON resident_check_in(status, check_in_date);

-- health_assessment：日期+状态组合筛选
CREATE INDEX idx_health_assessment_date_status ON health_assessment(assessment_date, status);

-- nursing_project：启用项目按 order_no 排序展示
CREATE INDEX idx_nursing_project_status_order ON nursing_project(status, order_no);

-- =====================================================================
-- [2] 系统表高频索引
-- =====================================================================

-- sys_user：手机号/邮箱在用户管理/导入去重场景用，现状无唯一约束
CREATE UNIQUE INDEX uk_sys_user_phonenumber ON sys_user(phonenumber);
CREATE UNIQUE INDEX uk_sys_user_email       ON sys_user(email);

-- sys_role：role_key 必须全局唯一（角色权限字符串）
CREATE UNIQUE INDEX uk_sys_role_role_key ON sys_role(role_key);

-- sys_config：config_key 字典式 key 必须唯一
CREATE UNIQUE INDEX uk_sys_config_key ON sys_config(config_key);

-- sys_dict_data：按 dict_type 频繁关联 sys_dict_type；与 status 组合覆盖"启用字典"场景
CREATE INDEX idx_sys_dict_data_type_status ON sys_dict_data(dict_type, status);

-- sys_menu / sys_dept：parent_id 用于树查询（前端 TreeUtil）
CREATE INDEX idx_sys_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX idx_sys_dept_parent_id ON sys_dept(parent_id);

-- sys_oper_log：监控页按操作时间排序、按操作人筛选
CREATE INDEX idx_sys_oper_log_ot_name ON sys_oper_log(oper_time, oper_name);

-- sys_logininfor：登录日志按时间排序、按用户筛选
CREATE INDEX idx_sys_logininfor_lt_name ON sys_logininfor(login_time, user_name);

-- sys_notice：按创建时间倒序列表
CREATE INDEX idx_sys_notice_create_time ON sys_notice(create_time);

-- =====================================================================
-- [3] 业务表 update_time 索引（审计/列表按修改时间排序/范围查询）
-- =====================================================================

CREATE INDEX idx_resident_check_in_update_time  ON resident_check_in(update_time);
CREATE INDEX idx_resident_check_out_update_time ON resident_check_out(update_time);
CREATE INDEX idx_health_assessment_update_time  ON health_assessment(update_time);
CREATE INDEX idx_visit_record_update_time       ON visit_record(update_time);
CREATE INDEX idx_nursing_plan_update_time       ON nursing_plan(update_time);
CREATE INDEX idx_nursing_project_update_time    ON nursing_project(update_time);
CREATE INDEX idx_nursing_level_update_time      ON nursing_level(update_time);
CREATE INDEX idx_nursing_project_plan_update_time ON nursing_project_plan(update_time);

-- =====================================================================
-- [4] 缺失的状态/单列索引
-- =====================================================================

-- nursing_plan：状态筛选（patch_20260724 漏列）
CREATE INDEX idx_nursing_plan_status ON nursing_plan(status);

-- =====================================================================
-- [5] 字段类型精简
-- =====================================================================

-- nursing_plan.status：bigint(20) → tinyint(1)（0/1 状态用 1 字节足矣，节省 7 字节/行）
ALTER TABLE nursing_plan MODIFY status TINYINT(1) DEFAULT 1 COMMENT '状态（0禁用，1启用）';

-- =====================================================================
-- [6] 验证 SQL（执行后人工跑）
-- =====================================================================

-- 索引清单
-- SHOW INDEX FROM nursing_project_plan;
-- SHOW INDEX FROM resident_check_in;
-- SHOW INDEX FROM health_assessment;
-- SHOW INDEX FROM sys_user;
-- SHOW INDEX FROM sys_role;
-- SHOW INDEX FROM sys_config;

-- EXPLAIN 时间范围（验证新加 update_time/create_time 索引命中）
-- EXPLAIN SELECT * FROM resident_check_in
--  WHERE update_time >= '2026-07-01' AND update_time < '2026-07-28'
--  ORDER BY update_time DESC;
-- 期望: type=range, key=idx_resident_check_in_update_time

-- EXPLAIN 前缀 LIKE（验证 B-tree 命中）
-- EXPLAIN SELECT * FROM sys_user WHERE user_name LIKE 'admin%';
-- 期望: type=range, key=uk_sys_user_user_name

-- EXPLAIN 精确 unique 命中
-- EXPLAIN SELECT * FROM sys_user WHERE phonenumber = '13800138000';
-- 期望: type=const, key=uk_sys_user_phonenumber

-- =====================================================================
-- [7] 执行约束
-- =====================================================================
-- 1) mysqldump 备份所有 DDL 影响表
-- 2) 大表 (sys_user/sys_oper_log/sys_logininfor) 建议低峰或 pt-osc 执行
-- 3) ALTER TABLE ... MODIFY 在 MySQL 8.0+ 是 INSTANT，秒级完成
-- 4) InnoDB online DDL 可缓解锁表
