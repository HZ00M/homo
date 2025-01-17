package com.homo.relational.driver.mysql.config;

import com.homo.core.configurable.relational.RelationalMysqlProperties;
import org.springframework.data.r2dbc.MysqlRelationalTemplate;
import com.homo.relational.driver.mysql.MysqlSchemaAccessDialect;
import com.homo.relational.driver.mysql.factory.MysqlTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@Slf4j
@Import(RelationalMysqlProperties.class)
public class RelationalMysqlAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private RelationalMysqlProperties mysqlProperties;

    @Bean
    R2dbcCustomConversions customConversions(List<GenericConverter> genericConverters, List<Converter<?, ?>> converters) {
        List<Object> allConverts = new ArrayList<>(genericConverters);
        allConverts.addAll(converters);
        log.info("register bean customConversions");
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, allConverts);
    }

    @Bean
    MysqlTemplateFactory mysqlTemplateFactory() {
        log.info("register bean mysqlTemplateFactory");
        return new MysqlTemplateFactory(mysqlProperties,applicationContext);
    }

    @Bean("homoMysqlTemplate")
    public MysqlRelationalTemplate homoMysqlTemplate(MysqlTemplateFactory mysqlTemplateFactory) {
        log.info("register bean homoMysqlTemplate");
        return mysqlTemplateFactory.createTemplate();
    }

    @Bean
    public MysqlSchemaAccessDialect homoMysqlSchemaAccessDialect(MysqlRelationalTemplate homoMysqlTemplate){
        log.info("register bean homoMysqlSchemaAccessDialect");
        return new MysqlSchemaAccessDialect(homoMysqlTemplate, mysqlProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
