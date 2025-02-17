package com.homo.core.mysql.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.homo.core.configurable.mysql.MysqlProperties;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class MysqlInfoHolder {
    private ConfigDriver configDriver;

    private MysqlProperties mysqlProperties;

    private String publicMysqlNs;

    private String dbUrl;

    private String dbHost;

    private String dbParam;

    private String username;

    private String password;

    private String driverClassName;

    private int initialSize;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int timeBetweenEvictionRunsMillis;

    private int minEvictableIdleTimeMillis;

    private String validationQuery;

    private boolean testWhileIdle;

    private boolean testOnBorrow;

    private boolean testOnReturn;

    private boolean poolPreparedStatements;

    private String filters;

    private String privateMysqlNs;

    public MysqlInfoHolder(String publicMysqlNs, String privateMysqlNs, ConfigDriver configDriver, MysqlProperties mysqlProperties){
        this.publicMysqlNs = publicMysqlNs;
        this.privateMysqlNs = privateMysqlNs;
        this.configDriver = configDriver;
        this.mysqlProperties = mysqlProperties;
        load();
        configDriver.listenerNamespace(publicMysqlNs,configChangeEvent -> load());
    }

    private void load() {
        driverClassName = configDriver.getProperty(publicMysqlNs,"spring.datasource.driver-class-name", mysqlProperties.driverClassName);
        initialSize = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.initialSize", mysqlProperties.initialSize);
        minIdle = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.minIdle", mysqlProperties.minIdle);
        maxActive = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.maxActive", mysqlProperties.maxActive);
        maxWait = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.maxWait", mysqlProperties.maxWait);
        timeBetweenEvictionRunsMillis = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.timeBetweenEvictionRunsMillis", mysqlProperties.timeBetweenEvictionRunsMillis);
        minEvictableIdleTimeMillis = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.minEvictableIdleTimeMillis", mysqlProperties.minEvictableIdleTimeMillis);
        validationQuery = configDriver.getProperty(publicMysqlNs,"spring.datasource.validationQuery", mysqlProperties.validationQuery);
        testWhileIdle = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testWhileIdle", mysqlProperties.testWhileIdle);
        testOnBorrow = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testOnBorrow", mysqlProperties.testOnBorrow);
        testOnReturn = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testOnReturn", mysqlProperties.testOnReturn);
        poolPreparedStatements = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.poolPreparedStatements", mysqlProperties.poolPreparedStatements);
        filters = configDriver.getProperty(publicMysqlNs,"spring.datasource.filters", mysqlProperties.filters);
        log.info("mysql initPublicConfig {}", this);

        Config mysqlNsConfig = ConfigService.getConfig(privateMysqlNs);
        dbUrl = mysqlNsConfig.getProperty("homo.datasource.url", mysqlProperties.dbUrl);
        dbHost = mysqlNsConfig.getProperty("homo.datasource.host", mysqlProperties.dbHost);
        dbParam = mysqlNsConfig.getProperty("homo.datasource.param", mysqlProperties.dbParam);
        username = mysqlNsConfig.getProperty("homo.datasource.username", mysqlProperties.username);
        password = mysqlNsConfig.getProperty("homo.datasource.password", mysqlProperties.password);
        log.trace("user mysql: mysqlNS: {} userName: {}, password: {}", privateMysqlNs, username, password);
    }
}
