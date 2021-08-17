package com.homo.service.driver.cache;

import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.utils.Homo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@Log4j2
public class RedisCacheDriverImpl implements CacheDriver {
    @Override
    public Homo<Map<String, byte[]>> get(String appId, String regionId, Integer logicType, String ownerId, List<String> keyList) {
        return null;
    }

    @Override
    public Homo<Pair<Boolean, Map<String, byte[]>>> getAll(String appId, String regionId, Integer logicType, String ownerId) {
        return null;
    }

    @Override
    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String appId, String regionId, Integer logicType, String ownerId, Map<String, byte[]> data) {
        return null;
    }

    @Override
    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String appId, String regionId, Integer logicType, String ownerId, Map<String, byte[]> data, long expireSeconds) {
        return null;
    }

    @Override
    public Homo<Pair<Boolean, Map<String, Long>>> incr(String appId, String regionId, Integer logicType, String ownerId, Map<String, Long> incrData) {
        return null;
    }

    @Override
    public Homo<Boolean> remove(String appId, String regionId, Integer logicType, String ownerId, List<String> remKeys) {
        return null;
    }
}
