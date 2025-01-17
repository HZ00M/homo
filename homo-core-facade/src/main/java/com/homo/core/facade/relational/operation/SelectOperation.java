package com.homo.core.facade.relational.operation;

import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.utils.rector.Homo;

import java.util.List;

/**
 * 查询操作
 */
public interface SelectOperation {
    <T> SelectSpec<T> find(Class<T> domainType,Object ... args);

    interface SelectSpec<T>{
        Homo<Long> count();

        Homo<Boolean> exists();

        Homo<T> findFirst();

        // 查找一条，如果有多条，就报错
        Homo<T> findOne();

        Homo<List<T>> findAll();

        SelectSpec<T> matching(HomoQuery homoQuery);
    }
}
