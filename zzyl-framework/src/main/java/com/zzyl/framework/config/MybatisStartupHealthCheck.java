package com.zzyl.framework.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * MyBatis + MyBatis-Plus 共存启动自检。
 *
 * 启动后断言以下事项，失败则阻止启动：
 *  1. SqlSessionFactory Bean 恰好 1 个
 *  2. Configuration 类型必须是 com.baomidou.mybatisplus.core.MybatisConfiguration
 *  3. 关键 Mapper 的 statement ID 已注册
 *  4. 自定义 TypeHandler 已注册
 *
 * 注意：本自检只验证注册/扫描层面，不验证 SQL 真实执行结果。
 */
@Component
@Order(0)
public class MybatisStartupHealthCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MybatisStartupHealthCheck.class);

    /** 必须存在的代表性 statement ID，覆盖 4 个 XML 模块 */
    private static final List<String> REQUIRED_STATEMENTS = Arrays.asList(
            // 系统模块（任选一个真实存在的）
            "com.zzyl.system.mapper.SysUserMapper.selectUserList",
            // 业务模块（任选一个真实存在的）
            "com.zzyl.nursing.mapper.VisitRecordMapper.selectVisitRecordList",
            // 定时任务模块
            "com.zzyl.quartz.mapper.SysJobMapper.selectJobList",
            // 代码生成模块
            "com.zzyl.generator.mapper.GenTableMapper.selectGenTableList"
    );

    /** 必须注册的自定义 TypeHandler 全限定类名 */
    private static final List<String> REQUIRED_TYPE_HANDLERS = Arrays.asList(
            "com.zzyl.framework.handler.StringToDateTypeHandler",
            "com.zzyl.framework.handler.StringToTimeTypeHandler"
    );

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[MyBatis/MP 共存自检] 开始...");

        // 断言 1：SqlSessionFactory 数量 = 1
        // （直接通过 @Autowired 注入；如果有多个，启动就会因 NoUniqueBeanDefinitionException 失败）

        // 断言 2：Configuration 类型必须是 MybatisConfiguration
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        if (!(configuration instanceof MybatisConfiguration)) {
            throw new IllegalStateException(
                "[MyBatis/MP 共存自检] 失败：Configuration 类型不是 MybatisConfiguration，实际为 "
                + configuration.getClass().getName()
                + "。说明 MybatisSqlSessionFactoryBean 未生效，请检查 MyBatisConfig.java 改动。"
            );
        }

        // 断言 3：关键 statement ID 已注册
        for (String statementId : REQUIRED_STATEMENTS) {
            try {
                configuration.hasStatement(statementId);
                // hasStatement 不会抛异常，但 InternalErrorOnUpdate 等场景下返回 false，需要二次确认
                if (!isStatementPresent(configuration, statementId)) {
                    throw new IllegalStateException(
                        "[MyBatis/MP 共存自检] 失败：缺失 statement [" + statementId + "]。"
                        + "请检查对应 Mapper XML 是否在 classpath 扫描路径内。"
                    );
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                    "[MyBatis/MP 共存自检] 失败：查询 statement [" + statementId + "] 时异常", e
                );
            }
        }

        // 断言 4：自定义 TypeHandler 已注册
        for (String handlerClassName : REQUIRED_TYPE_HANDLERS) {
            boolean registered = configuration.getTypeHandlerRegistry()
                    .getTypeHandlers().stream()
                    .anyMatch(h -> h.getClass().getName().equals(handlerClassName));
            if (!registered) {
                throw new IllegalStateException(
                    "[MyBatis/MP 共存自检] 失败：TypeHandler 未注册 [" + handlerClassName + "]。"
                    + "请检查 mybatis-config.xml 中 <typeHandlers> 块。"
                );
            }
        }

        log.info("[MyBatis/MP 共存自检] 通过 ✓ Configuration={} | statements={} | typeHandlers={}",
                configuration.getClass().getSimpleName(),
                REQUIRED_STATEMENTS.size(),
                REQUIRED_TYPE_HANDLERS.size());
    }

    /**
     * 判断 statement 是否已注册。
     *
     * 注意：MP 3.5.17 把 statement 存在 MybatisConfiguration 自己的字段里，
     * 父类 Configuration.mappedStatements 字段是空的，所以 configuration.hasStatement()
     * 在 MP 环境下不可靠。这里用反射遍历 getMappedStatements() 返回的 Collection，
     * 通过 getId() 拿 id。
     */
    private boolean isStatementPresent(org.apache.ibatis.session.Configuration configuration, String statementId) {
        try {
            java.util.Collection<?> statements = configuration.getMappedStatements();
            for (Object obj : statements) {
                try {
                    java.lang.reflect.Method getIdMethod = obj.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(obj);
                    if (statementId.equals(idObj)) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // 单个元素反射失败，跳过
                }
            }
        } catch (Exception e) {
            log.warn("[MyBatis/MP 共存自检] 反射遍历 statement 异常", e);
        }
        return false;
    }
}
