package com.homo.core.root.storage;

import com.homo.core.entity.storage.facade.driver.EntityStorageDriver;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.facade.lock.LockDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 实体存储模块
 */
@Slf4j
@Component
public class EntityStorage<F, S, U, P> {
    private boolean useCache;   //todo 增加缓存支持
    @Autowired(required = false)
    EntityStorageDriver<F, S, U, P> storageDriver;
    @Autowired(required = false)
    LockDriver<String> lockDriver;
    @Autowired(required = false)
    CacheDriver cacheDriver;
    public <T> void setUseCache(boolean isUse) {
        useCache = isUse;
    }

    public boolean isUseCache() {
        return useCache;
    }
}
