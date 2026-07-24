# 数据库模块审查与优化建议

> 审查范围：`zzyl-springboot` 工程全量数据库相关代码与脚本
> 审查日期：2026-07-24
> 关联依赖：Druid 1.2.28、MyBatis 3.0.5、PageHelper 2.1.1、MySQL 8.0+

---

## 一、整体盘点

| 模块 | 现状 |
|---|---|
| 数据库 | MySQL 8.0+，库名 `zzyl`，字符集 `utf8mb4` / 排序规则 `utf8mb4_general_ci` |
| 连接池 | Alibaba Druid 1.2.28（主库，从库未启用） |
| ORM | 传统 MyBatis 3.0.5，**非 MyBatis-Plus**，无 `BaseMapper` |
| 分页 | PageHelper 2.1.1 |
| 多数据源 | `DynamicDataSource` + AOP（已预留从库扩展） |
| 监控 | Druid 监控 `/druid/*`，启用慢 SQL 阈值 1000ms |
| 表数量 | 约 39 张（系统 20 + 业务 8 + Quartz 11） |
| Mapper | 约 25 个，SQL 以 XML 为主 |

---

## 二、问题分级与修复建议

### 🔴 P0 — 必须立刻修复

#### 1. `find_in_set` 导致全表扫描

- **位置**：`SysDeptMapper.xml:78`
- **现状**：`select * from sys_dept where find_in_set(#{deptId}, ancestors)`
- **问题**：`ancestors` 存的是逗号路径串，`FIND_IN_SET` 无法利用索引。
- **建议**：改用前缀匹配或重塑表结构：
  ```sql
  SELECT dept_id, parent_id, ancestors, dept_name
  FROM sys_dept
  WHERE ancestors LIKE CONCAT('%,', #{deptId}, ',%')
     OR dept_id = #{deptId};
  ```
  或为 `ancestors` 字段加索引（仅前缀匹配走得到）。

#### 2. `importUser` 批量导入存在 N+1

- **位置**：`SysUserServiceImpl.java:510-536`
- **现状**：循环内逐条 `selectUserByUserName`，导入 1000 条约 2000+ 次 SQL。
- **建议**：在循环外一次性查出已存在用户名集合：
  ```java
  Set<String> existing = userMapper.selectUserNames(
      userList.stream().map(SysUser::getUserName).collect(Collectors.toList()));
  userList.removeIf(u -> existing.contains(u.getUserName()));
  userMapper.batchInsertUser(userList); // 新增批量 insert
  ```

#### 3. `visit_record.visit_date / visit_time` 类型错配

- **位置**：`sql/visit.sql`
- **现状**：
  ```sql
  visit_date varchar(20)
  visit_time varchar(20)
  ```
- **建议**：
  ```sql
  ALTER TABLE visit_record
      MODIFY visit_date DATE NOT NULL,
      MODIFY visit_time TIME NOT NULL;
  ```
  同理核查 `health_assessment.assessment_date`、`resident_check_in.check_in_date` 等。

---

### 🟠 P1 — 重要问题

#### 4. 业务表索引严重缺失

| 表 | 缺失索引字段 | 建议 |
|---|---|---|
| `visit_record` | `visitor_phone`、`status`、`visit_date` | `idx_visit_date(visit_date)`、`idx_visitor_phone(visitor_phone)`、`idx_status(status)` |
| `health_assessment` | `elder_name`、`assessment_date` | 各自加单列索引 |
| `resident_check_in` | `elder_name`、`nursing_level_id`、`status` | 各自加单列索引 |
| `resident_check_out` | `elder_name`、`status` | 各自加单列索引 |
| `nursing_project_plan` | `plan_id`、`project_id` | 关联表必须加 |
| `nursing_project` | `status` | 单列索引 |
| `nursing_level` | `plan_id` | 单列索引 |

模板：
```sql
CREATE INDEX idx_visit_date ON visit_record(visit_date);
```
建议命名统一为 `idx_表名_字段` 风格。

#### 5. 循环内权限校验 — N+1

- **位置**：`SysUserServiceImpl.deleteUserByIds`、`SysRoleServiceImpl.checkRoleDataScope`
- **现状**：
  ```java
  for (Long userId : userIds) {
      checkUserAllowed(new SysUser(userId));
      checkUserDataScope(userId);
  }
  ```
- **建议**：循环外一次性查询：
  ```java
  List<SysUser> users = userMapper.selectUserByIds(userIds);
  users.forEach(u -> { checkUserAllowed(u); checkUserDataScope(u); });
  ```

#### 6. 字段类型过度使用

| 字段 | 当前 | 建议 |
|---|---|---|
| `sys_logininfor.login_ip` | `varchar(128)` | `varchar(45)` |
| `sys_oper_log.oper_url` | `varchar(255)` | `varchar(512)` |
| `sys_oper_log.oper_param` | `varchar(2000)` | `text` 或拆表 |
| `sys_notice.notice_content` | `longblob` | `longtext` |
| 业务表 `status` / `sex` | `char(1)` | `tinyint` + 字典说明 |

