-- ----------------------------
-- 1、护理等级表
-- ----------------------------
DROP TABLE IF EXISTS nursing_level;
CREATE TABLE nursing_level (
  id              bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
  name            varchar(100)    DEFAULT NULL                COMMENT '等级名称',
  plan_id         bigint(20)      DEFAULT NULL                COMMENT '护理计划ID',
  fee             decimal(10,2)   DEFAULT NULL                COMMENT '护理费用',
  status          int(2)          DEFAULT 1                   COMMENT '状态（0禁用，1启用）',
  description     varchar(500)    DEFAULT NULL                COMMENT '等级说明',
  remark          varchar(500)    DEFAULT NULL                COMMENT '备注',
  create_by       varchar(64)     DEFAULT ''                  COMMENT '创建者',
  create_time     datetime                                    COMMENT '创建时间',
  update_by       varchar(64)     DEFAULT ''                  COMMENT '更新者',
  update_time     datetime                                    COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='护理等级表';

-- ----------------------------
-- 2、护理计划表
-- ----------------------------
DROP TABLE IF EXISTS nursing_plan;
CREATE TABLE nursing_plan (
  id              bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '编号',
  sort_no         bigint(20)      DEFAULT NULL                COMMENT '排序号',
  plan_name       varchar(100)    DEFAULT NULL                COMMENT '名称',
  status          bigint(20)      DEFAULT 1                   COMMENT '状态（0禁用，1启用）',
  remark          varchar(500)    DEFAULT NULL                COMMENT '备注',
  create_by       varchar(64)     DEFAULT ''                  COMMENT '创建者',
  create_time     datetime                                    COMMENT '创建时间',
  update_by       varchar(64)     DEFAULT ''                  COMMENT '更新者',
  update_time     datetime                                    COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='护理计划表';

-- ----------------------------
-- 3、护理项目表
-- ----------------------------
DROP TABLE IF EXISTS nursing_project;
CREATE TABLE nursing_project (
  id                  bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '编号',
  name                varchar(100)    DEFAULT NULL                COMMENT '名称',
  order_no            bigint(20)      DEFAULT NULL                COMMENT '排序号',
  unit                varchar(50)     DEFAULT NULL                COMMENT '单位',
  price               decimal(10,2)   DEFAULT NULL                COMMENT '价格',
  image               varchar(255)    DEFAULT NULL                COMMENT '图片',
  nursing_requirement varchar(1000)   DEFAULT NULL                COMMENT '护理要求',
  status              int(2)          DEFAULT 1                   COMMENT '状态（0禁用，1启用）',
  remark              varchar(500)    DEFAULT NULL                COMMENT '备注',
  create_by           varchar(64)     DEFAULT ''                  COMMENT '创建者',
  create_time         datetime                                    COMMENT '创建时间',
  update_by           varchar(64)     DEFAULT ''                  COMMENT '更新者',
  update_time         datetime                                    COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='护理项目表';

-- ----------------------------
-- 4、护理计划与项目关联表
-- ----------------------------
DROP TABLE IF EXISTS nursing_project_plan;
CREATE TABLE nursing_project_plan (
  id              bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '编号',
  plan_id         bigint(20)      DEFAULT NULL                COMMENT '计划ID',
  project_id      bigint(20)      DEFAULT NULL                COMMENT '项目ID',
  execute_time    varchar(50)     DEFAULT NULL                COMMENT '计划执行时间',
  execute_cycle   bigint(20)      DEFAULT NULL                COMMENT '执行周期（0天，1周，2月）',
  execute_frequency bigint(20)    DEFAULT NULL                COMMENT '执行频次',
  remark          varchar(500)    DEFAULT NULL                COMMENT '备注',
  create_by       varchar(64)     DEFAULT ''                  COMMENT '创建者',
  create_time     datetime                                    COMMENT '创建时间',
  update_by       varchar(64)     DEFAULT ''                  COMMENT '更新者',
  update_time     datetime                                    COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='护理计划与项目关联表';
