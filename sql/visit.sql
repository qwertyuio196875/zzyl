-- ----------------------------
-- 来访管理模块 - 来访记录表
-- ----------------------------
drop table if exists visit_record;
create table visit_record (
  id                  bigint(20)      not null auto_increment    comment '编号',
  visitor_name        varchar(50)     default null                comment '访客姓名',
  visitor_phone       varchar(20)     default null                comment '访客电话',
  visitor_id_card     varchar(20)     default null                comment '访客身份证',
  visited_name        varchar(50)     default null                comment '被访人姓名',
  visited_dept        varchar(50)     default null                comment '被访人部门',
  visit_reason        varchar(500)   default null                comment '来访事由',
  visit_date          varchar(20)    default null                comment '预约来访日期',
  visit_time          varchar(20)    default null                comment '预约来访时间',
  actual_visit_time   datetime       default null                comment '实际来访时间',
  leave_time          datetime       default null                comment '离开时间',
  status              tinyint(2)     default 0                    comment '来访状态（0:待审核, 1:已预约, 2:已签到, 3:已离开, 4:已取消）',
  create_by           varchar(64)     default ''                   comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                   comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark             varchar(500)    default ''                   comment '备注',
  primary key (id)
) engine=innodb auto_increment=100 comment = '来访记录表';

-- ----------------------------
-- 来访管理模块 - 菜单SQL
-- ----------------------------
-- 添加护理管理一级菜单（如果不存在）
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2000, '养老护理', 0, 5, 'nursing', null, '', '', 1, 0, 'M', '0', '0', '', 'nursing', 'admin', sysdate(), '养老护理管理目录'
where not exists (select 1 from sys_menu where menu_id = 2000);

-- 添加来访管理菜单
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2001, '来访管理', 2000, 1, 'visit', 'nusring/visit/index', '', 'Visit', 1, 0, 'C', '0', '0', 'nursing:visit:list', 'peoples', 'admin', sysdate(), '来访管理菜单'
where not exists (select 1 from sys_menu where menu_id = 2001);

-- 来访管理按钮
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2002, '来访查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2002);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2003, '来访新增', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2003);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2004, '来访修改', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2004);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2005, '来访删除', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2005);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2006, '来访导出', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2006);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2007, '来访审核', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:approve', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2007);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2008, '来访取消', 2001, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:cancel', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2008);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2009, '访客签到', 2001, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:signIn', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2009);

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 2010, '离开登记', 2001, 9, '', '', '', '', 1, 0, 'F', '0', '0', 'nursing:visit:leave', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where menu_id = 2010);

-- ----------------------------
-- 来访状态字典数据
-- ----------------------------
insert into sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
select 100, '来访状态', 'visit_status', '0', 'admin', sysdate(), '来访状态字典'
where not exists (select 1 from sys_dict_type where dict_type = 'visit_status');

insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1000, 0, '待审核', '0', 'visit_status', '', 'danger', 'N', '0', 'admin', sysdate(), '来访状态-待审核'
where not exists (select 1 from sys_dict_data where dict_type = 'visit_status' and dict_value = '0');

insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1001, 1, '已预约', '1', 'visit_status', '', 'warning', 'N', '0', 'admin', sysdate(), '来访状态-已预约'
where not exists (select 1 from sys_dict_data where dict_type = 'visit_status' and dict_value = '1');

insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1002, 2, '已签到', '2', 'visit_status', '', 'success', 'N', '0', 'admin', sysdate(), '来访状态-已签到'
where not exists (select 1 from sys_dict_data where dict_type = 'visit_status' and dict_value = '2');

insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1003, 3, '已离开', '3', 'visit_status', '', 'info', 'N', '0', 'admin', sysdate(), '来访状态-已离开'
where not exists (select 1 from sys_dict_data where dict_type = 'visit_status' and dict_value = '3');

insert into sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1004, 4, '已取消', '4', 'visit_status', '', 'default', 'N', '0', 'admin', sysdate(), '来访状态-已取消'
where not exists (select 1 from sys_dict_data where dict_type = 'visit_status' and dict_value = '4');
