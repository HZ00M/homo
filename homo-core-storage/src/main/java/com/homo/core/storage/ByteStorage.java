package com.homo.core.storage;

import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ByteStorage 存储模块
 * <p>
 * 该类提供了对存储系统的操作封装，支持异步数据更新、查询、删除和自增操作。
 * 主要用于存储和管理字节数据（byte[]）。
 * </p>
 */
@Slf4j
public class ByteStorage implements Module {
    /**
     * 默认数据键值
     */
    public static final String DEFAULT_DATA_KEY = "data";

    @Autowired(required = false)
    StorageDriver storageDriver;

    /**
     * 根模块（用于获取应用 ID 和区域 ID）
     */
    @Autowired
    private RootModule rootModule;

    /**
     * 更新存储数据
     *
     * @param logicType 逻辑类型
     * @param ownerId   拥有者 ID
     * @param keyList   需要更新的数据（键值对）
     * @return 是否更新成功及更新后的数据
     */
    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String logicType, String ownerId, Map<String, byte[]> keyList) {
        return update(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId, keyList);
    }

    /**
     * 更新存储数据（指定应用 ID 和区域 ID）
     */
    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> keyList) {
        return storageDriver.asyncUpdate(appId, regionId, logicType, ownerId, keyList).errorContinue(Homo::error);
    }

    /**
     * 保存单条数据
     */
    public Homo<Boolean> save(String logicType, String ownerId, String key, byte[] data) {
        return save(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId, key, data);
    }

    /**
     * 保存单条数据（指定应用 ID 和区域 ID）
     */
    public Homo<Boolean> save(String appId, String regionId, String logicType, String ownerId, String key, byte[] data) {
        Map<String, byte[]> map = new HashMap<>();
        map.put(key, data);
        return storageDriver.asyncUpdate(appId, regionId, logicType, ownerId, map)
                .nextDo(ret -> Homo.result(ret.getKey()))
                .errorContinue(throwable -> Homo.error(new Exception(String.format("save error, logicType_%s, ownerId_%s, key_%s", logicType, ownerId, key))));
    }

    /**
     * 获取单个数据
     */
    public Homo<byte[]> get(String logicType, String ownerId, String key) {
        return get(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId, key);
    }

    /**
     * 获取单个数据（指定应用 ID 和区域 ID）
     */
    public Homo<byte[]> get(String appId, String regionId, String logicType, String ownerId, String key) {
        List<String> list = new ArrayList<>();
        list.add(key);
        return storageDriver.asyncGetByFields(appId, regionId, logicType, ownerId, list)
                .nextDo(ret -> Homo.result(ret == null || !ret.containsKey(key) ? null : ret.get(key)))
                .errorContinue(Homo::error);
    }

    /**
     * 批量获取数据
     */
    public Homo<Map<String, byte[]>> get(String logicType, String ownerId, List<String> keyList) {
        return get(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId, keyList);
    }

    /**
     * 批量获取数据（指定应用 ID 和区域 ID）
     */
    public Homo<Map<String, byte[]>> get(String appId, String regionId, String logicType, String ownerId, List<String> keyList) {
        return storageDriver.asyncGetByFields(appId, regionId, logicType, ownerId, keyList)
                .nextDo(Homo::result)
                .errorContinue(Homo::error);
    }

    /**
     * 获取所有数据
     */
    public Homo<Map<String, byte[]>> getAll(String logicType, String ownerId) {
        return getAll(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId);
    }

    /**
     * 获取所有数据（指定应用 ID 和区域 ID）
     */
    public Homo<Map<String, byte[]>> getAll(String appId, String regionId, String logicType, String ownerId) {
        return storageDriver.asyncGetAll(appId, regionId, logicType, ownerId)
                .nextDo(Homo::result)
                .errorContinue(Homo::error);
    }

    /**
     * 删除指定键的数据
     */
    public Homo<List<String>> removeKeys(String logicType, String ownerId, List<String> keys) {
        return removeKeys(rootModule.getServerInfo().getAppId(), rootModule.getServerInfo().getRegionId(), logicType, ownerId, keys);
    }

    /**
     * 删除指定键的数据（指定应用 ID 和区域 ID）
     */
    public Homo<List<String>> removeKeys(String appId, String regionId, String logicType, String ownerId, List<String> keys) {
        return storageDriver.asyncRemoveKeys(appId, regionId, logicType, ownerId, keys)
                .nextDo(ret -> ret ?
                        Homo.result(keys) :
                        Homo.error(new Exception(String.format("removeKeys failed, appId_%s regionId_%s logicType_%s, ownerId_%s, keys_%s",
                                appId, regionId, logicType, ownerId, keys))))
                .errorContinue(throwable ->
                        Homo.error(new Exception(String.format("removeKeys failed, appId_%s regionId_%s logicType_%s, ownerId_%s, keys_%s",
                                appId, regionId, logicType, ownerId, keys))));
    }

    /**
     * 对指定键的值进行自增操作
     */
    public Homo<Long> incr(String appId, String regionId, String logicType, String ownerId, String incrKey) {
        Map<String, Long> map = new HashMap<>();
        map.put(incrKey, 1L);
        return incr(appId, regionId, logicType, ownerId, map)
                .nextDo(ret -> Homo.result(ret.get(incrKey)))
                .errorContinue(Homo::error);
    }

    /**
     * 对多个键的值进行批量自增操作
     */
    public Homo<Map<String, Long>> incr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData) {
        return storageDriver.asyncIncr(appId, regionId, logicType, ownerId, incrData)
                .nextDo(ret ->
                        ret.getKey() ?
                                Homo.result(ret.getValue()) :
                                Homo.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%s, ownerId_%s, incrData_%s",
                                        appId, regionId, logicType, ownerId, incrData))))
                .errorContinue(throwable ->
                        Homo.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%s, ownerId_%s, incrData_%s",
                                appId, regionId, logicType, ownerId, incrData))));

    }
}
