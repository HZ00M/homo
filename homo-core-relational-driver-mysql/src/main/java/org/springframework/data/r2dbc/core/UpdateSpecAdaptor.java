package org.springframework.data.r2dbc.core;

import com.homo.core.facade.relational.operation.UpdateOperation;
import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.facade.relational.query.HomoUpdate;
import com.homo.core.utils.rector.Homo;
import com.homo.relational.driver.mysql.utils.QueryConvertUtil;
import com.homo.relational.driver.mysql.utils.TableNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;

@RequiredArgsConstructor
@Slf4j
public class UpdateSpecAdaptor<T> implements UpdateOperation.UpdateSpec<T>{
    private final R2dbcEntityTemplate template;
    private final Class<T> domainType;
    private final Object[] args;
    Query query;
    @Override
    public UpdateOperation.UpdateSpec<T> matching(HomoQuery homoQuery) {
        this.query = QueryConvertUtil.convertToQuery(homoQuery);
        return this;
    }

    @Override
    public Homo<Long> apply(HomoUpdate homoUpdate) {
        Query realQuery = getQuery();
        Update update = QueryConvertUtil.convertToUpdate(homoUpdate);
        if (log.isDebugEnabled()){
            log.debug("count operation start: {}, update: {}", realQuery.getCriteria().orElse(Criteria.empty()), update);
        }
        SqlIdentifier tableName = TableNameUtil.getTableName(domainType, args);
        return Homo.warp(
                template.doUpdate(realQuery,update,domainType,tableName)
                        .map(count->(long)count)
                        .doOnNext(ret->{
                            log.debug("update end, affect rows {}", ret);
                        })
        );
    }

    @Override
    public Homo<T> apply(T entity) {
        InsertSpecAdaptor<T> insertSpecAdaptor = new InsertSpecAdaptor<>(template, domainType, false, args);
        return insertSpecAdaptor.value(entity);
    }

    public Query getQuery() {
        if (query == null) {
            return Query.empty();
        }
        return query;
    }
}