#### 7. 业务表冗余字段

- **位置**：`resident_check_in`、`resident_check_out`
- **问题**：
  - `resident_check_in` 同时存 `elder_name`、`id_card`、`gender`、`age`、`phone`，与 `health_assessment` 完全重复；
  - 又存 `nursing_level_name`、`nursing_plan_name`，与对应表重复。
- **建议**：
  - 删除 `elder_name`、`id_card`、`gender`、`age`、`phone`，改为外键 `elder_id`（指向 `elder` 或 `health_assessment`）；
  - 删除 `nursing_level_name`、`nursing_plan_name`，查询时 JOIN；
  - 退住表同理：只保留入住记录 ID 与差异字段。

---

### 🟡 P2 — 性能与可维护性

#### 8. 事务边界过大

- **位置**：多处 `@Transactional` Service 方法，例如 `SysUserServiceImpl.insertUser()` 同时处理用户+岗位+角色。
- **建议**：
  - 拆分为独立 `@Transactional(propagation = REQUIRES_NEW)` 方法；
  - 只读校验方法加 `@Transactional(readOnly = true)`。

#### 9. `SELECT *` 风险

- **位置**：`SysDeptMapper.xml` 多处使用 `select *`。
- **建议**：resultMap 已知的情况下显式列名，规避大字段误传。

#### 10. 缺少二级缓存

字典 (`sys_dict_data`)、参数配置 (`sys_config`)、菜单 (`sys_menu`) 几乎只读但每次都打 DB。
- **建议**：
  ```xml
  <setting name="cacheEnabled" value="true"/>
  ```
  在对应 `<cache/>` 或 `<cache-ref/>` 中启用。

#### 11. 高频 `selectUserByUserName` 缓存

- **建议**：Redis 缓存 `sys:user:byName:{userName}`，TTL 30 分钟。

#### 12. 主从未启用但引入复杂度

当前 `slave.enabled=false`，保留 `DataSourceAspect`、`DynamicDataSourceContextHolder`、`@DataSource` 注解。
- **建议**：
  - 短期不启用就保留并写 README 说明（防误用 `@DataSource("SLAVE")` 路由错误）；
  - 或在 `@DataSource` 处理时增加 fallback。

---

### 🟢 P3 — 长期改进

#### 13. 引入数据库迁移工具

当前 `sql/*.sql` 一次性执行，无版本管理。
- **建议**：引入 Flyway 或 Liquibase，目录约定 `db/migration/V1__init.sql`，CI 自动校验。

#### 14. 启用 Druid Wall 防火墙

`druid-spring-boot-3-starter` 默认未启用 Wall。
- **建议**：
  ```yaml
  druid:
    filter:
      wall:
        enabled: true
        config:
          none-base-statement-allow: false
  ```

#### 15. 业务关键表添加乐观锁

对 `resident_check_in`、`nursing_plan` 等业务核心表增加 `version int default 0`，Mapper 配合乐观锁更新。

#### 16. 改造为 MyBatis-Plus 的可行性评估

当前传统 MyBatis + XML 样板代码较多。
- **建议**：评估迁移到 MyBatis-Plus（`BaseMapper<T>`、`LambdaQueryWrapper`、`Page` 对象），可减少 60% 样板代码，并原生支持乐观锁、字段填充、逻辑删除。

#### 17. 日志表分区

`sys_oper_log`、`sys_logininfor`、`sys_job_log` 易暴涨。
- **建议**：
  ```sql
  PARTITION BY RANGE (TO_DAYS(create_time)) (
      PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
      PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01'))
  );
  ```
  或定期归档到冷表。

---

## 三、连接池与配置建议

| 项目 | 当前 | 建议 |
|---|---|---|
| `useSSL=true` | 是 | 生产建议 `useSSL=true&requireSSL=true&verifyServerCertificate=true` |
| `serverTimezone=GMT+8` | 是 | OK |
| `password=1234` 写在 yml | 是 | 必须改为 `${DB_PASSWORD:}` + 环境变量 / Nacos / Vault |
| `druid statViewServlet login-username/password=ruoyi/1234` | 弱口令 | 改复杂密码，并限制内网/网关访问 |
| `druid.web-stat-filter` | 未配置 | 增加 `exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"` |
| `connectTimeout=10000`、`socketTimeout=30000` | 中等 | 高并发可压到 5000/10000，并启用 `removeAbandoned=true` |
| `maxActive=50` | 偏小 | 视实际并发上调，并加监控告警 |
| `HikariCP` 替代 | — | 评估团队熟悉度，HikariCP 性能更高、配置更轻；Druid 优势在统计与 Wall |

---

