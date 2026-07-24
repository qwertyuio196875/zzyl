-- ----------------------------
-- 中州养老 · 入退管理模块初始化脚本
-- 在导入 ry_20260417.sql 与 quartz.sql 之后执行
-- ----------------------------

-- ----------------------------
-- 1、业务表
-- ----------------------------
drop table if exists resident_check_out;
drop table if exists resident_check_in;
drop table if exists health_assessment;

create table health_assessment (
  id                      bigint(20)      not null auto_increment    comment '主键',
  assessment_no           varchar(32)     not null                   comment '评估编号',
  elder_name              varchar(64)     not null                   comment '老人姓名',
  id_card                 varchar(18)     default null               comment '身份证号',
  gender                  char(1)         default '0'                comment '性别（0男 1女）',
  age                     int(4)          default null               comment '年龄',
  phone                   varchar(20)     default null               comment '联系电话',
  assessment_date         date            default null               comment '评估日期',
  assessor                varchar(64)     default null               comment '评估人',
  health_level            char(1)         default null               comment '健康等级（1自理 2半自理 3不能自理 4特护）',
  cognitive_level         char(1)         default null               comment '认知等级（1正常 2轻度 3中度 4重度）',
  mobility_level          char(1)         default null               comment '行动能力（1正常 2辅助 3轮椅 4卧床）',
  self_care_level         char(1)         default null               comment '自理能力（1完全 2部分 3不能）',
  medical_history         varchar(1000)   default null               comment '既往病史',
  allergy_history         varchar(500)    default null               comment '过敏史',
  assessment_conclusion   varchar(1000)   default null               comment '评估结论',
  status                  char(1)         default '0'                comment '状态（0待评估 1已完成）',
  create_by               varchar(64)     default ''                 comment '创建者',
  update_by               varchar(64)     default ''                 comment '更新者',
  remark                  varchar(500)    default null               comment '备注',
  create_time             datetime                                   comment '创建时间',
  update_time             datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_assessment_no (assessment_no)
) engine=innodb auto_increment=1 comment = '健康评估表';

create table resident_check_in (
  id                      bigint(20)      not null auto_increment    comment '主键',
  check_in_no             varchar(32)     not null                   comment '入住编号',
  assessment_id           bigint(20)      default null               comment '健康评估ID',
  elder_name              varchar(64)     not null                   comment '老人姓名',
  id_card                 varchar(18)     default null               comment '身份证号',
  gender                  char(1)         default '0'                comment '性别（0男 1女）',
  age                     int(4)          default null               comment '年龄',
  phone                   varchar(20)     default null               comment '联系电话',
  nursing_level_id        bigint(20)      default null               comment '护理等级ID',
  nursing_level_name      varchar(64)     default null               comment '护理等级名称',
  nursing_plan_id         bigint(20)      default null               comment '护理计划ID',
  nursing_plan_name       varchar(64)     default null               comment '护理计划名称',
  building_name           varchar(64)     default null               comment '楼栋',
  room_no                 varchar(32)     default null               comment '房间号',
  bed_no                  varchar(32)     default null               comment '床位号',
  check_in_date           date            default null               comment '入住日期',
  contract_start_date     date            default null               comment '合同开始日期',
  contract_end_date       date            default null               comment '合同结束日期',
  emergency_contact       varchar(64)     default null               comment '紧急联系人',
  emergency_phone         varchar(20)     default null               comment '紧急联系电话',
  relationship            varchar(32)     default null               comment '与老人关系',
  deposit                 decimal(10,2)   default null               comment '押金（元）',
  monthly_fee             decimal(10,2)   default null               comment '月费用（元）',
  handler                 varchar(64)     default null               comment '办理人',
  status                  char(1)         default '0'                comment '状态（0待办理 1办理中 2已入住 3已取消 4已退住）',
  create_by               varchar(64)     default ''                 comment '创建者',
  update_by               varchar(64)     default ''                 comment '更新者',
  remark                  varchar(500)    default null               comment '备注',
  create_time             datetime                                   comment '创建时间',
  update_time             datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_check_in_no (check_in_no),
  key idx_assessment_id (assessment_id)
) engine=innodb auto_increment=1 comment = '入住办理表';

create table resident_check_out (
  id                      bigint(20)      not null auto_increment    comment '主键',
  check_out_no            varchar(32)     not null                   comment '退住编号',
  check_in_id             bigint(20)      not null                   comment '入住记录ID',
  elder_name              varchar(64)     not null                   comment '老人姓名',
  id_card                 varchar(18)     default null               comment '身份证号',
  bed_no                  varchar(32)     default null               comment '床位号',
  apply_date              date            default null               comment '申请日期',
  expected_check_out_date date            default null               comment '预计退住日期',
  actual_check_out_date   date            default null               comment '实际退住日期',
  reason                  varchar(32)     default null               comment '退住原因',
  reason_detail           varchar(1000)   default null               comment '退住说明',
  status                  char(1)         default '0'                comment '状态（0待审核 1审核通过 2审核驳回 3已退住）',
  approver                varchar(64)     default null               comment '审核人',
  approve_time            datetime        default null               comment '审核时间',
  approve_remark          varchar(500)    default null               comment '审核意见',
  settlement_amount       decimal(10,2)   default null               comment '结算金额（元）',
  refund_amount           decimal(10,2)   default null               comment '退款金额（元）',
  create_by               varchar(64)     default ''                 comment '创建者',
  update_by               varchar(64)     default ''                 comment '更新者',
  remark                  varchar(500)    default null               comment '备注',
  create_time             datetime                                   comment '创建时间',
  update_time             datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_check_out_no (check_out_no),
  key idx_check_in_id (check_in_id)
) engine=innodb auto_increment=1 comment = '退住管理表';

