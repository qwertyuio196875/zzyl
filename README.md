# 中州养老 (zzyl)

> 基于 RuoYi 改版的中州养老管理系统。Java 17 / Spring Boot 3.5.11。

## 项目简介

中州养老是一个面向养老机构的服务系统，覆盖入住办理、护理管理、来访登记等核心场景。前端基于 RuoYi-Vue，后端采用多模块 Spring Boot 架构。

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 / 框架 | Java 17、Spring Boot 3.5.11 |
| 数据访问 | MyBatis 3.0.5（手写 XML，**非 MyBatis-Plus**）、PageHelper 2.1.1 |
| 数据库 | MySQL 8.0+，Druid 1.2.28 连接池 |
| 缓存 / 会话 | Redis（Lettuce 客户端） |
| 安全 | Spring Security + JWT（基于 RuoYi） |
| AI | Spring AI 1.0.0，DashScope qwen3.7-plus |

## 模块结构

| 模块 | 说明 |
|---|---|
| `zzyl-admin` | **唯一启动模块**，入口 `com.zzyl.RuoYiApplication` |
| `zzyl-framework` | 通用基础设施：Security、JWT、Redis、Druid、PageHelper、AOP |
| `zzyl-common` | 通用工具、注解、异常、基础 Entity、统一返回 `R` |
| `zzyl-system` | 系统管理：用户、角色、部门、菜单、字典、配置 |
| `zzyl-quartz` | 定时任务 |
| `zzyl-generator` | 代码生成器（Velocity 模板） |
| `zzyl-nursing-platform` | 业务核心：入住 / 护理 / 来访 |
| `zzyl-oss` | 阿里云 OSS 封装 |
| `zzyl-ai-agent` | AI Agent 模块 |

## 快速开始

### 环境要求

- JDK 17（本地默认 `E:\develop\jdk17`）
- Maven 3.9.4+（本地默认 `E:\develop\apache-maven-3.9.4\bin`）
- MySQL 8.0+
- Redis 7.x

### 启动步骤

```bash
# 1. 初始化数据库（执行 sql/ 下的脚本）
# 2. 全量构建
mvn clean install -DskipTests

# 3. 启动应用
mvn -pl zzyl-admin spring-boot:run
```

详细命令、模块依赖、踩坑说明见 `AGENTS.md`。

## 文档索引

| 文档 | 说明 |
|---|---|
| `AGENTS.md` | 项目开发守则（必读） |
| `DATABASE_OPTIMIZATION_RECOMMENDATIONS.md` | MySQL/数据库优化建议（P0~P3 分级） |
| `REDIS_CACHE_OPTIMIZATION.md` | Redis 缓存治理与多级缓存演进方案 |

## 关键约束

- 主类 `RuoYiApplication` 上 `exclude = DataSourceAutoConfiguration`，**不要修改**
- 数据源统一走 Druid，**不要新增** `spring.datasource.*`
- Redis Key 统一格式 `zzyl:{env}:{module}:{biz}:{id}`
- 缓存更新策略：先 DB 后删缓存
- 生产环境必须使用环境变量注入密码，不允许 yml 明文

## 待评估事项

### MyBatis-Plus 引入

当前项目使用传统 MyBatis（手写 XML + `@Param`），**没有 `BaseMapper`**。`pom.xml` 中虽声明了 `mybatis-plus-bom 3.5.17`，但全工程没有任何代码实际使用 MyBatis-Plus。

如需引入 MyBatis-Plus，影响全模块，需要单独决策后再实施。可参考方案：

- **A. 暂不引入**：维持手写 XML + PageHelper，专注 SQL/索引优化（见 `DATABASE_OPTIMIZATION_RECOMMENDATIONS.md`）
- **B. 仅新功能使用**：新增 Service/Mapper 用 MyBatis-Plus，老代码保持现状逐步迁移
- **C. 全量迁移**：所有 Mapper 替换为 `BaseMapper<T>`，分页改为 MyBatis-Plus 内置方式

任何方案落地前需评估：现有 XML 是否兼容、`PageHelper` 与 MyBatis-Plus 分页插件是否冲突、迁移成本。