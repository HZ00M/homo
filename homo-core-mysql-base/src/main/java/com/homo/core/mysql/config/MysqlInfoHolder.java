package com.homo.core.mysql.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

;

@Data
@Slf4j
public class MysqlInfoHolder {
    private ConfigDriver configDriver;

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

    public MysqlInfoHolder(String publicMysqlNs,String privateMysqlNs,ConfigDriver configDriver){
        this.publicMysqlNs = publicMysqlNs;
        this.privateMysqlNs = privateMysqlNs;
        this.configDriver = configDriver;
        load();
        configDriver.listenerNamespace(publicMysqlNs,configChangeEvent -> load());
    }

    private void load() {
        driverClassName = configDriver.getProperty(publicMysqlNs,"spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        initialSize = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.initialSize", 5);
        minIdle = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.minIdle", 5);
        maxActive = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.maxActive", 20);
        maxWait = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.maxWait", 60000);
        timeBetweenEvictionRunsMillis = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.timeBetweenEvictionRunsMillis", 300000);
        minEvictableIdleTimeMillis = configDriver.getIntProperty(publicMysqlNs,"spring.datasource.minEvictableIdleTimeMillis", 60000);
        validationQuery = configDriver.getProperty(publicMysqlNs,"spring.datasource.validationQuery", "SELECT 1 FROM DUAL");
        testWhileIdle = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testWhileIdle", true);
        testOnBorrow = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testOnBorrow", false);
        testOnReturn = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.testOnReturn", false);
        poolPreparedStatements = configDriver.getBoolProperty(publicMysqlNs,"spring.datasource.poolPreparedStatements", true);
        filters = configDriver.getProperty(publicMysqlNs,"spring.datasource.filters", "stat,wall,log4j2");
        log.info("mysql initPublicConfig {}", this);

        Config mysqlNsConfig = ConfigService.getConfig(privateMysqlNs);
        dbUrl = mysqlNsConfig.getProperty("homo.datasource.url", "");
        dbHost = mysqlNsConfig.getProperty("homo.datasource.host", "");
        dbParam = mysqlNsConfig.getProperty("homo.datasource.param", "");
        username = mysqlNsConfig.getProperty("homo.datasource.username", "");
        password = mysqlNsConfig.getProperty("homo.datasource.password", "");
        log.trace("user mysql: mysqlNS: {} userName: {}, password: {}", privateMysqlNs, username, password);
    }
}
