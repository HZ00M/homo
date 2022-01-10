package com.homo.core.facade.document;

import com.homo.core.facade.Driver;
import com.homo.core.utils.callback.CallBack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public interface DocumentDriver<F, S, U, P> extends Driver {

    /**
     * 查询文档数据
     *
     * @param filter 过滤条件
     * @param sort   排序条件
     * @param limit  limit
     * @param skip   skip
     * @param clazz  文档对象类型
     * @param callBack    回调返回结果
     */
    <T> void asyncQuery(F filter, S sort, Integer limit, Integer skip, Class<T> clazz, CallBack<List<T>> callBack);

    /**
     * 查找并修改
     *
     * @param filter 过滤条件
     * @param clazz 文档对象类型
     * @param callBack 回调返回结果
     */
    <T> void asyncFindAndModify(Integer logicType, String ownerId,String key,F filter, U update, Class<T> clazz, CallBack<Boolean> callBack);

    /**
     * 异步聚合
     * @param pipeLine 聚合管道
     * @param viewClazz 返回结果视图
     * @param clazz 文档对象类型
     * @param callBack 回调返回结果
     */
    <T, V> void asyncAggregate(P pipeLine, Class<V> viewClazz, Class<T> clazz, CallBack<List<V>> callBack);

    /**
     * 获得同一路径下所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param callBack       回调返回结果
     */
    <T> void asyncGetAllKeysAndVal(String appId, String regionId, Integer logicType, String ownerId, Class<T> clazz, CallBack<Map<String, T>> callBack);

    /**
     * 更新多key多value数据（全量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    <T> void asyncUpdate(String appId, String regionId, Integer logicType, String ownerId, Map<String, T> data, Class<T> clazz, CallBack<Pair<Boolean, Map<String, T>>> callBack);

    /**
     * 更新单key的value数据（增量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据（更新其非空字段）
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    <T> void updatePartial(String appId, String regionId, Integer logicType, String ownerId, String key, Map<String, ?> data, Class<T> clazz, CallBack<Boolean> callBack);

    /**
     * 通过key列表获取value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList   key列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回值列表
     */
    <T> void asyncGet(String appId, String regionId, Integer logicType, String ownerId, List<String> keyList, Class<T> clazz, CallBack<Map<String, T>> callBack);

    /**
     * 增加key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData  指定key的值列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回新值列表
     */
    <T> void asyncIncr(String appId, String regionId, Integer logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz, CallBack<Pair<Boolean, Map<String, Long>>> callBack);

    /**
     * 逻辑删除key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys   指定key的值列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回是否删除成功
     */
    <T> void asyncRemoveKeys(String appId, String regionId, Integer logicType, String ownerId, List<String> remKeys, Class<T> clazz, CallBack<Boolean> callBack);
}
