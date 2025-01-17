package org.springframework.data.r2dbc.core;

import com.homo.core.facade.relational.operation.DeleteOperation;
import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.utils.rector.Homo;
import com.homo.relational.driver.mysql.utils.QueryConvertUtil;
import com.homo.relational.driver.mysql.utils.TableNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.SqlIdentifier;

@RequiredArgsConstructor
@Slf4j
public class DeleteSpecAdaptor<T> implements DeleteOperation.DeleteSpec<T> {
    private final R2dbcEntityTemplate template;
    private final Class<?> domainType;
    private final Object[] args;
    Query query;

    private Query getQuery() {
        if (query == null) {
            query = Query.empty();
        }
        return query;
    }

    @Override
    public DeleteOperation.DeleteSpec<T> matching(HomoQuery homoQuery) {
        this.query = QueryConvertUtil.convertToQuery(homoQuery);
        return this;
    }

    @Override
    public Homo<Long> all() {
        SqlIdentifier tableName = TableNameUtil.getTableName(domainType, args);
        if (log.isDebugEnabled()) {
            log.debug("delete operation start tableName {} {}",tableName, getQuery().getCriteria().orElse(Criteria.empty()));
        }
        return Homo.warp(
                template.doDelete(getQuery(),domainType,tableName)
                        .map(count->(long)count)
                        .doOnNext(ret->{
                            log.debug("delete end: {}", ret);
                        })
        );
    }
}
