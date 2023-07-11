package com.homo.core.root.storage;

import com.homo.core.utils.module.Module;
import com.homo.core.facade.document.EntityStorageDriver;
import com.homo.core.facade.lock.LockDriver;
import com.homo.core.utils.callback.CallBack;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

/**
 * 回调式 实体存储模块
 */
@Log4j2
public class EntityStorage<F, S, U, P> implements Module {

    /**
     * 存储驱动
     */
    @Autowired(required = false)
    public EntityStorageDriver<F, S, U, P> storageDriver;

    /**
     * 锁驱动
     */
    @Autowired(required = false)
    public LockDriver lockDriver;

    /**
     * 查询文档
     *
     * @param filter   过滤条件
     * @param sort     排序条件
     * @param limit    limit
     * @param skip     skip
     * @param clazz    存储对象类型
     * @param callBack      回调返回结果
     */
    public <T> void asyncQuery(
            F filter,
            S sort,
            Integer limit,
            Integer skip,
            Class<T> clazz,
            CallBack<List<T>> callBack) {
        storageDriver.asyncQuery(
                filter,
                sort,
                limit,
                skip,
                clazz,
                callBack);
    }

    /**
     * 查询文档
     *
     * @param filter 过滤条件
     * @param sort   排序条件
     * @param limit  limit
     * @param skip   skip
     * @param clazz  存储对象类型
     * @param callBack    回调返回结果
     */
    public <T,V> void asyncQuery(
            F filter,
            F viewFilter,
            S sort,
            Integer limit,
            Integer skip,
            Class<V> viewClazz,
            Class<T> clazz,
            CallBack<List<V>> callBack) {
        storageDriver.asyncQuery(
                filter,
                viewFilter,
                sort,
                limit,
                skip,
                viewClazz,
                clazz,
                callBack);
    }


    /**
     * 查询文档
     *
     * @param filter   过滤条件
     * @param update    更新条件
     * @param clazz    存储对象类型
     * @param callBack      回调返回结果
     */
    public <T> void asyncFindAndModify(
            String logicType,
            String ownerId,
            String key,
            F filter,
            U update,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        storageDriver.asyncFindAndModify(
                logicType,
                ownerId,
                key,
                filter,
                update,
                clazz,
                callBack);
    }


    /**
     * 聚合查询
     *
     * @param pipeline   聚合规则
     * @param viewClazz    返回视图
     * @param clazz    原表对应实体类
     * @param callBack      回调返回结果
     */
    public <T,V> void asyncAggregate(
            P pipeline,
            Class<V> viewClazz,
            Class<T> clazz,
            CallBack<List<V>> callBack) {
        storageDriver.asyncAggregate(
                pipeline,
                viewClazz,
                clazz,
                callBack);
    }


    /**
     * 查询所有符合条件文档
     *
     * @param filter 过滤条件
     * @param sort   排序条件
     * @param clazz  存储对象类型
     * @param callBack    回调返回结果
     */
    public <T> void asyncQueryAll(
            F filter,
            S sort,
            Class<T> clazz,
            CallBack<List<T>> callBack) {
        storageDriver.asyncQuery(
                filter,
                sort,
                0,
                0,
                clazz,
                callBack);
    }

    /**
     * 获得所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     存储对象类型
     * @param callBack       回调返回结果
     */
    public <T> void getAllKeysAndVal(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            Class<T> clazz,
            CallBack<Map<String, T>> callBack) {
        storageDriver.asyncGetAll(appId,
                regionId,
                logicType,
                ownerId,
                clazz,
                callBack);
    }

    /**
     * 默认appid,regionid获得所有key 和 value
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param callBack       回调返回结果
     */
    public <T> void getAllKeysAndVal(String logicType,
                                     String ownerId,
                                     Class<T> clazz,
                                     CallBack<Map<String, T>> callBack) {
        storageDriver.asyncGetAll(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId, clazz,
                callBack);
    }

