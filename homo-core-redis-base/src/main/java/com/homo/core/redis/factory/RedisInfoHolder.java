package com.homo.core.redis.factory;

import com.homo.core.common.config.ConfigDriver;
import lombok.Data;

@Data
public class RedisInfoHolder {

    ConfigDriver configDriver;

    private Integer timeOutMs;

    private Integer maxTotal;

    private Integer maxIdle;

    private Integer minIdel;

    private Integer maxWaitMillis;

    private boolean testOnBorrow;

    private Integer soTimeOut;

    private Integer maxAttemps;

    private Integer dataBase;



    public RedisInfoHolder(ConfigDriver configDriver) {
        this.configDriver =configDriver;
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
    }
}
