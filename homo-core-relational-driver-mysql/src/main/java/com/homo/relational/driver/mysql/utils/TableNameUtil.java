package com.homo.relational.driver.mysql.utils;

import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.origin.ArrayUtil;
import com.homo.relational.base.SchemaInfoCoordinator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

@UtilityClass
@Slf4j
public class TableNameUtil {
    public SqlIdentifier getTableName(Class<?> domainType,Object[] args){
        TableSchema tableSchema = SchemaInfoCoordinator.getTable(domainType);
        Assert.notNull(tableSchema, "table must be defined, add @HomoTable in domain class!");
        if (ArrayUtils.isEmpty(args)){
            return SqlIdentifier.quoted(tableSchema.getIdentifier().getText());
        }
        Assert.notNull(tableSchema.getDivideStrategy(), "table must has nameStrategy in domain class!");
        return SqlIdentifier.quoted(tableSchema.getDivideStrategy().genTableName(tableSchema.getIdentifier().getText(),args));
    }

    public static SqlIdentifier getTableName(TableSchema table, Object[] args) {
        Assert.notNull(table, "table must be defined, add @TpfTable in domain class!");
        if (ArrayUtil.isEmpty(args)) {
            return SqlIdentifier.quoted(table.getIdentifier().getText());
        }
        Assert.notNull(table.getDivideStrategy(), "table must has nameStragry in domain class!");
        return SqlIdentifier.quoted(table.getDivideStrategy().genTableName(table.getTableName(), args));
    }
}