    /**
     * 默认appid,regionid更新多key,value数据，通过回调返回详细结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据,
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void updateWithCallBack(
            String logicType,
            String ownerId,
            Map<String, T> data,
            Class<T> clazz,
            CallBack<Pair<Boolean, Map<String, T>>> callBack) {
        storageDriver.asyncUpdate(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                data,
                clazz,
                callBack);
    }

    /**
     * 更新多key,value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void updateWithCallBack(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            Map<String, T> data,
            Class<T> clazz,
            CallBack<Pair<Boolean, Map<String, T>>> callBack) {
        storageDriver.asyncUpdate(appId,
                regionId,
                logicType,
                ownerId,
                data,
                clazz,
                callBack);
    }

    /**
     * 更新多key,value数据，不返回结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param data      待保存数据
     */
    public <T> void update(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            Class<T> clazz,
            Map<String, T> data) {
        updateWithCallBack(
                appId,
                regionId,
                logicType,
                ownerId,
                data,
                clazz,
                new CallBack<Pair<Boolean, Map<String, T>>>() {
                    @Override
                    public void onBack(Pair<Boolean, Map<String, T>> booleanMapPair) {
                        log.debug("ok");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("update error!", throwable);
                    }
                });
    }

    /**
     * 默认appid, regionId更新多key,value数据，不返回结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param data      待保存数据
     */
    public <T> void update(String logicType,
                           String ownerId,
                           Class<T> clazz,
                           Map<String, T> data) {
        updateWithCallBack(
                logicType,
                ownerId,
                data,
                clazz,
                new CallBack<Pair<Boolean, Map<String, T>>>() {
                    @Override
                    public void onBack(Pair<Boolean, Map<String, T>> booleanMapPair) {
                        log.debug("ok");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("update error!", throwable);
                    }
                });
    }


    /**
     * 默认appid,regionid,更新单个文档内的key value数据,通过回调返回详细结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据,
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void updatePartialWithCallBack(
            String logicType,
            String ownerId,
            String key,
            Map<String, ?> data,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        storageDriver.asyncUpdatePartial(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                data,
                clazz,
                callBack);
    }

    /**
     * 更新单个文档内的key value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void updatePartialWithCallBack(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Map<String, ?> data,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        storageDriver.asyncUpdatePartial(appId,
                regionId,
                logicType,
                ownerId,
                key,
                data,
                clazz,
                callBack);
    }

    /**
     * 更新单个文档内的key value，不返回结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     */
    public <T> void updatePartial(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Map<String, ?> data,
            Class<T> clazz
    ) {
        updatePartialWithCallBack(
                appId,
                regionId,
                logicType,
                ownerId,
                key,
                data,
                clazz,
                new CallBack<Boolean>() {
                    @Override
                    public void onBack(Boolean booleanMapPair) {
                        log.debug("ok");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("update error!", throwable);
                    }
                });
    }

    /**
     * 默认appid, regionId，更新单个文档内的key value，不返回结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     */
    public <T> void updatePartial(String logicType,
                                  String ownerId,
                                  String key,
                                  Map<String, ?> data, Class<T> clazz) {
        updatePartialWithCallBack(
                logicType,
                ownerId,
                key,
                data,
                clazz,
                new CallBack<Boolean>() {
                    @Override
                    public void onBack(Boolean booleanMapPair) {
                        log.debug("ok");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("update error!", throwable);
                    }
                });
    }

