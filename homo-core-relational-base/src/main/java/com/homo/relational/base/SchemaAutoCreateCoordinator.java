package com.homo.relational.base;

import com.homo.core.facade.relational.operation.RelationalTemplate;
import com.homo.core.facade.relational.schema.SchemaAccessDialect;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.module.ServiceModule;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SchemaAutoCreateCoordinator implements ServiceModule {

    private RelationalTemplate template;
    private SchemaAccessDialect dialect;

    private Set<String> existingTables = new HashSet<>();

    public SchemaAutoCreateCoordinator(SchemaAccessDialect dialect,RelationalTemplate template) {
        this.dialect = dialect;
        this.template = template;
    }

    @Override
    public void moduleInit() {
        loadExistingTables();
        for (TableSchema tableSchema : SchemaInfoCoordinator.getTableList()) {
            if (tableSchema.isGenerate()) {
                createTable(tableSchema).block();
            }
        }
    }

    public void loadExistingTables() {
        dialect.fetchExistTables()
                        .consumerValue(tables->{
                            existingTables.addAll(tables);
                            log.info("Load existing tables: {}", tables);
                        }).block();
    }

    public Homo<Boolean> createTable(TableSchema tableSchema) {
        if (tableExists(tableSchema)) {
            return Homo.result(true);
        }
        return dialect.createTable(tableSchema);
    }

    public boolean tableExists(TableSchema tableSchema) {
        return existingTables.contains(tableSchema);
    }

}
