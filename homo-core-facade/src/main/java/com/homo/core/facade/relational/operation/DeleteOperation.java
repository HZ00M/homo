package com.homo.core.facade.relational.operation;

import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.utils.rector.Homo;

/**
 * 数据删除操作接口
 */
public interface DeleteOperation {
    <T> DeleteSpec<T> delete(Class<T> type, Object ... args);

    interface DeleteSpec<T> {
        // 匹配条件
        DeleteSpec<T> matching(HomoQuery homoQuery);
        //return 删除的记录数
        Homo<Long> all();
    }
}
