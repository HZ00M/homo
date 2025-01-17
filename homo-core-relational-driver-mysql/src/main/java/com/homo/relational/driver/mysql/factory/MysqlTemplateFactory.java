package com.homo.relational.driver.mysql.factory;

import com.homo.core.configurable.relational.RelationalMysqlProperties;
import org.springframework.data.r2dbc.MysqlRelationalTemplate;
import com.homo.relational.driver.mysql.mapping.HomoDataAccessStrategy;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.context.ApplicationContext;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.Duration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class MysqlTemplateFactory {
    ApplicationContext applicationContext;
    RelationalMysqlProperties properties;
    public MysqlTemplateFactory(RelationalMysqlProperties properties,ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    public MysqlRelationalTemplate createTemplate() {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(DRIVER, "pool")
                .option(PORT,properties.getPort())
                .option(PROTOCOL, properties.getDriver())
                .option(HOST, properties.getHost())
                .option(DATABASE, properties.getDatabase())
                .option(USER, properties.getUsername())
                .option(PASSWORD, properties.getPassword())
                .option(CONNECT_TIMEOUT, Duration.ofSeconds(properties.getTimeoutSecond()))
                .option(Option.valueOf("socketTimeout"), Duration.ofSeconds(4))// optional, default null, null means no timeout
                .build();
        ConnectionFactory connectionFactory = ConnectionFactories.get(options);
        return createTemplate(connectionFactory);

    }

    public MysqlRelationalTemplate createTemplate(ConnectionFactory connectionFactory) {
        CustomConversions customConversions = applicationContext.getBean(CustomConversions.class);
        HomoDataAccessStrategy homoDataAccessStrategy = new HomoDataAccessStrategy(MySqlDialect.INSTANCE, customConversions);
        DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);
        R2dbcEntityTemplate r2dbcEntityTemplate = new R2dbcEntityTemplate(databaseClient, homoDataAccessStrategy);
        MysqlRelationalTemplate mysqlTemplate = new MysqlRelationalTemplate(r2dbcEntityTemplate);
        return mysqlTemplate;
    }
}
