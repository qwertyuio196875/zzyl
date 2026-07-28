package com.zzyl.mpdemo;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.zzyl.framework.mpdemo.DemoUser;
import com.zzyl.framework.mpdemo.DemoUserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis-Plus BaseMapper 极简验证（轻量版）。
 *
 * 不走 @SpringBootTest 完整上下文，改用手动装配 MyBatis-Plus SqlSessionFactory，
 * 避免 SecurityConfig.requestMappingHandlerMapping 在 WebEnvironment.NONE 下不可用的问题。
 *
 * 验证 BaseMapper 4 个核心 CRUD：
 *   1) insert(entity) - 插入并自动回填自增 ID
 *   2) selectById(id) - 按主键查
 *   3) updateById(entity) - 按主键更新
 *   4) deleteById(id) - 按主键删除
 */
@DisplayName("MyBatis-Plus BaseMapper 极简验证")
class DemoUserMapperTest {

    /** 由调用方通过 System.setProperty 传入，默认连接本地 zzyl 库 */
    private static final String JDBC_URL = System.getProperty("jdbc.url", "jdbc:mysql://localhost:3306/zzyl");
    private static final String JDBC_USER = System.getProperty("jdbc.user", "root");
    private static final String JDBC_PWD  = System.getProperty("jdbc.password", "1234");

    private static DruidDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;
    private static DemoUserMapper demoUserMapper;

    @BeforeAll
    static void setupAll() throws Exception {
        // 1. 建表（自包含）
        dataSource = buildDataSource();
        createTable();

        // 2. 手工装配 MyBatis-Plus SqlSessionFactory
        sqlSessionFactory = buildSqlSessionFactory();
        demoUserMapper = sqlSessionFactory.openSession(true).getMapper(DemoUserMapper.class);
    }

    @AfterAll
    static void teardownAll() {
        dropTable();
        if (sqlSessionFactory != null) {
            sqlSessionFactory.openSession().close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    // ==================== 辅助方法 ====================

    private static DruidDataSource buildDataSource() throws Exception {
        Properties props = new Properties();
        props.setProperty(DruidDataSourceFactory.PROP_URL, JDBC_URL);
        props.setProperty(DruidDataSourceFactory.PROP_USERNAME, JDBC_USER);
        props.setProperty(DruidDataSourceFactory.PROP_PASSWORD, JDBC_PWD);
        props.setProperty(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.mysql.cj.jdbc.Driver");
        props.setProperty(DruidDataSourceFactory.PROP_INITIALSIZE, "1");
        props.setProperty(DruidDataSourceFactory.PROP_MAXACTIVE, "5");
        return (DruidDataSource) DruidDataSourceFactory.createDataSource(props);
    }

    private static SqlSessionFactory buildSqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 下划线转驼峰
        MybatisConfiguration cfg = new MybatisConfiguration();
        cfg.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(cfg);

        // 扫描 entity 包（DemoUser），MyBatis-Plus 会自动处理继承 BaseMapper 的接口
        factoryBean.setTypeAliasesPackage("com.zzyl.framework.mpdemo");

        // 先获取工厂（内部完成 MyBatis-Plus 初始化）
        SqlSessionFactory factory = (SqlSessionFactory) factoryBean.getObject();

        // 关键：MybatisSqlSessionFactoryBuilder.build() 内部创建了新的 MybatisConfiguration，
        // 原先 cfg.addMapper() 的注册没有传递过去。
        // 所以这里从工厂的 configuration 重新注册 DemoUserMapper
        ((MybatisConfiguration) factory.getConfiguration()).addMapper(DemoUserMapper.class);

        return factory;
    }

    private static void createTable() throws Exception {
        execute("DROP TABLE IF EXISTS mp_demo_user");
        execute("CREATE TABLE mp_demo_user (" +
                "  id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                "  name        VARCHAR(64)  NOT NULL                COMMENT '姓名'," +
                "  age         INT                                   COMMENT '年龄'," +
                "  email       VARCHAR(128)                          COMMENT '邮箱'," +
                "  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MyBatis-Plus 阶段 ② 验证表'");
    }

    private static void dropTable() {
        try {
            execute("DROP TABLE IF EXISTS mp_demo_user");
        } catch (Exception ignored) {}
    }

    private static void execute(String sql) throws Exception {
        try (var c = dataSource.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private static void cleanTable() throws Exception {
        execute("DELETE FROM mp_demo_user");
    }

    // ==================== 测试方法 ====================

    @BeforeEach
    void cleanBefore() throws Exception {
        cleanTable();
    }

    @AfterEach
    void cleanAfter() throws Exception {
        cleanTable();
    }

    @Test
    @DisplayName("insert → selectById → updateById → deleteById 全流程")
    void testFullCrud() {
        // 1) insert
        DemoUser u = new DemoUser();
        u.setName("alice");
        u.setAge(30);
        u.setEmail("alice@example.com");
        int inserted = demoUserMapper.insert(u);
        assertEquals(1, inserted, "insert 应影响 1 行");
        assertNotNull(u.getId(), "insert 后实体应自动回填自增 ID");
        Long id = u.getId();

        // 2) selectById
        DemoUser found = demoUserMapper.selectById(id);
        assertNotNull(found, "selectById 应能查到刚插入的记录");
        assertEquals("alice", found.getName());
        assertEquals(30, found.getAge());
        assertEquals("alice@example.com", found.getEmail());
        assertNotNull(found.getCreateTime(), "create_time 应被 DEFAULT CURRENT_TIMESTAMP 自动填充");

        // 3) updateById
        u.setAge(31);
        u.setEmail("alice2@example.com");
        int updated = demoUserMapper.updateById(u);
        assertEquals(1, updated, "updateById 应影响 1 行");
        DemoUser updated2 = demoUserMapper.selectById(id);
        assertEquals(31, updated2.getAge());
        assertEquals("alice2@example.com", updated2.getEmail());

        // 4) selectList / selectCount 简单验证
        List<DemoUser> all = demoUserMapper.selectList(null);
        assertEquals(1, all.size(), "应只有 1 条记录");

        Long count = demoUserMapper.selectCount(null);
        assertEquals(1L, count.longValue());

        // 5) deleteById
        int deleted = demoUserMapper.deleteById(id);
        assertEquals(1, deleted, "deleteById 应影响 1 行");
        DemoUser afterDelete = demoUserMapper.selectById(id);
        assertNull(afterDelete, "删除后 selectById 应返回 null");
    }

    @Test
    @DisplayName("批量插入 + selectBatchIds")
    void testBatchInsert() {
        DemoUser u1 = new DemoUser();
        u1.setName("bob");
        u1.setAge(25);
        demoUserMapper.insert(u1);

        DemoUser u2 = new DemoUser();
        u2.setName("carol");
        u2.setAge(28);
        demoUserMapper.insert(u2);

        List<DemoUser> all = demoUserMapper.selectList(null);
        assertEquals(2, all.size());

        List<Long> ids = List.of(u1.getId(), u2.getId());
        List<DemoUser> batch = demoUserMapper.selectBatchIds(ids);
        assertEquals(2, batch.size());
    }
}
