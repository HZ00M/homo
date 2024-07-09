package com.homo.core.mysql.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.homo.core.mysql.datasource.DynamicDataSource;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

;

@AutoConfiguration
@Slf4j
public class DataResourceAutoConfiguration {
    @Value("${mysql.public.namespace:homo_mysql_config}")
    private String publicNamespace;
    @Value("${mysql.private.namespace:mysql-connect-info}")
    private String privateNamespace;

    @Bean("mysqlInfoHolder")
    @DependsOn("configDriver")
    public MysqlInfoHolder mysqlInfoHolder(ConfigDriver configDriver){
        log.info("register bean mysqlInfoHolder");
        configDriver.registerNamespace(publicNamespace);
        configDriver.registerNamespace(privateNamespace);
        return new MysqlInfoHolder(publicNamespace,privateNamespace,configDriver);
    }

    @Bean("master")
    @Primary
    public DataSource masterDataSource(MysqlInfoHolder mysqlInfoHolder){
        log.info("register bean masterDataSource");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(mysqlInfoHolder.getDbUrl());
        dataSource.setUsername(mysqlInfoHolder.getUsername());
        dataSource.setPassword(mysqlInfoHolder.getPassword());
        dataSource.setDriverClassName(mysqlInfoHolder.getDriverClassName());
        dataSource.setInitialSize(mysqlInfoHolder.getInitialSize());
        dataSource.setMinIdle(mysqlInfoHolder.getMinIdle());
        dataSource.setMaxActive(mysqlInfoHolder.getMaxActive());
        dataSource.setMaxWait(mysqlInfoHolder.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(mysqlInfoHolder.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(mysqlInfoHolder.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(mysqlInfoHolder.getValidationQuery());
        dataSource.setTestWhileIdle(mysqlInfoHolder.isTestWhileIdle());
        dataSource.setTestOnBorrow(mysqlInfoHolder.isTestOnBorrow());
        dataSource.setTestOnReturn(mysqlInfoHolder.isTestOnReturn());
        dataSource.setPoolPreparedStatements(mysqlInfoHolder.isPoolPreparedStatements());
        try {
            dataSource.setFilters(mysqlInfoHolder.getFilters());
        }catch (SQLException e){
            log.error("druid configuration initialization filter", e);
        }
        return dataSource;
    }

    @Bean("dynamicDataSource")
    @DependsOn(value = {"master"})
    public DynamicDataSource dynamicDataSource(Map<String,DataSource> dataSourceMap){
        log.info("register bean dynamicDataSource");
        DynamicDataSource dynamicDataSource = new DynamicDataSource(dataSourceMap);
        return dynamicDataSource;
    }
}
