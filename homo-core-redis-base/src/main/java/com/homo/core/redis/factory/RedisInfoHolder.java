package com.homo.core.redis.factory;

import com.homo.core.configurable.redis.RedisProperties;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class RedisInfoHolder {

    private ConfigDriver configDriver;
    private RedisProperties properties;

    private String publicRedisNs;

    private String privateRedisNs;

    private Integer dataBase;

    private Integer timeOutMs;

    private Integer maxTotal;

    private Integer maxIdle;

    private Integer minIdel;

    private Integer maxWaitMillis;

    private boolean testOnBorrow;

    private Integer soTimeOut;

    private Integer maxAttemps;

    private Integer expireTime;

    private String url;

    private int  port;

    private String auth;

    private String proxyDir;


    public RedisInfoHolder(String publicRedisNs, String privateRedisNs, ConfigDriver configDriver, RedisProperties properties) {
        this.publicRedisNs = publicRedisNs;
        this.privateRedisNs = privateRedisNs;
        this.configDriver = configDriver;
        this.properties = properties;
        load();
        this.configDriver.listenerNamespace(publicRedisNs, configChangeEvent -> {
            load();
        });
    }

    public void load() {
        dataBase = configDriver.getIntProperty(publicRedisNs,"homo.redis.dataBase", properties.dataBase);
        timeOutMs = configDriver.getIntProperty(publicRedisNs,"homo.redis.timeOutMs", properties.timeOutMs);
        maxTotal = configDriver.getIntProperty(publicRedisNs,"homo.redis.maxTotal", properties.maxTotal);
        maxIdle = configDriver.getIntProperty(publicRedisNs,"homo.redis.maxIdle", properties.maxIdle);
        minIdel = configDriver.getIntProperty(publicRedisNs,"homo.redis.minIdle", properties.minIdle);
        maxWaitMillis = configDriver.getIntProperty(publicRedisNs,"homo.redis.maxWaitMillis", properties.maxWaitMillis);
        testOnBorrow = configDriver.getBoolProperty(publicRedisNs,"homo.redis.testOnBorrow", properties.testOnBorrow);
        soTimeOut = configDriver.getIntProperty(publicRedisNs,"homo.redis.soTimeOut", properties.soTimeOut);
        maxAttemps = configDriver.getIntProperty(publicRedisNs,"homo.redis.maxAttemps", properties.maxAttemps);
        expireTime = configDriver.getIntProperty(publicRedisNs,"homo.redis.expire",properties.expireTime);

        url = configDriver.getProperty(privateRedisNs,"homo.redis.url","");
        port = configDriver.getIntProperty(privateRedisNs,"homo.redis.port",null);
        auth = configDriver.getProperty(privateRedisNs,"homo.redis.auth","");
        proxyDir = configDriver.getProperty(privateRedisNs,"homo.redis.proxyDir","");
    }
}
