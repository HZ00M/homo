package com.homo.core.redis.factory;

import com.homo.core.common.apollo.ConfigDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

;

@Data
@Slf4j
public class RedisInfoHolder {

    ConfigDriver configDriver;

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


    public RedisInfoHolder(String publicRedisNs, String privateRedisNs, ConfigDriver configDriver) {
        this.publicRedisNs = publicRedisNs;
        this.privateRedisNs = privateRedisNs;
        this.configDriver = configDriver;
        load();
        this.configDriver.listenerNamespace(publicRedisNs, configChangeEvent -> {
            load();
        });
    }

    public void load() {
        dataBase = configDriver.getIntProperty(publicRedisNs,"redis.dataBase",0);
        timeOutMs = configDriver.getIntProperty(publicRedisNs,"timeOutMs", 300000);
        maxTotal = configDriver.getIntProperty(publicRedisNs,"maxTotal", 100);
        maxIdle = configDriver.getIntProperty(publicRedisNs,"maxIdle", 10);
        minIdel = configDriver.getIntProperty(publicRedisNs,"minIdle", 10);
        maxWaitMillis = configDriver.getIntProperty(publicRedisNs,"maxWaitMillis", -1);
        testOnBorrow = configDriver.getBoolProperty(publicRedisNs,"testOnBorrow", false);
        soTimeOut = configDriver.getIntProperty(publicRedisNs,"soTimeOut", 30000);
        maxAttemps = configDriver.getIntProperty(publicRedisNs,"maxAttemps", 5);
        expireTime = configDriver.getIntProperty(publicRedisNs,"expire",86400);

        url = configDriver.getProperty(privateRedisNs,"homo.redis.url","");
        port = configDriver.getIntProperty(privateRedisNs,"homo.redis.port",null);
        auth = configDriver.getProperty(privateRedisNs,"homo.redis.auth","");
        proxyDir = configDriver.getProperty(privateRedisNs,"homo.redis.proxyDir","");
    }
}
