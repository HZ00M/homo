package com.homo.relational.driver.mysql;

import com.homo.core.configurable.relational.RelationalMysqlProperties;
import com.homo.core.facade.relational.mapping.HomoIndex;
import com.homo.core.facade.relational.schema.ColumnSchema;
import com.homo.core.facade.relational.schema.SchemaAccessDialect;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.MysqlRelationalTemplate;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
public class MysqlSchemaAccessDialect implements SchemaAccessDialect {
    @Autowired
    private MysqlRelationalTemplate relationalTemplate;
    @Autowired
    private RelationalMysqlProperties properties;
    private final TypeNames typeNames = new TypeNames();


    public MysqlSchemaAccessDialect(MysqlRelationalTemplate relationalTemplate, RelationalMysqlProperties properties) {
        this.relationalTemplate = relationalTemplate;
        typeNames.registerColumnType(Types.BIT, "bit");
        typeNames.registerColumnType(Types.BOOLEAN, "boolean");
        typeNames.registerColumnType(Types.TINYINT, "tinyint");
        typeNames.registerColumnType(Types.SMALLINT, "smallint");
        typeNames.registerColumnType(Types.INTEGER, "integer");
        typeNames.registerColumnType(Types.BIGINT, "bigint");
        typeNames.registerColumnType(Types.FLOAT, "float($p)");
        typeNames.registerColumnType(Types.DOUBLE, "double precision");
        typeNames.registerColumnType(Types.NUMERIC, "numeric($p,$s)");
        typeNames.registerColumnType(Types.DECIMAL, "decimal($p,$s)");
        typeNames.registerColumnType(Types.REAL, "real");
        typeNames.registerColumnType(Types.DATE, "date");
        typeNames.registerColumnType(Types.TIME, "time");
        typeNames.registerColumnType(Types.TIMESTAMP, "datetime");

        typeNames.registerColumnType(Types.VARBINARY, "bit varying($l)");
        typeNames.registerColumnType(Types.LONGVARBINARY, "bit varying($l)");
        typeNames.registerColumnType(Types.BLOB, "tinyblob", 255);
        typeNames.registerColumnType(Types.BLOB, "blob", 65535);
        typeNames.registerColumnType(Types.BLOB, "mediumblob", 16777215);
        typeNames.registerColumnType(Types.BLOB, "longblob", 4294967295L);

        typeNames.registerColumnType(Types.CHAR, "char($l)");
        typeNames.registerColumnType(Types.VARCHAR, "varchar($l)", 16383);
        typeNames.registerColumnType(Types.VARCHAR, "mediumtext", 4194304);
        typeNames.registerColumnType(Types.VARCHAR, "longtext", 4294967295L);
        typeNames.registerColumnType(Types.LONGVARCHAR, "varchar($l)");
        typeNames.registerColumnType(Types.CLOB, "clob");

        typeNames.registerColumnType(Types.NCHAR, "nchar($l)");
        typeNames.registerColumnType(Types.NVARCHAR, "nvarchar($l)");
        typeNames.registerColumnType(Types.LONGNVARCHAR, "nvarchar($l)");
        typeNames.registerColumnType(Types.NCLOB, "nclob");
        typeNames.registerColumnType(Types.JAVA_OBJECT, "longtext");

        typeNames.registerColumnTypeJava(Boolean.class.getTypeName(), Types.BOOLEAN);
        typeNames.registerColumnTypeJava(boolean.class.getTypeName(), Types.BOOLEAN);
        typeNames.registerColumnTypeJava(byte[].class.getTypeName(), Types.BLOB);
        typeNames.registerColumnTypeJava(BigDecimal.class.getTypeName(), Types.DECIMAL);
        typeNames.registerColumnTypeJava(BigInteger.class.getTypeName(), Types.DECIMAL);
        typeNames.registerColumnTypeJava(Byte.class.getTypeName(), Types.BIT);
        typeNames.registerColumnTypeJava(Character[].class.getTypeName(), Types.VARCHAR);
        typeNames.registerColumnTypeJava(char.class.getTypeName(), Types.CHAR);
        typeNames.registerColumnTypeJava(String.class.getTypeName(), Types.VARCHAR);
        typeNames.registerColumnTypeJava(char[].class.getTypeName(), Types.VARCHAR);
        typeNames.registerColumnTypeJava(int.class.getTypeName(), Types.INTEGER);
        typeNames.registerColumnTypeJava(Integer.class.getTypeName(), Types.INTEGER);
        typeNames.registerColumnTypeJava(long.class.getTypeName(), Types.BIGINT);
        typeNames.registerColumnTypeJava(Long.class.getTypeName(), Types.BIGINT);
        typeNames.registerColumnTypeJava(Float.class.getTypeName(), Types.FLOAT);
        typeNames.registerColumnTypeJava(float.class.getTypeName(), Types.FLOAT);
        typeNames.registerColumnTypeJava(Double.class.getTypeName(), Types.DOUBLE);
        typeNames.registerColumnTypeJava(Short.class.getTypeName(), Types.SMALLINT);
        typeNames.registerColumnTypeJava(short.class.getTypeName(), Types.SMALLINT);
        typeNames.registerColumnTypeJava(double.class.getTypeName(), Types.DOUBLE);
        typeNames.registerColumnTypeJava(List.class.getTypeName(), Types.JAVA_OBJECT);
        typeNames.registerColumnTypeJava(Map.class.getTypeName(), Types.JAVA_OBJECT);
        typeNames.registerColumnTypeJava(Set.class.getTypeName(), Types.JAVA_OBJECT);
        typeNames.registerColumnTypeJava(Object.class.getTypeName(), Types.JAVA_OBJECT);
        typeNames.registerColumnTypeJava(Date.class.getTypeName(), Types.DATE);
        typeNames.registerColumnTypeJava(Time.class.getTypeName(), Types.TIME);
        typeNames.registerColumnTypeJava(Timestamp.class.getTypeName(), Types.TIMESTAMP);
        typeNames.registerColumnTypeJava(LocalDateTime.class.getTypeName(), Types.TIMESTAMP);
        typeNames.registerColumnTypeJava(LocalDate.class.getTypeName(), Types.DATE);
        typeNames.registerColumnTypeJava(LocalTime.class.getTypeName(), Types.TIME);
    }

