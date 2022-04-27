package com.homo.core.facade.cache;

import com.homo.core.common.facade.Driver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.lang.Pair;

import java.util.List;
import java.util.Map;


public interface CacheDriver extends Driver {
    /**
     * 获得指定key
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param fieldList   key列表
     * @return 详细结果
     */
    void asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList, CallBack<Map<String,byte[]>> callBack);

    /**
     * 获得所有key 和 value
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @return 详细结果
     */
    void asyncGetAll(String appId, String regionId, String logicType, String ownerId, CallBack<Map<String,byte[]>> callBack);

    /**
     * 更新多key,value数据，通过响应式返回详细结果
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     * @return 详细结果
     */
     void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data,CallBack<Boolean> callBack);

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
     void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data,long expireSeconds,CallBack<Boolean> callBack);

    /**
     * 增加key列表的值
     * @param appId     游戏id
     * @param regionId  区服id
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData 指定key的值列表
     * @return
     */
     void asyncIncr(String appId, String regionId, String logicType, String ownerId,Map<String,Long> incrData,CallBack<Pair<Boolean,Map<String,Long>>> callBack);

    /**
     * 删除key列表的值
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys 指定key的值列表
     * @return
     */
    void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys,CallBack<Boolean> callBack);
}
