package com.homo.core.facade.cache;

import com.homo.core.facade.Driver;
import com.homo.core.utils.Homo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * 直接操作redis，不落地
 */
public interface CacheDriver extends Driver {

    /**
     * 通过key列表获取value
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList key列表
     * @return 返回值列表
     */
    Homo<Map<String,byte[]>> get(String appId, String regionId, Integer logicType , String ownerId, List<String> keyList);

    /**
     * 获得所有key 和 value
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @return 详细结果
     */
    Homo<Pair<Boolean,Map<String,byte[]>>> getAll(String appId, String regionId, Integer logicType, String ownerId);

    /**
     * 更新多key,value数据，通过响应式返回详细结果
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     * @return 详细结果
     */
    Homo<Pair<Boolean,Map<String,byte[]>>> update(String appId, String regionId, Integer logicType, String ownerId, Map<String, byte[]> data);

    /**
     * 更新多key,value数据（带过期时间，并通过响应式返回详细结果
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     * @param expireSeconds 超时时间,0为不超时
     * @return
     */
    Homo<Pair<Boolean,Map<String,byte[]>>> update(String appId, String regionId, Integer logicType, String ownerId, Map<String, byte[]> data,long expireSeconds);

    /**
     * 增加key列表的值
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData 指定key的值列表
     * @return
     */
    Homo<Pair<Boolean,Map<String,Long>>> incr(String appId, String regionId, Integer logicType, String ownerId,Map<String,Long> incrData);

    /**
     * 删除key列表的值
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys 指定key的值列表
     * @return
     */
    Homo<Boolean> remove(String appId, String regionId, Integer logicType, String ownerId, List<String> remKeys);
}
