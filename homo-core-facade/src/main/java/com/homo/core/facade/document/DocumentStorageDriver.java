package com.homo.core.facade.document;

import com.homo.core.utils.Driver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.rector.Homo;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface DocumentStorageDriver<F, S, U, P> extends Driver {

    /**
     * 查询文档数据
     *
     * @param filter 过滤条件
     * @param sort   排序条件
     * @param limit  limit
     * @param skip   skip
     * @param clazz  文档对象类型
     */
    <T> Homo<List<T>> asyncQuery(F filter, S sort, @NotNull Integer limit, Integer skip, Class<T> clazz);

    /**
     * 查询文档数据，返回视图
     *
     * @param filter     过滤条件
     * @param viewFilter 视图过滤条件
     * @param sort       排序条件
     * @param limit      limit
     * @param skip       skip
     * @param clazz      文档对象类型
     */
    <T, V> Homo<List<V>> asyncQuery(F filter, F viewFilter, S sort, @NotNull Integer limit, Integer skip, Class<V> viewClazz, Class<T> clazz);

    /**
     * 查找并修改
     *
     * @param filter 过滤条件
     * @param clazz 文档对象类型
     */
    <T> Homo<Boolean> asyncFindAndModify(String logicType, String ownerId,String key,F filter, U update, Class<T> clazz);

    /**
     * 异步聚合
     * @param pipeLine 聚合管道
     * @param viewClazz 返回结果视图
     * @param clazz 文档对象类型
     */
    <T, V> Homo<List<V>> asyncAggregate(P pipeLine, Class<V> viewClazz, Class<T> clazz);

    /**
     * 通过key列表获取value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList   key列表
     * @param clazz     文档对象类型
     */
    <T> Homo<Map<String, T>> asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, Class<T> clazz);

    /**
     * 获得同一路径下所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     */
    <T> Homo<Map<String, T>> asyncGetAll(String appId, String regionId, String logicType, String ownerId, Class<T> clazz);

    /**
     * 更新多key多value数据（全量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     */
    <T> Homo<Boolean> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, T> data, Class<T> clazz);

    /**
     * 更新单key的value数据（增量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据（更新其非空字段）
     * @param clazz     文档对象类型
     */
    <T> Homo<Boolean> asyncUpdatePartial(String appId, String regionId, String logicType, String ownerId, String key, Map<String, ?> data, Class<T> clazz);


    /**
     * 增加key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData  指定key的值列表
     * @param clazz     文档对象类型
     */
    <T> Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz);

    /**
     * 逻辑删除key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys   指定key的值列表
     * @param clazz     文档对象类型
     */
    <T> Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, Class<T> clazz);

    /**
     * 查询集合数量
     *
     * @param clazz 文档对象类型
     */
    <T> Homo<Long> getCount(F filter, int limit, int skip, String hint, Class<T> clazz);
}
