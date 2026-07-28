-- mp_demo_user 表：MyBatis-Plus 阶段 ② 极简验证专用
-- 与 RuoYi 业务表完全隔离，验证后可手动 DROP TABLE 删除
DROP TABLE IF EXISTS mp_demo_user;
CREATE TABLE mp_demo_user (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)  NOT NULL                COMMENT '姓名',
    age         INT                                   COMMENT '年龄',
    email       VARCHAR(128)                          COMMENT '邮箱',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MyBatis-Plus 阶段 ② 验证表';
