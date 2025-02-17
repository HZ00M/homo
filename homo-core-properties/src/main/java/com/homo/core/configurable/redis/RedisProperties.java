package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

/**
 * Redis 配置属性类
 * <p>
 * 该类用于读取 Redis 相关的配置信息，并通过 Spring 的 @Value 注解进行属性注入。
 * </p>
 */
@Configurable
@Data
public class RedisProperties {

    /**
     * Redis 服务器 URL（默认空）
     */
    @Value("${homo.redis.url:}")
    public String url;

    /**
     * Redis 端口号（默认 6379）
     */
    @Value("${homo.redis.port:6379}")
    public String port;

    /**
     * Redis 认证密码（默认空）
     */
    @Value("${homo.redis.auth:}")
    public String auth;

    /**
     * Redis 代理（默认空）
     */
    @Value("${homo.redis.proxyDir:}")
    public String proxyDir;

    /**
     * Redis 数据库索引（默认 0）
     */
    @Value("${homo.redis.dataBase:0}")
    public Integer dataBase;

    /**
     * Redis 连接超时时间（单位：毫秒，默认 300000）
     */
    @Value("${homo.redis.timeOutMs:300000}")
    public Integer timeOutMs;

    /**
     * 连接池最大连接数（默认 100）
     */
    @Value("${homo.redis.maxTotal:100}")
    public Integer maxTotal;

    /**
     * 连接池最大空闲连接数（默认 10）
     */
    @Value("${homo.redis.maxIdle:10}")
    public Integer maxIdle;

    /**
     * 连接池最小空闲连接数（默认 10）
     */
    @Value("${homo.redis.minIdle:10}")
    public Integer minIdle;

    /**
     * 连接池最大等待时间（单位：毫秒，默认 -1，表示无限等待）
     */
    @Value("${homo.redis.maxWaitMillis:-1}")
    public Integer maxWaitMillis;

    /**
     * 是否在借用连接时进行验证（默认 false）
     */
    @Value("${homo.redis.testOnBorrow:false}")
    public Boolean testOnBorrow;

    /**
     * Redis Socket 超时时间（单位：毫秒，默认 30000）
     */
    @Value("${homo.redis.soTimeOut:30000}")
    public Integer soTimeOut;

    /**
     * 最大重试次数（默认 5）
     */
    @Value("${homo.redis.maxAttemps:5}")
    public Integer maxAttemps;

    /**
     * Key 过期时间（单位：秒，默认 86400，即 24 小时）
     */
    @Value("${homo.redis.expire:86400}")
    public Integer expireTime;
}