-- ----------------------------
-- 2、字典
-- ----------------------------
insert into sys_dict_type values(12, '评估状态', 'assessment_status', '0', 'admin', sysdate(), '', null, '健康评估状态');
insert into sys_dict_type values(13, '健康等级', 'assessment_level', '0', 'admin', sysdate(), '', null, '健康评估等级');
insert into sys_dict_type values(14, '入住状态', 'checkin_status', '0', 'admin', sysdate(), '', null, '入住办理状态');
insert into sys_dict_type values(15, '退住状态', 'checkout_status', '0', 'admin', sysdate(), '', null, '退住申请状态');
insert into sys_dict_type values(16, '退住原因', 'checkout_reason', '0', 'admin', sysdate(), '', null, '退住原因');

insert into sys_dict_data values(110, 1, '待评估', '0', 'assessment_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(111, 2, '已完成', '1', 'assessment_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(112, 1, '自理', '1', 'assessment_level', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(113, 2, '半自理', '2', 'assessment_level', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(114, 3, '不能自理', '3', 'assessment_level', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(115, 4, '特护', '4', 'assessment_level', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(116, 1, '待办理', '0', 'checkin_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(117, 2, '办理中', '1', 'checkin_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(118, 3, '已入住', '2', 'checkin_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(119, 4, '已取消', '3', 'checkin_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(128, 5, '已退住', '4', 'checkin_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(120, 1, '待审核', '0', 'checkout_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(121, 2, '审核通过', '1', 'checkout_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(122, 3, '审核驳回', '2', 'checkout_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(123, 4, '已退住', '3', 'checkout_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(124, 1, '回家养老', '1', 'checkout_reason', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(125, 2, '转院治疗', '2', 'checkout_reason', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(126, 3, '家属接回', '3', 'checkout_reason', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(127, 4, '其他', '4', 'checkout_reason', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 3、菜单
-- ----------------------------
insert into sys_menu values('3000', '入退管理', '0', '6', 'admission', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '入退管理目录');
insert into sys_menu values('3001', '健康评估', '3000', '1', 'assessment', 'admission/assessment/index', '', '', 1, 0, 'C', '0', '0', 'admission:assessment:list', 'form', 'admin', sysdate(), '', null, '健康评估菜单');
insert into sys_menu values('3002', '入住办理', '3000', '2', 'checkin', 'admission/checkin/index', '', '', 1, 0, 'C', '0', '0', 'admission:checkin:list', 'user', 'admin', sysdate(), '', null, '入住办理菜单');
insert into sys_menu values('3003', '退住管理', '3000', '3', 'checkout', 'admission/checkout/index', '', '', 1, 0, 'C', '0', '0', 'admission:checkout:list', 'exit-fullscreen', 'admin', sysdate(), '', null, '退住管理菜单');

insert into sys_menu values('3100', '健康评估查询', '3001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:assessment:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3101', '健康评估新增', '3001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:assessment:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', '健康评估修改', '3001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:assessment:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', '健康评估删除', '3001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:assessment:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', '健康评估导出', '3001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:assessment:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3110', '入住办理查询', '3002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3111', '入住办理新增', '3002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3112', '入住办理修改', '3002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3113', '入住办理删除', '3002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3114', '入住办理导出', '3002', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3115', '入住确认', '3002', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkin:confirm', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3120', '退住管理查询', '3003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3121', '退住申请新增', '3003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3122', '退住申请修改', '3003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3123', '退住申请删除', '3003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3124', '退住申请导出', '3003', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3125', '退住审核', '3003', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:approve', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3126', '退住确认', '3003', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'admission:checkout:complete', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu values ('2', '3000');
insert into sys_role_menu values ('2', '3001');
insert into sys_role_menu values ('2', '3002');
insert into sys_role_menu values ('2', '3003');
insert into sys_role_menu values ('2', '3100');
insert into sys_role_menu values ('2', '3101');
insert into sys_role_menu values ('2', '3102');
insert into sys_role_menu values ('2', '3103');
insert into sys_role_menu values ('2', '3104');
insert into sys_role_menu values ('2', '3110');
insert into sys_role_menu values ('2', '3111');
insert into sys_role_menu values ('2', '3112');
insert into sys_role_menu values ('2', '3113');
insert into sys_role_menu values ('2', '3114');
insert into sys_role_menu values ('2', '3115');
insert into sys_role_menu values ('2', '3120');
insert into sys_role_menu values ('2', '3121');
insert into sys_role_menu values ('2', '3122');
insert into sys_role_menu values ('2', '3123');
insert into sys_role_menu values ('2', '3124');
insert into sys_role_menu values ('2', '3125');
insert into sys_role_menu values ('2', '3126');
