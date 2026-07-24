# AGENTS.md — zzyl-springboot

> 中州养老（基于 RuoYi 改版）的 Spring Boot 多模块项目。Java 17 / Spring Boot 3.5.11 / MyBatis 3.0.5（非 MyBatis-Plus）/ Druid 1.2.28 / PageHelper 2.1.1。

## 一、运行命令

```bash
# 全量构建（在仓库根目录，所有 Maven 命令都在根目录执行）
mvn clean install -DskipTests

# 仅编译单个模块及其依赖
mvn -pl zzyl-nursing-platform -am clean install -DskipTests

# 启动应用（必须用 -pl zzyl-admin，且先 install 整个工程一次）
mvn -pl zzyl-admin spring-boot:run

# 跑测试
mvn test                                                    # 全部
mvn -pl zzyl-nursing-platform test                          # 单模块
mvn -pl zzyl-nursing-platform test -Dtest=ClassName         # 单测
```

- 无 Maven Wrapper（没有 `.mvn/wrapper`），需本机已装 mvn。
- 若 `mvn` 在 IDEA 之外找不到，已在用户环境 `E:\develop\apache-maven-3.9.4\bin`；Java 17 在 `E:\develop\jdk17`。

## 二、模块结构

| 模块 | 用途 |
|---|---|
| `zzyl-admin` | **唯一启动模块**。入口 `com.zzyl.RuoYiApplication`（`exclude = DataSourceAutoConfiguration`），含 `RuoYiServletInitializer` 用于外部容器打 war。 |
| `zzyl-framework` | 通用基础设施：Security/JWT、Redis、Druid、PageHelper、AOP（Log/DataScope/RateLimiter）、线程池、定时任务对接、文件上传。 |
| `zzyl-common` | 通用工具、注解、异常、基础 Entity（`BaseEntity`/`TreeEntity`）、统一返回 `R`。 |
| `zzyl-system` | 系统管理 CRUD：用户/角色/部门/菜单/字典/配置/岗位/日志 等 18 个 Mapper。 |
| `zzyl-quartz` | 定时任务模块（对应 SQL `sql/quartz.sql`）。 |
| `zzyl-generator` | 代码生成器（Velocity 模板）。 |
| `zzyl-nursing-platform` | 业务：入住（`com.zzyl.admission.*`）、护理（`com.zzyl.nursing.*`）、来访（`visit_record`）。 |
| `zzyl-oss` | 阿里云 OSS 封装。 |
| `zzyl-ai-agent` | AI Agent（Spring AI 1.0.0 + OpenAI SDK 兼容 DashScope `qwen3.7-plus`）。 |

入口依赖：`zzyl-admin` → 聚合 `framework + common + system + nursing-platform + oss + ai-agent + quartz + generator`。改业务看 `zzyl-nursing-platform`，改安全/缓存/数据源看 `zzyl-framework`，改服务入口/Web 看 `zzyl-admin`。

## 三、MyBatis / SQL 约定（高频踩坑）

- Entity 在 `**/domain` 包；Mapper 在 `**/mapper`；XML 在 `src/main/resources/mapper/**/*Mapper.xml`，由 `mybatis.mapperLocations` 自动扫描；`typeAliasesPackage` = `com.zzyl.**.domain`。
- **不是** MyBatis-Plus，**没有** `BaseMapper`。手写 XML + 注解、`@Param` 显式传参。
- 分页用 `PageHelper.startPage(...)` 紧跟在 SQL 前一句调用（`zzyl-common/utils/PageUtils.java` 已有封装）。DB 方言固定 `mysql`。
- Mapper XML 中**禁用** `select *` 风格的 `find_in_set` —— 详见 `SysDeptMapper.xml` 的历史教训：
  - `find_in_set(#{id}, ancestors)` 无法用索引 → 全表扫描；改用 `LIKE CONCAT('%,', #{id}, ',%')` 并对 `ancestors` 加前缀索引。
- Service 调用 Mapper 后若再循环里逐条 `selectById` / `selectXxxByXxx`，就是 N+1，**先**把数据批量查回 Map 再处理。`SysUserServiceImpl.importUser` 是已知反面教材。