    /**
     * 更新单个key,value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param value     待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void saveWithDetailcallBack(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            T value,
            Class<T> clazz,
            CallBack<Pair<Boolean, T>> callBack) {
        Map<String, T> updataData = new HashMap<>(1);
        updataData.put(key, value);
        CallBack<Pair<Boolean, Map<String, T>>> updatecallBack =
                new CallBack<Pair<Boolean, Map<String, T>>>() {
                    @Override
                    public void onBack(Pair<Boolean, Map<String, T>> booleanMapPair) {
                        if (booleanMapPair.getLeft()) {
                            callBack.onBack(ImmutablePair.of(true, null));
                        } else {
                            callBack.onBack(ImmutablePair.of(false, booleanMapPair.getRight().get(key)));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callBack.onError(throwable);
                    }
                };
        updateWithCallBack(appId, regionId, logicType, ownerId, updataData, clazz, updatecallBack);
    }

    /**
     * 使用默认的appid,regionId更新单个key,value数据，通过回调返回详细结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param value     待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    public <T> void saveWithDetailcallBack(
            String logicType, String ownerId, String key, T value, Class<T> clazz, CallBack<Pair<Boolean, T>> callBack) {
        saveWithDetailcallBack(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                value,
                clazz,
                callBack);
    }

    /**
     * 更新单个key,value数据，通过回调返回成功失败结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param value     待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调成功失败结果
     */
    public <T> void save(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            T value,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        Map<String, T> updateData = new HashMap<>(1);
        updateData.put(key, value);
        CallBack<Pair<Boolean, Map<String, T>>> updatecallBack =
                new CallBack<Pair<Boolean, Map<String, T>>>() {
                    @Override
                    public void onBack(Pair<Boolean, Map<String, T>> booleanMapPair) {
                        callBack.onBack(booleanMapPair.getLeft());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callBack.onError(throwable);
                    }
                };
        updateWithCallBack(appId, regionId, logicType, ownerId, updateData, clazz, updatecallBack);
    }

    /**
     * 使用默认的appId, regionId更新单个key,value数据，通过回调返回成功失败结果
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param value     待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调成功失败结果
     */
    public <T> void save(String logicType, String ownerId, String key, T value, Class<T> clazz, CallBack<Boolean> callBack) {
        save(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                value,
                clazz,
                callBack);
    }

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
    public <T> void get(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            List<String> keyList,
            Class<T> clazz,
            CallBack<Map<String, T>> callBack) {
        storageDriver.asyncGetByKeys(appId, regionId, logicType, ownerId, keyList, clazz,
                callBack);
    }

    /**
     * 用默认的appid, regionId, 通过key列表获取value
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList   key列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回值列表
     */
    public <T> void get(
            String logicType, String ownerId, List<String> keyList, Class<T> clazz, CallBack<Map<String, T>> callBack) {
        storageDriver.asyncGetByKeys(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                keyList,
                clazz,
                callBack);
    }

