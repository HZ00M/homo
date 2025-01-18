package org.springframework.data.r2dbc.core;

import com.homo.core.facade.relational.operation.SelectOperation;
import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.utils.rector.Homo;
import com.homo.relational.driver.mysql.utils.MapperUtil;
import com.homo.relational.driver.mysql.utils.QueryConvertUtil;
import com.homo.relational.driver.mysql.utils.TableNameUtil;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.*;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SelectSpecAdaptor<T> implements SelectOperation.SelectSpec<T> {
    private final R2dbcEntityTemplate template;
    private final Class<T> domainType;
    private final Object[] args;
    Query query;
    SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    private Query getQuery() {
        if (query == null) {
            query = Query.empty();
        }
        return query;
    }

    @Override
    public SelectOperation.SelectSpec<T> matching(HomoQuery homoQuery) {
        this.query = QueryConvertUtil.convertToQuery(homoQuery);
        return this;
    }

    @Override
    public Homo<Long> count() {
        Query realQuery = getQuery();
        if (log.isDebugEnabled()) {
            log.debug("count operation start: {}, {}, {}, {}",
                    realQuery.getCriteria().orElse(Criteria.empty()), realQuery.getLimit(), realQuery.getOffset(), realQuery.getSort());
        }
        RelationalPersistentEntity<?> entity = template.getConverter().getMappingContext().getPersistentEntity(domainType);
        StatementMapper.TypedStatementMapper<T> statementMapper = template.getDataAccessStrategy().getStatementMapper().forType(domainType);
        SqlIdentifier tableName = TableNameUtil.getTableName(domainType, args);
        StatementMapper.SelectSpec selectSpec = statementMapper.createSelect(tableName)
                .doWithTable((table, spec) -> {
                    boolean hasIdProperty = entity.hasIdProperty();
                    Expression countExpression;
                    if (hasIdProperty) {
                        SqlIdentifier columnName = entity.getRequiredIdProperty().getColumnName();
                        countExpression = table.column(columnName);
                    } else {
                        countExpression = Expressions.asterisk();
                    }
                    return spec.withProjection(Functions.count(countExpression));
                });
        Optional<CriteriaDefinition> criteria = query.getCriteria();
        if (criteria.isPresent()) {
            selectSpec = criteria.map(selectSpec::withCriteria).orElse(selectSpec);
        }
        PreparedOperation<Select> operation = MapperUtil.createSelectPreparedOperation(selectSpec, Objects.requireNonNull(statementMapper.getRenderContext()));
        return Homo.warp(
                template.getDatabaseClient().sql(operation)
                        .map(new Function<Row, Long>() {
                            @Override
                            public Long apply(Row row) {
                                return row.get(0, Long.class);
                            }
                        })
                        .first()
                        .defaultIfEmpty(0L)
        );
    }

    @Override
    public Homo<Boolean> exists() {
        Query realQuery = getQuery();
        if (log.isDebugEnabled()) {
            log.debug("exists operation start: {}, {}, {}, {}",
                    realQuery.getCriteria().orElse(Criteria.empty()), realQuery.getLimit(), realQuery.getOffset(), realQuery.getSort());
        }
        return new Homo<>(
                template.doExists(realQuery, domainType, TableNameUtil.getTableName(domainType, args))
                        .doOnNext(result -> {
                            log.debug("exists operation result {}", result);
                        })
        );
    }

    @Override
    public Homo<T> findFirst() {
        Query realQuery = getQuery();
        if (log.isDebugEnabled()) {
            log.debug("findFirst operation start: {}, {}, {}, {}",
                    realQuery.getCriteria().orElse(Criteria.empty()), realQuery.getLimit(), realQuery.getOffset(), realQuery.getSort());
        }
        PreparedOperation<Select> queryOperation = getQueryOperationByQuery(realQuery);
        DatabaseClient.GenericExecuteSpec executeSpec = template.getDatabaseClient().sql(queryOperation);
        RowsFetchSpec<T> rowsFetchSpec = getRowsFetchSpec(executeSpec, domainType, domainType);

        return Homo.warp(
                rowsFetchSpec.first()
                        .doOnNext(ret -> {
                            log.debug("findFirst realQuery {} ret {}", realQuery, ret);
                        })
        );
    }

    @Override
    public Homo<T> findOne() {
        Query realQuery = getQuery();
        if (log.isDebugEnabled()) {
            log.debug("findOne operation start: {}, {}, {}, {}", realQuery.getCriteria().orElse(Criteria.empty()), realQuery.getLimit(), realQuery.getOffset(), realQuery.getSort());
        }
        PreparedOperation<Select> operation = getQueryOperationByQuery(query);
        DatabaseClient.GenericExecuteSpec executeSpec = template.getDatabaseClient().sql(operation);
        RowsFetchSpec<T> rowsFetchSpec = getRowsFetchSpec(executeSpec, domainType, domainType);
        return Homo.warp(
                rowsFetchSpec.one()
                        .doOnNext(ret -> {
                            log.debug("findOne realQuery {} ret {}", realQuery, ret);
                        })
                        .doOnError(throwable ->{
                            log.warn("findOne error {}", throwable.getMessage());
                            Homo.result(throwable);
                        })
        );
    }

    @Override
    public Homo<List<T>> findAll() {
        Query realQuery = getQuery();
        if (log.isDebugEnabled()) {
            log.debug("findAll operation start: {}, {}, {}, {}", realQuery.getCriteria().orElse(Criteria.empty()), realQuery.getLimit(), realQuery.getOffset(), realQuery.getSort());
        }
        PreparedOperation<Select> operation = getQueryOperationByQuery(query);
        DatabaseClient.GenericExecuteSpec executeSpec = template.getDatabaseClient().sql(operation);
        RowsFetchSpec<T> rowsFetchSpec = getRowsFetchSpec(executeSpec, domainType, domainType);
        return Homo.warp(
                rowsFetchSpec.all()
                        .collectList()
                        .doOnNext(ret -> {
                            log.debug("findAll realQuery {} ret {}", realQuery, ret);
                        })
        );
    }

    private PreparedOperation<Select> getQueryOperationByQuery(Query query) {
        StatementMapper.TypedStatementMapper<T> statementMapper = template.getDataAccessStrategy().getStatementMapper().forType(domainType);
        SqlIdentifier tableName = TableNameUtil.getTableName(domainType, args);
        StatementMapper.SelectSpec selectSpec = statementMapper.createSelect(tableName)
                .doWithTable((table, spec) -> {
                    List<Expression> selectProjection = getSelectProjection(table, query, domainType);
                    return spec.withProjection(selectProjection);
                });
        if (query.getLimit() > 0) {
            selectSpec = selectSpec.limit(query.getLimit());
        }
        if (query.getOffset() > 0) {
            selectSpec = selectSpec.offset(query.getOffset());
        }
        if (query.isSorted()) {
            selectSpec = selectSpec.withSort(query.getSort());
        }
        Optional<CriteriaDefinition> criteria = query.getCriteria();
        if (criteria.isPresent()) {
            selectSpec = criteria.map(selectSpec::withCriteria).orElse(selectSpec);
        }
        return MapperUtil.createSelectPreparedOperation(selectSpec, Objects.requireNonNull(statementMapper.getRenderContext()));
    }

    private <R> RowsFetchSpec<R> getRowsFetchSpec(DatabaseClient.GenericExecuteSpec executeSpec, Class<?> entityClass,
                                                  Class<R> returnType) {
        boolean simpleType;
        BiFunction<Row, RowMetadata, R> rowMapper;
        if (returnType.isInterface()) {
            simpleType = template.getConverter().isSimpleType(entityClass);
            rowMapper = template.getDataAccessStrategy().getRowMapper(entityClass)
                    .andThen(o -> projectionFactory.createProjection(returnType, o));
        } else {
            simpleType = template.getConverter().isSimpleType(returnType);
            rowMapper = template.getDataAccessStrategy().getRowMapper(returnType);
        }
        // avoid top-level null values if the read type is a simple one (e.g. SELECT MAX(age) via Integer.class)
        if (simpleType) {
            return new UnwrapOptionalFetchSpecAdapter<>(
                    executeSpec.map((row, metadata) -> Optional.ofNullable(rowMapper.apply(row, metadata))));
        }
        return executeSpec.map(rowMapper);
    }

    private <K> List<Expression> getSelectProjection(Table table, Query query, Class<K> returnType) {
        if (query.getColumns().isEmpty()) {
            if (returnType.isInterface()) {
                ProjectionInformation projectionInformation = projectionFactory.getProjectionInformation(returnType);
                if (projectionInformation.isClosed()) {
                    return projectionInformation.getInputProperties().stream().map(FeatureDescriptor::getName)
                            .map(table::column).collect(Collectors.toList());
                }
            }
            return Collections.singletonList(table.asterisk());
        }
        return query.getColumns().stream().map(table::column).collect(Collectors.toList());
    }

    private static class UnwrapOptionalFetchSpecAdapter<T> implements RowsFetchSpec<T> {

        private final RowsFetchSpec<Optional<T>> delegate;

        private UnwrapOptionalFetchSpecAdapter(RowsFetchSpec<Optional<T>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Mono<T> one() {
            return delegate.one().handle((optional, sink) -> optional.ifPresent(sink::next));
        }

        @Override
        public Mono<T> first() {
            return delegate.first().handle((optional, sink) -> optional.ifPresent(sink::next));
        }

        @Override
        public Flux<T> all() {
            return delegate.all().handle((optional, sink) -> optional.ifPresent(sink::next));
        }
    }


}
