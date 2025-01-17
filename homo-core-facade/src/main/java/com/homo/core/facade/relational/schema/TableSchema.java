package com.homo.core.facade.relational.schema;

import com.homo.core.facade.relational.mapping.HomoIndex;
import com.homo.core.facade.relational.mapping.HomoTableDivideStrategy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class TableSchema {
    private String tableName;
    private IdentifierSchema identifier;
    private final Map<String, ColumnSchema> columns = new LinkedHashMap<>();
    private Class<?> domainClass;
    private boolean generate;
    private HomoIndex[] indexes;
    private HomoTableDivideStrategy divideStrategy;
    private ColumnSchema primaryColumn;
    private String driverName;

    public TableSchema(String tableName, String driverName,IdentifierSchema identifier, Class<?> domainClass, HomoIndex[] indexes,
                       boolean generate, Class<? extends HomoTableDivideStrategy> divideStrategy, List<ColumnSchema> columns)
            throws InstantiationException, IllegalAccessException {
        this.tableName = tableName;
        this.identifier = identifier;
        this.domainClass = domainClass;
        this.indexes = indexes;
        this.generate = generate;
        this.driverName = driverName;
        this.divideStrategy = divideStrategy.newInstance();
        for (ColumnSchema column : columns) {
            if (column.isPrimaryKey()) {
                if (primaryColumn != null) {
                    log.warn("Primary key repeated in table {} replace old {} new {}", tableName, primaryColumn.getName(), column.getName());
                }
                primaryColumn = column;
            }
            this.columns.put(column.getRawName(), column);
        }
    }

    public void addColumn(ColumnSchema column) {
        columns.put(column.getRawName(), column);
    }
    public String getColumnName(String rawName) {
        if (!columns.containsKey(rawName)) {
            return rawName;
        }
        return columns.get(rawName).getName();
    }

    public boolean hasColumn(String reference) {
        return columns.containsKey(reference);
    }
}
