package com.homo.core.redis.factory;

import com.homo.core.common.config.ConfigDriver;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class RedisInfoHolder {

    @Autowired
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

    private String redisType;

    public void load(String publicRedisNs) {
        redisType = configDriver.getProperty(publicRedisNs,"redis.type","");
        dataBase = configDriver.getProperty(publicRedisNs,"redis.dataBase",0);
        timeOutMs = configDriver.getProperty(publicRedisNs,"timeOutMs", 300000);
        maxTotal = configDriver.getProperty(publicRedisNs,"maxTotal", 100);
        maxIdle = configDriver.getProperty(publicRedisNs,"maxIdle", 10);
        minIdel = configDriver.getProperty(publicRedisNs,"minIdle", 10);
        maxWaitMillis = configDriver.getProperty(publicRedisNs,"maxWaitMillis", -1);
        testOnBorrow = configDriver.getProperty(publicRedisNs,"testOnBorrow", false);
        soTimeOut = configDriver.getProperty(publicRedisNs,"soTimeOut", 30000);
        maxAttemps = configDriver.getProperty(publicRedisNs,"maxAttemps", 5);
    }
}
