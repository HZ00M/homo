package org.springframework.data.r2dbc.core;

import com.homo.core.facade.relational.operation.InsertOperation;
import com.homo.core.facade.relational.schema.ColumnSchema;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.rector.Homo;
import com.homo.relational.base.SchemaInfoCoordinator;
import com.homo.relational.driver.mysql.utils.MapperUtil;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.r2dbc.core.PreparedOperation;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Slf4j
public  class InsertIgnoreSpecAdaptor<T> implements InsertOperation.InsertSpec<T> {
    private final R2dbcEntityTemplate template;
    private final Class<T> domainType;
    private final Object[] args;

    @Override
    public Homo<T> value(T obj) {
        log.debug("insert ignore using value start: {}", obj);
        TableSchema tableSchema = SchemaInfoCoordinator.getTable(domainType);
        ColumnSchema primaryColumn = tableSchema.getPrimaryColumn();
        if (primaryColumn.isAutoGenerate() && primaryColumn.getTypeName().equals("java.lang.String")) {                // 使用的是 String 类型的唯一id, 并且设置了自动计算
            // 使用的是 String 类型的唯一id, 并且设置了自动生成
            String id = UUID.randomUUID().toString();
            try {
                if (primaryColumn.getReadMethod().invoke(obj) == null) {
                    primaryColumn.getWriteMethod().invoke(obj, id);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                return Homo.error(e);
            }
        }
        OutboundRow outboundRow = template.getDataAccessStrategy().getOutboundRow(obj);
        StatementMapper statementMapper = template.getDataAccessStrategy().getStatementMapper();
        StatementMapper.InsertSpec insertSpec = statementMapper.createInsert(tableSchema.getTableName());
        for (SqlIdentifier column : outboundRow.keySet()) {
            Parameter parameterValue = outboundRow.get(column);
            if (parameterValue.hasValue()) {
                insertSpec = insertSpec.withColumn(column, parameterValue);
            }
        }
        PreparedOperation<?> operation = MapperUtil.createInsertIgnorePreparedOperation(insertSpec, statementMapper.getRenderContext(), tableSchema, args);

        List<SqlIdentifier> identifierColumns = template.getDataAccessStrategy().getIdentifierColumns(domainType);

        BiFunction<Row, RowMetadata, T> mappingFunction = this.template.getDataAccessStrategy().getConverter().populateIdIfNecessary(obj);
        return new Homo<>(template.getDatabaseClient().sql(operation).filter(statement -> {
            try {
                if (identifierColumns.isEmpty() || tableSchema.getPrimaryColumn().getReadMethod().invoke(obj) != null) {
                    return statement;// 主键已存在或无主键字段时，直接返回原语句
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("insert ignore read entity id fail: ", e);
                return statement;
            }
            return statement.returnGeneratedValues(template.getDataAccessStrategy().renderForGeneratedValues(identifierColumns.get(0)));
        }).map(mappingFunction).all().last(obj).doOnNext(result -> {
            log.debug("insert ignore using value end: {}", result);
        }));
    }

    @Override
    public Homo<List<T>> values(T... objs) {
        log.debug("insert using values start: {}", objs);
        return new Homo<>(
                Flux.fromIterable(Arrays.asList(objs))
                        .flatMapSequential(this::value)
                        .collectList()
        );
    }
}
