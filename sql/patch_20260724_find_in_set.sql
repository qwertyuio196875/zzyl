-- =====================================================================
-- 数据库优化补丁：find_in_set 全表扫描修复
-- 适用版本: zzyl-springboot (RuoYi 改版)
-- 修复日期: 2026-07-24
-- 关联文档: DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-1 / §4.3
-- 说明:
--   1) 应用代码已把 find_in_set(#{deptId}, ancestors) 改为
--      ancestors LIKE CONCAT('%,', #{deptId}, ',%')
--      并保留 dept_id = #{deptId} 的兜底分支。
--   2) ancestors 是 varchar(50) 之类的短串（前缀长度已能覆盖典型路径），
--      仅添加"前缀索引"即可让 LIKE '%,x,%' 命中索引。
--   3) 备份当前索引后再执行；执行前请确认 sys_dept 表数据量与字符集。
-- =====================================================================

-- 1. 备份原表结构（仅 DDL，运维可手工 mysqldump）
--    mysqldump -h <host> -u <user> -p<pass> zzyl sys_dept > sys_dept_bak_20260724.sql

-- 2. 新增 ancestors 前缀索引（按当前 ancestors 平均长度选前缀，
--    通常 50 即可覆盖；若祖先链很长，可评估加到 100 后 EXPLAIN 验证）
ALTER TABLE sys_dept
    ADD INDEX idx_sys_dept_ancestors_prefix (ancestors(50));

-- 3. （可选）补 dept_id 上业务侧常见排序键，避免回表
--    ALTER TABLE sys_dept ADD INDEX idx_sys_dept_parent_id (parent_id);

-- 4. 验证（执行后人工确认 EXPLAIN 不再 type=ALL）
--    EXPLAIN
--    SELECT dept_id FROM sys_dept
--     WHERE ancestors LIKE CONCAT('%,', 100, ',%')
--        OR dept_id = 100;
--
--    期望：type=range 或 ref，key=idx_sys_dept_ancestors_prefix，rows 显著下降。

-- 5. 旧 find_in_set 写法确认无遗漏（如 grep 已无命中，可忽略）
--    SELECT * FROM performance_schema.events_statements_summary_by_digest
--     WHERE digest_text LIKE '%find_in_set%ancestors%';
--    若返回为空，说明运行期已无此 SQL 模式。
