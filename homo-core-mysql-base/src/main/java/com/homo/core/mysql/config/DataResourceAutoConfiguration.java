package com.homo.core.mysql.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.homo.core.configurable.mysql.MysqlNamespaceProperties;
import com.homo.core.configurable.mysql.MysqlProperties;
import com.homo.core.mysql.datasource.DynamicDataSource;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;


@AutoConfiguration
@Import({MysqlNamespaceProperties.class,MysqlProperties.class})
@Slf4j
public class DataResourceAutoConfiguration {
    @Autowired
    private MysqlNamespaceProperties mysqlNamespaceProperties;
    @Autowired
    private MysqlProperties mysqlProperties;
    @Bean("mysqlInfoHolder")
    @DependsOn("configDriver")
    public MysqlInfoHolder mysqlInfoHolder(ConfigDriver configDriver){
        String publicNamespace = mysqlNamespaceProperties.getPublicNamespace();
        String privateNamespace = mysqlNamespaceProperties.getPrivateNamespace();
        log.info("register bean mysqlInfoHolder publicNamespace {} and privateNamespace {}",publicNamespace,privateNamespace);
        configDriver.registerNamespace(publicNamespace);
        configDriver.registerNamespace(privateNamespace);
        return new MysqlInfoHolder(publicNamespace,privateNamespace,configDriver,mysqlProperties);
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