    @Override
    public Homo<Boolean> createTable(TableSchema table) {
        String createSql = sqlCreateString(table);
        return relationalTemplate.execute(createSql).rowsUpdated().nextValue(result -> {
            log.info("createTable using: {}, result: {}", createSql, result);
            return result >= 0;
        });
    }

    @Override
    public Homo<Set<String>> fetchExistTables() {
        String tablesQuerySql = sqlShowTableString(properties.getDatabase());
        return new Homo(
                relationalTemplate.execute(tablesQuerySql).all().map(result->{
                    log.debug("all tables: {}", result);
                    Set<String> tables = new HashSet<>();
                    //String tableKey = String.format("Tables_in_%s", db);
                    for (Map<String, Object> table : result) {
                        tables.add((String) table.get("TABLE_NAME"));
                    }
                    return tables;
                }).switchIfEmpty(Mono.just(new HashSet<>()))
        );
    }

    public String sqlShowTableString(String db) {
        return String.format("select * from information_schema.`TABLES` where  table_schema='%s'", db);
    }

    public String sqlCreateString(TableSchema tableSchema) {
        StringBuilder buf = new StringBuilder("CREATE TABLE IF NOT EXISTS")
                .append(' ')
                .append(tableSchema.getIdentifier().getText())
                .append(" (");

        // Try to find out the name of the primary key to create it as identity if the IdentityGenerator is used
        String pkname = null;
        if (tableSchema.getPrimaryColumn()!= null && tableSchema.getPrimaryColumn().isAutoGenerate()) {
            pkname = tableSchema.getPrimaryColumn().getQuotedName();
        }
        Iterator<ColumnSchema> iter = tableSchema.getColumns().values().iterator();
        boolean first = true;
        while (iter.hasNext()) {
            ColumnSchema col = iter.next();
            if (!first) {
                buf.append(", ");
            }
            first = false;
            String colName = col.getQuotedName();
            buf.append(colName).append(' ');
            String typeName = col.getTypeName();
            if (col.isJson()) {
                typeName = Object.class.getTypeName();
            }
            if (col.getQuotedName().equals(pkname)) {
                // to support dialects that have their own identity data type
                buf.append(typeNames.getTypeName(typeNames.get(typeName), col.getLength(), col.getPrecision(), col.getScale()));
                buf.append(' ').append(getIdentityColumnString(typeNames.get(typeName)));
            } else {
                buf.append(typeNames.getTypeName(typeNames.get(typeName), col.getLength(), col.getPrecision(), col.getScale()));
                if (col.isNullable()) {
                    buf.append(getNullColumnString());
                } else {
                    buf.append(" not null");
                }
            }
        }
        if (tableSchema.getPrimaryColumn()!=null && tableSchema.getPrimaryColumn().isAutoGenerate()) {
            buf.append(", ").append(sqlConstraintPrimaryString(tableSchema.getPrimaryColumn()));
        }
        if (tableSchema.getIndexes() != null && tableSchema.getIndexes().length > 0) {
            buf.append(", ").append(sqlConstraintIndexString(tableSchema.getIndexes()));
        }

        buf.append(')');
        return buf.append(getTableTypeString("ENGINE")).toString();
    }
    String getIdentityColumnString(int type) {
        if (type != Types.BIGINT && type != Types.INTEGER) {
            return "not null";
        }
        return "not null auto_increment";
    }

    public String getNullColumnString() {
        return "";
    }

    public String sqlConstraintPrimaryString(ColumnSchema primaryColumn) {
        StringBuilder buf = new StringBuilder("primary key (");
        buf.append(primaryColumn.getName());
        return buf.append(')').toString();
    }

    public String getTableTypeString(String engineKeyword) {
        return String.format( " %s=InnoDB", engineKeyword );
    }

    public String sqlConstraintIndexString(HomoIndex[] indices) {
        StringBuilder buf = new StringBuilder();
        Iterator<HomoIndex> indexIterator = Arrays.stream(indices).iterator();
        while (indexIterator.hasNext()) {
            HomoIndex index = indexIterator.next();
            if (index.indexType() == HomoIndex.IndexType.UNIQUE) {
                buf.append("UNIQUE");
            }
            buf.append(" KEY ").append(constrainIndexName(index)).append("(");
            Iterator<String> column = Arrays.stream(index.columns()).iterator();
            while (column.hasNext()) {
                buf.append("`").append(column.next()).append("`");
                if (column.hasNext()) {
                    buf.append(",");
                }
            }
            buf.append(")");
            if (indexIterator.hasNext()) {
                buf.append(",");
            }
        }
        return buf.toString();
    }

    private String constrainIndexName(HomoIndex index) {
        if (StringUtils.hasText(index.name())) {
            return index.name();
        }
        StringBuilder buf = new StringBuilder();
        if (index.indexType() == HomoIndex.IndexType.UNIQUE) {
            buf.append("uk_");
        } else {
            buf.append("nr_");
        }
        Iterator<String> it = Arrays.stream(index.columns()).iterator();
        while (it.hasNext()) {
            buf.append(it.next());
            if (it.hasNext()) {
                buf.append("_");
            }
        }
        return buf.toString();
    }
}