## 四、数据库与配置（避免默认值惹坑）

- 主类已 `exclude = DataSourceAutoConfiguration`，所有数据源走 `DruidConfig`（在 `zzyl-framework`）。**不要**新增 `spring.datasource.*` 标准配置。
- 应用配置分两份：`application.yml` + `application-druid.yml`（profile `druid` 在 `application.yml` 中默认激活）。修改 DB 连接/账号改动 application-druid.yml。
- 默认 root/1234 + `localhost:3306/zzyl` 仅本地用；**生产改造**用 `${DB_PASSWORD:}` 环境变量并把 `druid.statViewServlet` 弱口令改掉。
- DB 初始化脚本在 `sql/`（`ry_*.sql` 是基础表、`*.sql` 是业务表）。**业务表索引缺失严重**（`visit_record/visitor_phone/status/visit_date`、`nursing_project_plan/plan_id|project_id`、`resident_check_in/nursing_level_id` 等），见 `DATABASE_OPTIMIZATION_RECOMMENDATIONS.md`。
- 多数据源扩展点在 `DataSourceAspect` + `@DataSource` + `DataSourceType`（已预留，主从尚未启用）。

## 五、Redis / Session / 缓存

- Redis 在 `application.yml.spring.data.redis.host=localhost:6379`，本机无密码。生产必须显式密码。
- `RedisConfig` + `FastJson2JsonRedisSerializer` 已经在 framework 配好；业务缓存键命名（参考现成）`sys:user:byName:{userName}`。
- 字典/参数/菜单这类高频读低频改数据**当前无 MyBatis 二级缓存**，P2 阶段建议开 `<setting name="cacheEnabled" value="true"/>`。

## 六、AI 模块

- `application.yml` 的 `spring.ai.openai.*` 指向 DashScope `https://dashscope.aliyuncs.com/compatible-mode`，模型 `qwen3.7-plus`。**Key 来自环境变量 `OPENAI_API_KEY`** —— 不要硬编码、不要传日志。
- AI 入口在 `com.zzyl.web.controller.aiconsult.ChatController`；SDK 在 `zzyl-ai-agent`。

## 七、测试约定

- 仅 `zzyl-nursing-platform` 与 `zzyl-ai-agent` 写测试，其余模块没有。框架无统一 test starter，测试代码里直接用了 spring-context 的间接依赖，先确认要测的模块有 `spring-boot-starter-test` 再跑 `mvn test`。
- 已有测试风格：JUnit 5 + Mockito，`@ExtendWith(MockitoExtension.class)` + `@Mock` Mapper，再 `@InjectMocks` Service。例：`ResidentCheckInServiceImplTest`。
- AI 测试是 `main` 跑（`AITest.java`），不是 JUnit，跑前必须配 `OPENAI_API_KEY`。

## 八、不做的事（避免重复犯错）

- 不要把 `select *` 加到新 SQL 里，特别是带 `longtext/longblob` 的 `sys_notice/sys_oper_log/sys_logininfor`。
- 不要在 MyBatis XML 写 `find_in_set`；用前缀 `LIKE` 或重塑层级字段。
- 不要在外层 `for` 里逐条 `selectXxxByXxx`；先批量取回 Map。
- 不要为新连接池加 `spring.datasource.*`；坚持用 `spring.datasource.druid.*` + `DruidConfig` 的方式。
- 不要修改 `RuoYiApplication` 上的 `exclude = {DataSourceAutoConfiguration.class}`，否则自配 Druid 不生效。
- 不要硬编码密码/API Key/Token 密钥到 yml；用 `${ENV:default}` 或 Nacos 注入。
- 不要把 SQL 一次脚本当迁移工具用 —— P3 阶段迁 Flyway/Liquibase 之前不要在这基础上加破坏性变更。

## 九、查看数据库优化的完整提案

`DATABASE_OPTIMIZATION_RECOMMENDATIONS.md` 位于仓库根，含 P0/P1/P2/P3 分级修复清单与可复用 SQL 补丁。
