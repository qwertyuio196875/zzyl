-- =====================================================================
-- 系统表字段类型精简 (P1-6 子项 A)
-- 适用: zzyl-springboot
-- 日期: 2026-07-24
-- 关联文档: DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-6 / §4.2
--
-- 范围 (6 个字段 DDL, 零 Java 改动):
--   1) sys_logininfor.ipaddr       varchar(128) -> varchar(45)
--   2) sys_oper_log.oper_url       varchar(255) -> varchar(512)
--   3) sys_oper_log.oper_param     varchar(2000) -> TEXT
--   4) sys_oper_log.json_result    varchar(2000) -> TEXT
--   5) sys_oper_log.error_msg      varchar(2000) -> TEXT
--   6) sys_notice.notice_content   longblob -> longtext
--
-- 不在本次范围 (单独后续 PR):
--   - 业务表 char(1) -> tinyint (涉及 30+ 字段的 Entity/Service/前端改造)
--   - 拆表 sys_oper_log_param (P3 级)
--
-- 字段类型选择说明:
--   - varchar(45): IPv6 最长 45 字符, 完全覆盖
--   - varchar(512): oper_url 扩展到 512, 适配长路径/带 query string
--   - TEXT (非 MEDIUMTEXT): 严格按文档 §4.2, 上限 65535 字节, 与 RuoYi 默认一致
--   - longtext: notice_content 是长文本, 上限 4GB, 远超 HTML 富文本场景
--
-- 执行约束:
--   1) 执行前 mysqldump 备份 3 张表
--   2) 抽样评估收缩/转换安全性 (见下方 SELECT)
--   3) 建议低峰窗口执行 (ALTER TABLE 会锁表, InnoDB online DDL 可缓解)
--   4) 验证 SHOW CREATE + 抽样 SELECT
-- =====================================================================

-- [0] 备份 (DBA 手工 mysqldump):
-- mysqldump -h <host> -u<user> -p<pass> zzyl \
--   sys_logininfor sys_oper_log sys_notice \
--   > system_tables_bak_20260724.sql

-- 抽样确认收缩/转换安全性 (执行 ALTER 前必跑) ----------------------------

-- ipaddr 收缩到 45: 需确认现存数据长度 <= 45 (IPv6 最长)
-- SELECT MAX(LENGTH(ipaddr)) AS max_len, COUNT(*) AS total
--   FROM sys_logininfor WHERE LENGTH(ipaddr) > 45;
-- 期望: max_len <= 45 且 total = 0

-- oper_url 扩展到 512: 只是放长, 必然成功
-- SELECT MAX(LENGTH(oper_url)) AS max_len FROM sys_oper_log;

-- 3 个 TEXT 上限校验: 需确认 oper_param / json_result / error_msg 均 <= 65535
-- SELECT MAX(LENGTH(oper_param))  AS max_len_oper_param  FROM sys_oper_log;
-- SELECT MAX(LENGTH(json_result)) AS max_len_json_result FROM sys_oper_log;
-- SELECT MAX(LENGTH(error_msg))   AS max_len_error_msg   FROM sys_oper_log;
-- 期望: 均 <= 65535; 若超过, 改用 MEDIUMTEXT

-- notice_content BLOB -> longtext: 需确认现存是字符流而非二进制
-- SELECT notice_id, LENGTH(notice_content), CHAR_LENGTH(notice_content)
--   FROM sys_notice LIMIT 5;
-- 观察: LENGTH (字节数) 与 CHAR_LENGTH (字符数) 比例, 二进制内容比例通常为 1:1
--      HTML 富文本 (UTF-8) 通常有中文字符, 字符数 < 字节数

-- [1] sys_logininfor.ipaddr 128 -> 45 ------------------------------------
ALTER TABLE sys_logininfor
    MODIFY ipaddr VARCHAR(45) DEFAULT '' COMMENT '登录IP地址';

-- [2] sys_oper_log.oper_url 255 -> 512 -----------------------------------
ALTER TABLE sys_oper_log
    MODIFY oper_url VARCHAR(512) DEFAULT '' COMMENT '请求URL';

-- [3] sys_oper_log.oper_param 2000 -> TEXT -------------------------------
ALTER TABLE sys_oper_log
    MODIFY oper_param TEXT COMMENT '请求参数';

-- [4] sys_oper_log.json_result 2000 -> TEXT ------------------------------
ALTER TABLE sys_oper_log
    MODIFY json_result TEXT COMMENT '返回参数';

-- [5] sys_oper_log.error_msg 2000 -> TEXT --------------------------------
ALTER TABLE sys_oper_log
    MODIFY error_msg TEXT COMMENT '错误消息';

-- [6] sys_notice.notice_content longblob -> longtext ---------------------
ALTER TABLE sys_notice
    MODIFY notice_content LONGTEXT COMMENT '公告内容';

-- 验证 -------------------------------------------------------------------

-- 表结构
-- SHOW CREATE TABLE sys_logininfor\G
-- SHOW CREATE TABLE sys_oper_log\G
-- SHOW CREATE TABLE sys_notice\G

-- 抽样回读 (应用零感知, 与改造前结果一致)
-- SELECT info_id, user_name, ipaddr FROM sys_logininfor ORDER BY info_id DESC LIMIT 10;
-- SELECT oper_id, oper_url FROM sys_oper_log ORDER BY oper_id DESC LIMIT 10;
-- SELECT oper_id, LEFT(oper_param, 100) AS oper_param_preview FROM sys_oper_log ORDER BY oper_id DESC LIMIT 5;
-- SELECT notice_id, LEFT(notice_content, 100) AS content_preview FROM sys_notice LIMIT 3;

-- 关键: 应用侧 (SysLogininfor / SysOperLog / SysNotice Entity) 字段仍是 String,
-- MyBatis 自动适配, 无需任何代码变更。