package org.springframework.data.r2dbc.core;

import com.homo.relational.base.aggregate.HomoAggregation;
import com.homo.core.facade.relational.operation.AggregateOperation;
import com.homo.core.utils.rector.Homo;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.GroupBySpec;
import com.homo.relational.driver.mysql.utils.MapperUtil;
import org.springframework.data.relational.core.sql.AggregateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.PreparedOperation;

import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Slf4j
public class AggregateSpecAdaptor<T> implements AggregateOperation.AggregateSpec<T> {
    private final R2dbcEntityTemplate template;
    private final HomoAggregation aggregation;
    private final Class<T> outputType;
    private final Object[] args;

    private Homo<List<T>> execute() {
        StatementMapper statementMapper = template.getDataAccessStrategy().getStatementMapper();
        GroupBySpec groupBySpec = AggregateUtil.convertSpec(aggregation);
        PreparedOperation<?> operation = MapperUtil.createAggregateOperation(groupBySpec, statementMapper.getRenderContext(), args);
        BiFunction<Row, RowMetadata, T> rowMapper = template.getDataAccessStrategy().getRowMapper(outputType);
        return new Homo<>(
                template.getDatabaseClient()
                        .sql(operation)
                        .map(rowMapper)
                        .all()
                        .collectList()
                        .doOnNext(ret -> {
                            log.debug("aggregation end: {}", ret);
                        })
        );
    }

    @Override
    public Homo<T> first() {
        return execute()
                .nextValue(list->{
                    if (list.isEmpty()){
                        return null;
                    }
                    return list.get(0);
                });
    }

    @Override
    public Homo<List<T>> all() {
        return execute();
    }
}
