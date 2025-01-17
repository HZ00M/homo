package com.homo.core.facade.relational.operation;

import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.facade.relational.query.HomoUpdate;
import com.homo.core.utils.rector.Homo;

/**
 * 数据更新操作接口
 */
public interface UpdateOperation {
    <T> UpdateSpec<T> update(Class<T> type,Object ... args);

    interface UpdateSpec<T> {
        // 匹配条件
        UpdateSpec<T> matching(HomoQuery homoQuery);

        // 更新内容
        Homo<Long> apply(HomoUpdate homoUpdate);

        //对象更新，等同于save
        Homo<T> apply(T entity);
    }
}
