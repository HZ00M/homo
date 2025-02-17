package com.homo.core.storage;

import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.facade.document.DocumentStorageDriver;
import com.homo.core.facade.lock.LockDriver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.exception.LockException;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 响应式 实体存储模块
 */
@Slf4j
public class DocumentStorage<F, S, U, P> implements Module {
    @Autowired
    private RootModule rootModule;
    @Autowired(required = false)
    DocumentStorageDriver<F, S, U, P> documentDriver;
    @Autowired(required = false)
    LockDriver lockDriver;
    @Autowired(required = false)
    CacheDriver cacheDriver;

    public <T> Homo<List<T>> query(F filter,
                                   S sort,
                                   Integer limit,
                                   Integer skip,
                                   Class<T> clazz) {
        return documentDriver.asyncQuery(filter, sort, limit, skip, clazz);
    }

    public <T, V> Homo<List<V>> query(F filter,
                                      F viewFilter,
                                      S sort,
                                      Integer limit,
                                      Integer skip,
                                      Class<V> viewClazz,
                                      Class<T> clazz) {
        return documentDriver.asyncQuery(filter, viewFilter, sort, limit, skip, viewClazz, clazz);
    }

    public <T> Homo<Boolean> findAndModify(String logicType,
                                           String ownerId,
                                           String key,
                                           F filter,
                                           U update,
                                           Class<T> clazz) {
        return documentDriver.asyncFindAndModify(logicType, ownerId, key, filter, update, clazz);
    }

    public <T, V> Homo<List<V>> aggregate(P pipeline,
                                          Class<V> viewClazz,
                                          Class<T> clazz) {
        return documentDriver.asyncAggregate(pipeline, viewClazz, clazz);
    }


    public <T> Homo<Boolean> update(String logicType, String ownerId, Class<T> clazz, Map<String, T> keyList) {
        return documentDriver.asyncUpdate(rootModule.getServerInfo().appId, rootModule.getServerInfo().regionId, logicType, ownerId, keyList, clazz);
    }

    public <T> Homo<Boolean> update(String appId, String regionId, String logicType, String ownerId, Class<T> clazz, Map<String, T> keyList) {
        return documentDriver.asyncUpdate(appId, regionId, logicType, ownerId, keyList, clazz);
    }

    public <T> Homo<Boolean> updatePartial(String logicType, String ownerId, String key, Class<T> clazz, Map<String, ?> keyList) {
        return documentDriver.asyncUpdatePartial(
                rootModule.getServerInfo().appId,
                rootModule.getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                keyList,
                clazz);
    }

    public <T> Homo<Boolean> updatePartial(String appId, String regionId, String logicType, String ownerId, String key, Class<T> clazz, Map<String, ?> keyList) {
        return documentDriver.asyncUpdatePartial(appId,
                regionId,
                logicType,
                ownerId,
                key,
                keyList,
                clazz);
    }

    public <T> Homo<Boolean> save(String logicType, String ownerId, String key, T data, Class<T> clazz) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().regionId;
        return save(appId, regionId, logicType, ownerId, key, data, clazz);
    }

    public <T> Homo<Boolean> save(String appId, String regionId, String logicType, String ownerId, String key, T data, Class<T> clazz) {
        Map<String, T> updateData = new HashMap<>(1);
        updateData.put(key, data);
        return documentDriver.asyncUpdate(appId,
                        regionId,
                        logicType,
                        ownerId,
                        updateData,
                        clazz)
                .nextDo(ret -> {
                    return Homo.result(ret);
                });
    }


    public <T> Homo<Map<String, T>> getAllKeysAndVal(String logicType, String ownerId, Class<T> clazz) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().regionId;
        return getAllKeysAndVal(appId, regionId, logicType, ownerId, clazz);
    }

    public <T> Homo<Map<String, T>> getAllKeysAndVal(String appId, String regionId, String logicType, String ownerId, Class<T> clazz) {
        return documentDriver.asyncGetAll(appId, regionId, logicType, ownerId, clazz);
    }

    public <T> Homo<Boolean> removeKeys(String logicType, String ownerId, List<String> keys, Class<T> clazz) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().regionId;
        return removeKeys(appId, regionId, logicType, ownerId, keys, clazz);
    }

    public <T> Homo<Boolean> removeKeys(String appId, String regionId, String logicType, String ownerId, List<String> keys, Class<T> clazz) {
        return documentDriver.asyncRemoveKeys(appId, regionId, logicType, ownerId, keys, clazz);
    }

    public <T> Homo<Map<String, Long>> incr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz) {
        return documentDriver.asyncIncr(appId, regionId, logicType, ownerId, key, incrData, clazz)
                .nextDo(ret -> {
                    Boolean success = ret.getLeft();
                    if (!success) {
                        log.error("incr fail logicType {} ownerId {} key {} data {}", logicType, ownerId, key, incrData);
                    }
                    Map<String, Long> retData = ret.getRight();
                    return Homo.result(retData);
                });
    }

    public <T> Homo<Long> incr(String logicType, String ownerId, String key, String incrKey, Class<T> clazz) {
        Map<String, Long> incrData = new HashMap<>();
        incrData.put(incrKey, 1L);
        return incr(rootModule.getServerInfo().appId, rootModule.getServerInfo().regionId, logicType, ownerId, key, incrData, clazz)
                .nextDo(ret -> {
                    Long newValue = ret.get(incrKey);
                    return Homo.result(newValue);
                })
                ;
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
     */
    public <T> Homo<Map<String, T>> get(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            List<String> keyList,
            Class<T> clazz) {
        return documentDriver.asyncGetByKeys(appId, regionId, logicType, ownerId, keyList, clazz);
    }

    /**
     * 用默认的appid, regionId, 通过key列表获取value
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList   key列表
     * @param clazz     文档对象类型
     */
    public <T> Homo<Map<String, T>> get(String logicType, String ownerId, List<String> keyList, Class<T> clazz) {
        return get(
                rootModule.getServerInfo().appId,
                rootModule.getServerInfo().regionId,
                logicType,
                ownerId,
                keyList,
                clazz
        );
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
     */
    public <T> Homo<T> get(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Class<T> clazz) {
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        return get(appId, regionId, logicType, ownerId, keyList, clazz)
                .nextDo(map -> {
                    return Homo.result(map.get(key));
                });
    }

    /**
     * 用默认的appid, regionId, 通过单key获取value
     *
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param key       key
     * @param clazz     文档对象类型
     */
    public <T> Homo<T> get(String logicType,
                           String ownerId,
                           String key,
                           Class<T> clazz) {
        return get(
                rootModule.getServerInfo().appId,
                rootModule.getServerInfo().regionId,
                logicType,
                ownerId,
                key,
                clazz);
    }
}
