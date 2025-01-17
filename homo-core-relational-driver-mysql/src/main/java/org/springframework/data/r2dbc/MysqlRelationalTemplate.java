package org.springframework.data.r2dbc;

import com.homo.relational.base.aggregate.HomoAggregation;
import com.homo.core.facade.relational.operation.RelationalTemplate;
import com.homo.core.facade.relational.operation.UpdateOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.*;

@Getter
@Slf4j
public class MysqlRelationalTemplate implements RelationalTemplate<HomoAggregation> {
    private final R2dbcEntityTemplate template;

    public MysqlRelationalTemplate(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public ExecuteSpec execute(String sql) {
        return new ExecuteSpecAdaptor(template, sql);
    }

    @Override
    public <T> InsertSpec<T> save(Class<T> domainType, Object... args) {
        return new InsertSpecAdaptor(template, domainType, true, args);
    }

    @Override
    public <T> InsertSpec<T> insert(Class<T> domainType, Object... args) {
        return new InsertSpecAdaptor(template, domainType, false, args);
    }

    @Override
    public <T> InsertSpec<T> insertIgnore(Class<T> domainType, Object... args) {
        return new InsertIgnoreSpecAdaptor<>(template, domainType, args);
    }

    @Override
    public <T> SelectSpec<T> find(Class<T> domainType, Object... args) {
        return new SelectSpecAdaptor<>(template, domainType,  args);
    }

    @Override
    public <T> UpdateOperation.UpdateSpec<T> update(Class<T> type, Object... args) {
        return new UpdateSpecAdaptor<>(template, type,args);
    }

    @Override
    public <T> DeleteSpec<T> delete(Class<T> type, Object... args) {
        return new DeleteSpecAdaptor<>(template, type, args);
    }

    @Override
    public <T> AggregateSpec<T> aggregate(HomoAggregation aggregation, Class<T> outputType, Object... args) {
        return new AggregateSpecAdaptor<>(template, aggregation, outputType, args);
    }

}
