package com.homo.core.facade.relational.operation;

import com.homo.core.utils.rector.Homo;

import java.util.List;

/**
 * 数据保存操作接口
 */
public interface InsertOperation {
    /**
     * 保存一个对象，如果这个对象存在就更新这个对象
     */
    <T> InsertSpec<T> save(Class<T> domainType, Object... args);

    /**
     * 插入一个对象，如果对象存在即失败
     * @param <T>
     */
    <T> InsertSpec<T> insert(Class<T> domainType, Object... args);

    /**
     * 插入一个对象，如果对象存在即忽略
     * @param <T>
     */
    <T> InsertSpec<T> insertIgnore(Class<T> domainType, Object... args);

    interface InsertSpec<T>{
        /**
         * 保存单个对象
         */
        Homo<T> value(T obj);
        /**
         * 保存多个对象
         */
        Homo<List<T>> values(T... objs);
    }
}
