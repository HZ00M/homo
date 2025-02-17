package com.homo.core.facade.storege.landing;

import com.homo.core.utils.rector.Homo;

import java.util.List;

/**
 * 落地驱动
 * 提供数据捞取，落地服务
 */
public interface LandingDriver<T> {
    /**
     * 对指定hset的所有key捞出。通过该接口将数据库的冷数据进行热加载进一级缓存
     * @param appId
     * @param regionId
     * @param logicType
     * @param ownerId
     * @param redisKey
     * @return
     */
    Homo<Boolean> hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey);

    /**
     * 对指定hset的指定key捞出。通过该接口将数据库的冷数据进行热加载进一级缓存
     * @param appId
     * @param regionId
     * @param logicType
     * @param ownerId
     * @param redisKey
     * @return
     */
    Homo<List<T>> hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields);

    /**
     * 通过该方法将dirtyDriver的脏数据落地到数据库
     * @param dirtyTableName
     * @param dirtyList
     * @return
     */
    boolean batchLanding(String dirtyTableName, List<String> dirtyList);

    /**
     * 通过该方法将dirtyDriver的脏数据落地到数据库
     * @param dirtyList
     * @param dirtyList
     * @return
     */
    boolean singleLanding(List<String> dirtyList, String dirtyName);
}
