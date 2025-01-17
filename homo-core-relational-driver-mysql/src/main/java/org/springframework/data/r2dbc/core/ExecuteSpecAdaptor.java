package org.springframework.data.r2dbc.core;

import com.homo.core.facade.relational.operation.ExecuteOperation;
import com.homo.core.utils.rector.Homo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public  class ExecuteSpecAdaptor implements ExecuteOperation.ExecuteSpec {
    private final R2dbcEntityTemplate template;
    private final String sql;

    @Override
    public Homo<Map<String, Object>> one() {
        Mono<Map<String, Object>> mono = template.getDatabaseClient().sql(sql).fetch().one();
        return new Homo<>(mono);
    }

    @Override
    public Homo<Map<String, Object>> first() {
        Mono<Map<String, Object>> first = template.getDatabaseClient().sql(sql).fetch().first();
        return new Homo<>(first);
    }

    @Override
    public Homo<Integer> rowsUpdated() {
        Mono<Integer> updateRows = template.getDatabaseClient().sql(sql).fetch().rowsUpdated();
        return new Homo<>(updateRows);
    }

    @Override
    public Homo<List<Map<String, Object>>> all() {
        Mono<List<Map<String, Object>>> all = template.getDatabaseClient().sql(sql).fetch().all().collectList();
        return new Homo<>(all);
    }
}