    /**
     * 通过单key获取value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param clazz     文档对象类型
     * @param callBack       回调返回值
     */
    public <T> void get(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Class<T> clazz,
            CallBack<T> callBack) {
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        CallBack<Map<String, T>> callBackGetList =
                new CallBack<Map<String, T>>() {
                    @Override
                    public void onBack(Map<String, T> stringMap) {
                        if (stringMap == null) {
                            callBack.onBack(null);
                        } else {
                            callBack.onBack(stringMap.get(key));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callBack.onError(throwable);
                    }
                };
        get(appId, regionId, logicType, ownerId, keyList, clazz, callBackGetList);
    }

    /**
     * 用默认的appid, regionId, 通过单key获取value
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param clazz     文档对象类型
     * @param callBack       回调返回值
     */
    public <T> void get(String logicType,
                        String ownerId,
                        String key,
                        Class<T> clazz,
                        CallBack<T> callBack) {
        get(
                getServerInfo().appId,
                getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                clazz,
                callBack);
    }

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
    public <T> void incr(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Map<String, Long> incrData,
            Class<T> clazz,
            CallBack<Pair<Boolean, Map<String, Long>>> callBack) {
        storageDriver.asyncIncr(appId,
                regionId,
                logicType,
                ownerId,
                key,
                incrData,
                clazz,
                callBack);
    }

    /**
     * 增加key列表的值
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData  指定key列表自增
     * @param clazz     文档对象类型
     * @param callBack       回调返回新值列表
     */
    public <T> void incr(
            String logicType,
            String ownerId,
            String key,
            Map<String, Long> incrData,
            Class<T> clazz,
            CallBack<Pair<Boolean, Map<String, Long>>> callBack) {
        incr(getServerInfo().appId, getServerInfo().regionId, logicType, ownerId, key, incrData, clazz, callBack);
    }

    /**
     * 增加key列表的值
     *
     * @param appId     appId
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       指定key自增一次
     * @param clazz     文档对象类型
     * @param callBack       回调返回新值列表
     */
    public <T> void incr(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Class<T> clazz,
            CallBack<Pair<Boolean, Long>> callBack) {
        Map<String, Long> incrData = new HashMap<>();
        incrData.put(key, 1L);
        storageDriver.asyncIncr(
                appId,
                regionId,
                logicType,
                ownerId,
                "data",
                incrData,
                clazz,
                new CallBack<Pair<Boolean, Map<String, Long>>>() {
                    @Override
                    public void onBack(Pair<Boolean, Map<String, Long>> value) {
                        ImmutablePair immutablePair = new ImmutablePair<>(value.getLeft(), value.getRight().get(key));
                        callBack.onBack(immutablePair);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        onError(throwable);
                    }
                }
        );
    }

    public <T> void incr(
            String logicType,
            String ownerId,
            String key,
            Class<T> clazz,
            CallBack<Pair<Boolean, Long>> callBack) {
        Map<String, Long> incrData = new HashMap<>();
        incrData.put(key, 1L);
        incr(getServerInfo().appId, getServerInfo().regionId, logicType, ownerId, key, incrData, clazz, new CallBack<Pair<Boolean, Map<String, Long>>>() {
            @Override
            public void onBack(Pair<Boolean, Map<String, Long>> pair) {
                Map<String, Long> rightMap = pair.getRight();
                Pair<Boolean, Long> onBackPair = new ImmutablePair<>(pair.getLeft(), rightMap.get(key));
                callBack.onBack(onBackPair);
            }

            @Override
            public void onError(Throwable throwable) {
                callBack.onError(throwable);
            }
        });
    }

    /**
     * 删除key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys   指定key的值列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回是否删除成功
     */
    public <T> void removeKeys(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            List<String> remKeys,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        storageDriver.asyncRemoveKeys(appId, regionId, logicType, ownerId, remKeys, clazz,
                callBack);
    }

    /**
     * 删除key列表的值
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param callBack       回调返回是否删除成功
     */
    public <T> void removeKeys(
            String logicType,
            String ownerId,
            List<String> remKeys,
            Class<T> clazz,
            CallBack<Boolean> callBack) {
        log.info("removeKeys start, logicType_{} ownerId_{}", logicType, ownerId);
        storageDriver.asyncRemoveKeys(getServerInfo().appId, getServerInfo().regionId, logicType, ownerId, remKeys, clazz, callBack);
    }


    /**
     * 异步加锁
     *
     * @param appId      appId
     * @param regionId   regionId
     * @param logicType  逻辑类型
     * @param ownerId    ID
     * @param lockField  指定key
     * @param lockVal    期望值  没有值传""
     * @param uniqueId   令牌
     * @param expireTime 锁过期时间
     * @param callBack        回调返回是否解锁成功
     */
    public <T> void asyncLock(String appId, String regionId, String logicType, String ownerId,
                              String lockField, Integer expireTime,CallBack<Boolean> callBack) {
        lockDriver.asyncLock(appId, regionId, logicType, ownerId, lockField, expireTime,
                callBack);
    }


    /**
     * 异步解锁
     *
     * @param appId     appId
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param lockField 指定key
     * @param callBack       回调返回是否解锁成功
     */
    public <T> void asyncUnlock(String appId, String regionId, String logicType, String ownerId,
                                String lockField, CallBack<Boolean> callBack) {
        lockDriver.asyncUnlock(appId, regionId, logicType, ownerId, lockField, callBack);
    }

    /**
     * 同步加锁
     *
     * @param appId      appId
     * @param regionId   regionId
     * @param logicType  逻辑类型
     * @param ownerId    ID
     * @param lockField  指定key
     * @param lockVal    期望值
     * @param uniqueId   令牌
     * @param expireTime 锁过期时间
     * @return
     */
    public boolean lock(String appId, String regionId, String logicType, String ownerId,
                        String lockField, Integer expireTime) {
        return lockDriver.lock(appId, regionId, logicType, ownerId, lockField, expireTime);
    }

    /**
     * 同步解锁
     *
     * @param appId     appId
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param lockField 指定key
     * @return
     */
    public boolean unlock(String appId, String regionId, String logicType, String ownerId,
                          String lockField) {
        return lockDriver.unlock(appId, regionId, logicType, ownerId, lockField);
    }
}
