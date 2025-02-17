package com.homo.core.configurable.mysql;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

/**
 * 配置类：MysqlProperties
 * <p>
 * 该类用于加载 MySQL 数据源的相关配置，支持 Spring 配置文件 (application.properties 或 application.yml) 注入。
 * 通过 @Value 注解获取外部配置，并提供默认值。
 * </p>
 */
@Configurable
@Data
public class MysqlProperties {

    /**
     * 数据库连接 URL，示例：
     * jdbc:mysql://localhost:3306/db_name
     */
    @Value("${homo.datasource.url:}")
    public String dbUrl;

    /**
     * 数据库主机地址
     */
    @Value("${homo.datasource.host:}")
    public String dbHost;

    /**
     * 数据库连接附加参数，例如时区、编码等
     */
    @Value("${homo.datasource.param:}")
    public String dbParam;

    /**
     * 数据库用户名
     */
    @Value("${homo.datasource.username:}")
    public String username;

    /**
     * 数据库密码
     */
    @Value("${homo.datasource.password:}")
    public String password;

    /**
     * MySQL JDBC 驱动类名，默认使用 MySQL 8.0+ 的驱动
     */
    @Value("${homo.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    public String driverClassName; // 变量名 driverClassNam 可能有误，应为 driverClassName

    /**
     * 连接池初始化时的连接数，默认 5
     */
    @Value("${homo.datasource.initialSize:5}")
    public int initialSize;

    /**
     * 连接池最小空闲连接数，默认 5
     */
    @Value("${homo.datasource.minIdle:5}")
    public int minIdle;

    /**
     * 连接池最大活跃连接数，默认 20
     */
    @Value("${homo.datasource.maxActive:20}")
    public int maxActive;

    /**
     * 连接获取等待超时时间（毫秒），默认 60000ms（60秒）
     */
    @Value("${homo.datasource.maxWait:60000}")
    public int maxWait;

    /**
     * 连接池清理空闲连接的时间间隔（毫秒），默认 300000ms（5分钟）
     */
    @Value("${homo.datasource.timeBetweenEvictionRunsMillis:300000}")
    public int timeBetweenEvictionRunsMillis;

    /**
     * 连接最小空闲时间（毫秒），超时后会被回收，默认 60000ms（1分钟）
     */
    @Value("${homo.datasource.minEvictableIdleTimeMillis:60000}")
    public int minEvictableIdleTimeMillis;

    /**
     * 连接校验 SQL 语句，确保连接可用，默认 "SELECT 1 FROM DUAL"
     */
    @Value("${homo.datasource.validationQuery:SELECT 1 FROM DUAL}")
    public String validationQuery;

    /**
     * 是否在连接空闲时进行检查，默认 true
     */
    @Value("${homo.datasource.testWhileIdle:true}")
    public Boolean testWhileIdle;

    /**
     * 是否在获取连接时进行检查，默认 false
     */
    @Value("${homo.datasource.testOnBorrow:false}")
    public Boolean testOnBorrow;

    /**
     * 是否在归还连接时进行检查，默认 false
     */
    @Value("${homo.datasource.testOnReturn:false}")
    public Boolean testOnReturn;

    /**
     * 是否开启 PreparedStatement 预编译缓存，默认 true
     */
    @Value("${homo.datasource.poolPreparedStatements:true}")
    public Boolean poolPreparedStatements;

    /**
     * 连接池的 SQL 过滤器配置，默认 "stat,wall,log4j2"
     * - stat：SQL 监控统计
     * - wall：SQL 防火墙
     * - log4j2：日志记录
     */
    @Value("${homo.datasource.filters:stat,wall,log4j2}")
    public String filters;
}
