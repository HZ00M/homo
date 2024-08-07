package com.homo.core.facade.storege.landing;

import com.homo.core.utils.rector.Homo;

import java.util.List;

public interface DBDataHolder<T> {
    Homo<Boolean> hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey);

    Homo<List<T>> hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields);

    boolean batchLanding(String dirtyTableName, List<String> dirtyList);

    boolean singleLanding(List<String> dirtyList, String dirtyName);
}
