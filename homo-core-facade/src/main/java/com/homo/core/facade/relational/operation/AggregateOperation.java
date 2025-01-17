package com.homo.core.facade.relational.operation;

import com.homo.core.utils.rector.Homo;

import java.util.List;

/**
 * 数据聚合相关接口 实现groupBy, join等操作
 */
public interface AggregateOperation<P extends AggregateOperation.Aggregation> {
    <T> AggregateSpec<T> aggregate(P aggregation, Class<T> outputType, Object ... args);

    interface AggregateSpec<T>{
        Homo<T> first();

        Homo<List<T>> all();
    }

    interface Aggregation{
    }
}
