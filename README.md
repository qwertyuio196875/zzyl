# 中州养老 (zzyl)

> 基于 RuoYi 改版的中州养老管理系统。Java 17 / Spring Boot 3.5.11 / MyBatis（非 MyBatis-Plus）。

## 项目简介

中州养老是面向养老机构的一体化服务系统，覆盖入住办理、护理管理、来访登记、智能问答等核心场景。前端基于 RuoYi-Vue，后端采用多模块 Spring Boot 架构，AI 能力通过 Spring AI 接入 DashScope `qwen3.7-plus`。

## 核心功能

| 模块    | 路径前缀                   | 说明                               |
| ----- | ---------------------- | -------------------------------- |
| 入住办理  | `com.zzyl.admission.*` | 入住申请、签约、床位分配、入住登记                |
| 护理管理  | `com.zzyl.nursing.*`   | 护理等级、护理项目、护理计划、护理记录              |
| 来访登记  | `visit_record`         | 来访预约、签到、来访记录查询                   |
| AI 咨询 | `aiconsult/chat`       | 基于 DashScope 的智能问答               |
| 系统管理  | —                      | 用户、角色、部门、菜单、字典、配置、定时任务（沿用 RuoYi） |

## 技术栈

| 类别      | 技术                                                                                             |
| ------- | ---------------------------------------------------------------------------------------------- |
| 语言 / 框架 | Java 17、Spring Boot 3.5.11                                                                     |
| 数据访问    | MyBatis 3.0.5（手写 XML）、PageHelper 2.1.1；新功能可按 `MYBATIS_PLUS_ADOPTION_PLAN.md` 渐进引入 MyBatis-Plus |
| 数据库     | MySQL 8.0+，Druid 1.2.28 连接池                                                                    |
| 缓存 / 会话 | Redis 7.x（Lettuce 客户端）                                                                         |
| 安全      | Spring Security + JWT（基于 RuoYi）                                                                |
| 对象存储    | 阿里云 OSS（`zzyl-oss`）                                                                            |
| AI      | Spring AI 1.0.0 + DashScope 兼容模式（模型 `qwen3.7-plus`）                                            |

## 模块结构

```
zzyl-admin  ──► 唯一启动模块（com.zzyl.RuoYiApplication）
   ├─ zzyl-framework        Security/JWT、Redis、Druid、PageHelper、AOP、文件上传、线程池
   ├─ zzyl-common           通用工具、注解、异常、BaseEntity、统一返回 R
   ├─ zzyl-system           用户/角色/部门/菜单/字典/配置/岗位/日志（18 个 Mapper）
   ├─ zzyl-quartz           定时任务（对应 sql/quartz.sql）
   ├─ zzyl-generator        代码生成器（Velocity 模板）
   ├─ zzyl-nursing-platform 业务核心：入住 / 护理 / 来访
   ├─ zzyl-oss              阿里云 OSS 封装
   └─ zzyl-ai-agent         AI Agent（Spring AI + DashScope）
```

> 业务改动集中在 `zzyl-nursing-platform`；安全/缓存/数据源改动集中在 `zzyl-framework`；启动入口/Web 改动集中在 `zzyl-admin`。

## 目录布局

```
zzyl-springboot/
├─ zzyl-admin/               启动模块（War/Jar 入口）
├─ zzyl-framework/           通用基础设施
├─ zzyl-common/              公共工具与基础类
├─ zzyl-system/              系统管理
├─ zzyl-nursing-platform/    业务核心
├─ zzyl-quartz/              定时任务
├─ zzyl-generator/           代码生成
├─ zzyl-oss/                 对象存储
├─ zzyl-ai-agent/            AI Agent
├─ sql/                      数据库脚本（基础表 ry_*.sql + 业务表 *.sql + 补丁 patch_*.sql）
├─ logs/                     运行日志
├─ AGENTS.md                 开发守则（必读）
└─ README.md                 本文件
```

## 快速开始

```bash
# 1. 初始化：执行 sql/ry_*.sql + 业务 sql/*.sql，按需跑 patch_*.sql
# 2. 全量构建
mvn clean install -DskipTests
# 3. 启动
mvn -pl zzyl-admin spring-boot:run
```

本地默认账号 `root/123456`，Redis 无密码，**仅供本地调试**；生产用 `DB_PASSWORD` / `REDIS_PASSWORD` / `OPENAI_API_KEY` 等环境变量覆盖。测试用 `mvn -pl <module> test [-Dtest=ClassName]`。

## 关键约束

- 不改 `RuoYiApplication` 的 `exclude = DataSourceAutoConfiguration`
- 数据源统一走 `DruidConfig` + `spring.datasource.druid.*`，不新增 `spring.datasource.*`
- Redis Key 格式 `zzyl:{env}:{module}:{biz}:{id}`，走 `CacheKeyConstants`
- 缓存「先 DB 后删缓存」；禁止 `@CachePut` 直写；禁止无 TTL 永久 Key
- 密码、API Key 走 `${ENV:default}` 注入，**禁止 yml 明文**
- 禁止 `select *`、XML 里写 `find_in_set`、循环里逐条 `selectById`
- MyBatis-Plus 仅新功能引入（见 `MYBATIS_PLUS_ADOPTION_PLAN.md`），老模块保留手写 XML
- 多数据源扩展点：`DataSourceAspect` + `@DataSource` + `DataSourceType`（主从未启用）

详细约定见 `AGENTS.md`。

## 许可

仅供中州养老内部使用。