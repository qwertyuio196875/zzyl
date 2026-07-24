-- =====================================================================
-- visit_record 字段类型修正：varchar(20) → DATE / TIME
-- 应用: zzyl-springboot
-- 日期: 2026-07-24
-- 关联文档: DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-3 / §4.2
-- 决策: 仅改类型、加索引；不加 NOT NULL（业务现实存在 NULL/空串）。
-- 配套改动: zzyl-framework 新增 StringToDateTypeHandler /
--           StringToTimeTypeHandler,mybatis-config.xml 注册,
--           VisitRecordMapper.xml 列上加 jdbcType,DATE/TIME。
--           Java 端 VisitRecord.visitDate/visitTime 仍为 String。
-- 执行约束:
--   1) 执行前 mysqldump 备份 visit_record
--   2) 先抽样评估脏数据规模(空串/非 yyyy-MM-dd/非 HH:mm:ss)
--   3) 建议低峰或停机窗口执行(索引会锁表)
--   4) 验证 SHOW CREATE TABLE / EXPLAIN / 抽样 SELECT
-- =====================================================================

-- [0/3] 备份 (DBA 手工 mysqldump):
-- mysqldump -h <host> -u<user> -p<pass> zzyl visit_record > visit_record_bak_20260724.sql

-- 抽样：先看脏数据规模，决定能否直接 MODIFY
-- SELECT visit_date, COUNT(*) FROM visit_record GROUP BY visit_date ORDER BY 2 DESC LIMIT 20;
-- SELECT visit_time, COUNT(*) FROM visit_record GROUP BY visit_time ORDER BY 2 DESC LIMIT 20;

-- [1/3] 数据清洗：空串归 NULL；非标格式需 DBA 人工决策（保留 / 修改 / 清理）
--   为安全起见，UPDATE 影响行数 > 0 时请记录 row_count 备用。
UPDATE visit_record SET visit_date = NULL WHERE visit_date = '' OR visit_date IS NULL;
UPDATE visit_record SET visit_time = NULL WHERE visit_time = '' OR visit_time IS NULL;

-- [2/3] 类型转换：保留 default null（业务上"待审核"等状态可能没填时间）
ALTER TABLE visit_record
    MODIFY visit_date DATE DEFAULT NULL COMMENT '预约来访日期',
    MODIFY visit_time TIME DEFAULT NULL COMMENT '预约来访时间';

-- [3/3] 索引补全（P1-4 本表部分，参见 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-4 / §4.1）
CREATE INDEX idx_visit_date    ON visit_record (visit_date);
CREATE INDEX idx_visitor_phone ON visit_record (visitor_phone);
CREATE INDEX idx_status        ON visit_record (status);

-- 验证 -----------------------------------------------------------------------------

-- 表结构
-- SHOW CREATE TABLE visit_record\G

-- 索引
-- SHOW INDEX FROM visit_record WHERE Key_name LIKE 'idx_%';

-- EXPLAIN：日期范围查询走索引
-- EXPLAIN
-- SELECT * FROM visit_record
--  WHERE visit_date BETWEEN '2026-07-01' AND '2026-07-31'
--  ORDER BY visit_date DESC, visit_time DESC;
-- 期望: type=range 或 ref, key=idx_visit_date, rows 显著下降

-- 抽样回读：日期列已为规范 ISO 格式
-- SELECT id, visit_date, visit_time FROM visit_record LIMIT 10;

-- 业务对照：与改造前结果对比 DATE 列排序与字典序排序等价
-- SELECT id, visit_date, visit_time FROM visit_record ORDER BY visit_date DESC, visit_time DESC LIMIT 20;
