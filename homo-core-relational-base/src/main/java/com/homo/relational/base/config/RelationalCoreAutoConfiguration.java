package com.homo.relational.base.config;

import com.homo.core.configurable.relational.RelationalProperties;
import com.homo.core.facade.relational.operation.RelationalTemplate;
import com.homo.core.facade.relational.schema.SchemaAccessDialect;
import com.homo.relational.base.ScanningCoordinator;
import com.homo.relational.base.SchemaAutoCreateCoordinator;
import com.homo.relational.base.SchemaInfoCoordinator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import(RelationalProperties.class)
public class RelationalCoreAutoConfiguration {
    @Bean("scanningCoordinator")
    public ScanningCoordinator scanningCoordinator() {
        log.info("register bean scanningCoordinator");
        return new ScanningCoordinator();
    }

    @Bean("schemaCoordinator")
    @DependsOn("scanningCoordinator")
    public SchemaInfoCoordinator schemaCoordinator() {
        log.info("register bean schemaCoordinator");
        return new SchemaInfoCoordinator();
    }

    //返回map注入失败了
//    @Bean
//    @DependsOn("schemaCoordinator")
//    public Map<String,SchemaAutoCreateCoordinator> schemaAutoCreateCoordinator(Set<SchemaAccessDialect> dialects, Set<RelationalTemplate> templates) {
//        log.info("register bean schemaAutoCreateCoordinator");
//        Map<String,SchemaAutoCreateCoordinator> map = new HashMap();
//        for (SchemaAccessDialect dialect : dialects) {
//            templates.stream().filter(item->item.driverName().equals(dialect.driverName())).findFirst().ifPresent(template->{
//                SchemaAutoCreateCoordinator coordinator = new SchemaAutoCreateCoordinator(dialect, template);
//                map.put(dialect.driverName()+"schemaAutoCreateCoordinator",coordinator);
//            });
//        }
//        return map;
//    }

    @Bean
    @DependsOn("schemaCoordinator")
    public SchemaAutoCreateCoordinator schemaAutoCreateCoordinator(SchemaAccessDialect dialect, RelationalTemplate template) {
        log.info("register bean schemaAutoCreateCoordinator");
        SchemaAutoCreateCoordinator coordinator = new SchemaAutoCreateCoordinator(dialect, template);
        return coordinator;
    }
}
