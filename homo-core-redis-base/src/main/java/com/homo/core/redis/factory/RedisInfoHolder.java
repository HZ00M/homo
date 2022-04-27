package com.homo.core.redis.factory;

import com.homo.core.common.config.ConfigDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RedisInfoHolder {

    private String publicRedisNs;

    ConfigDriver configDriver;

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


    public RedisInfoHolder(ConfigDriver configDriver,String publicRedisNs) {
        this.configDriver =configDriver;
        this.publicRedisNs = publicRedisNs;
        load(publicRedisNs);
        configDriver.listenerNamespace(publicRedisNs,configChangeEvent -> {
            load(publicRedisNs);
        });
    }

    public void load(String publicRedisNs) {
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

    }
}
