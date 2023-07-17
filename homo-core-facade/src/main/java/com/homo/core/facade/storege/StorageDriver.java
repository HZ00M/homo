package com.homo.core.facade.storege;

import com.homo.core.facade.Driver;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.rector.Homo;

import java.util.List;
import java.util.Map;


public interface StorageDriver extends Driver {

    /**
     * 通过key列表获取value
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param fieldList key列表
     */
    Homo<Map<String, byte[]>> asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList);

    /**
     * 获得所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     */
    Homo<Map<String, byte[]>> asyncGetAll(String appId, String regionId, String logicType, String ownerId);
    /**
     * 更新多key,value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     */
    Homo<Pair<Boolean, Map<String, byte[]>>> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data);

    /**
     * 增加key列表的值
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData 指定key的值列表
     */
    Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData);
    /**
     * 删除key列表的值(逻辑删除)
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys 指定key的值列表
     */
    Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys);
}