## 四、SQL 补丁示例（可立即复用）

### 4.1 索引补全

```sql
-- visit_record
CREATE INDEX idx_visit_date ON visit_record(visit_date);
CREATE INDEX idx_visitor_phone ON visit_record(visitor_phone);
CREATE INDEX idx_status ON visit_record(status);

-- nursing 业务表
CREATE INDEX idx_plan_id ON nursing_project_plan(plan_id);
CREATE INDEX idx_project_id ON nursing_project_plan(project_id);
CREATE INDEX idx_status ON nursing_project(status);
CREATE INDEX idx_plan_id ON nursing_level(plan_id);

-- admission 业务表
CREATE INDEX idx_elder_name ON health_assessment(elder_name);
CREATE INDEX idx_assessment_date ON health_assessment(assessment_date);
CREATE INDEX idx_elder_name ON resident_check_in(elder_name);
CREATE INDEX idx_nursing_level_id ON resident_check_in(nursing_level_id);
CREATE INDEX idx_status ON resident_check_in(status);
CREATE INDEX idx_elder_name ON resident_check_out(elder_name);
CREATE INDEX idx_status ON resident_check_out(status);
```

### 4.2 字段类型修复

```sql
-- visit_record 日期/时间类型修正
ALTER TABLE visit_record
    MODIFY visit_date DATE NOT NULL,
    MODIFY visit_time TIME NOT NULL;

-- 缩短过宽的 IP 字段
ALTER TABLE sys_logininfor MODIFY login_ip VARCHAR(45);

-- 修复 URL 字段
ALTER TABLE sys_oper_log MODIFY oper_url VARCHAR(512);

-- 公告内容由 blob 改 text
ALTER TABLE sys_notice MODIFY notice_content LONGTEXT;
```

### 4.3 规范化 `find_in_set`

```sql
-- 新增 ancestors 前缀索引
CREATE INDEX idx_ancestors_prefix ON sys_dept(ancestors(20));
```

### 4.4 启动 Druid Wall

```yaml
spring:
  datasource:
    druid:
      filter:
        wall:
          enabled: true
          config:
            none-base-statement-allow: false
```

---

## 五、修复优先级路线图

| 阶段 | 优先级 | 工作项 |
|---|---|---|
| **第 1 周** | P0 | 修 `find_in_set`、改 `importUser` N+1、修复 `visit_date/time` 类型 |
| **第 2 周** | P1 | 补业务表索引、批量校验改写、删冗余字段、字段类型精简 |
| **第 3–4 周** | P2 | 拆大事务、增二级缓存、`SELECT *` 收口、Druid 密码安全化、Wall 启用 |
| **后续** | P3 | Flyway 迁移、MyBatis-Plus 评估、日志表分区 |

---

## 六、验证清单

修复完成后请逐项验证：

- [ ] `EXPLAIN` `SELECT * FROM sys_dept WHERE find_in_set('1', ancestors)` 不再 type=ALL
- [ ] `EXPLAIN` 批量导入用户的关键 SQL 命中索引或走批量执行
- [ ] `SHOW INDEX FROM visit_record;` 看到 `visit_date`、`visitor_phone`、`status` 索引
- [ ] `SHOW WARNINGS` 启动 Druid Wall 后无 SQL 注入告警
- [ ] `SELECT * FROM visit_record WHERE visit_date BETWEEN '2026-07-01' AND '2026-07-31'` 走索引
- [ ] Redis 缓存命中率（`sys_dict_data`、`sys_config`）监控 > 80%
- [ ] `SysUserMapper.batchInsertUser` 单元测试覆盖 1000 条数据导入 < 500ms

---

## 七、参考文件路径

| 类型 | 路径 |
|---|---|
| 主配置 | `zzyl-admin/src/main/resources/application.yml` |
| Druid 配置 | `zzyl-admin/src/main/resources/application-druid.yml` |
| MyBatis 设置 | `zzyl-admin/src/main/resources/mybatis/mybatis-config.xml` |
| Java 配置 | `zzyl-framework/.../config/DruidConfig.java` |
| Java 配置 | `zzyl-framework/.../config/MyBatisConfig.java` |
| 动态数据源 | `zzyl-framework/.../datasource/DynamicDataSource.java` |
| 注解 | `zzyl-common/.../annotation/DataSource.java` |
| Mapper | `zzyl-system/mapper/SysUserMapper.java` + `SysUserMapper.xml` |
| Mapper | `zzyl-system/mapper/SysDeptMapper.java` + `SysDeptMapper.xml` |
| Service | `zzyl-system/service/impl/SysUserServiceImpl.java` |
| Service | `zzyl-system/service/impl/SysRoleServiceImpl.java` |
| SQL | `sql/ry_20260417.sql`、`sql/quartz.sql`、`sql/admission.sql`、`sql/nursing.sql`、`sql/visit.sql` |
