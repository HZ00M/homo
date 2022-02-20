package com.homo.core.facade.storege;

import com.homo.core.common.facade.Driver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.lang.Pair;

import java.util.List;
import java.util.Map;


public interface StorageDriver extends Driver {

    /**
     * 通过key列表获取value
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList key列表
     * @param callBack 回调返回值列表
     */
    void asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, CallBack<Map<String, byte[]>> callBack);

    /**
     * 获得所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param callBack       回调返回结果
     */
    void asyncGetAll(String appId, String regionId, String logicType, String ownerId, CallBack<Map<String, byte[]>> callBack);
    /**
     * 更新多key,value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     * @param callBack       回调详细结果
     */
    void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, CallBack<Pair<Boolean, Map<String, byte[]>>> callBack);

    /**
     * 增加key列表的值
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData 指定key的值列表
     * @param callBack 回调返回新值列表
     */
    void asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData, CallBack<Pair<Boolean, Map<String, Long>>> callBack);
    /**
     * 删除key列表的值(逻辑删除)
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys 指定key的值列表
     * @param callBack 回调返回是否删除成功
     */
    void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, CallBack<Boolean> callBack);
}
