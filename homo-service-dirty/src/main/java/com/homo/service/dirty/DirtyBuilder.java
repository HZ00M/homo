package com.homo.service.dirty;

import com.homo.core.facade.storege.Dirty;
import com.homo.core.utils.callback.CallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DirtyBuilder<T> {

    public String key;
    public Map<String, String> dirtyMap = new HashMap<>();
    public CallBack<T> callBack;

    public static <T> DirtyBuilder<T> create(String key, CallBack<T> callBack) {
        DirtyBuilder<T> builder = new DirtyBuilder<>();
        builder.key = key;
        builder.callBack = callBack;
        return builder;
    }

    public DirtyBuilder<T> update(String appId, String regionId, String logicType, String ownerId, String field) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, Type.UPDATE.name());
        return this;
    }

    public DirtyBuilder<T> incr(String appId, String regionId, String logicType, String ownerId, String field) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, Type.INCR.name());
        return this;
    }

    public DirtyBuilder<T> remove(String appId, String regionId, String logicType, String ownerId, String field) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, Type.REMOVE.name());
        return this;
    }

    public DirtyBuilder<T> callBack(CallBack<T> callBack) {
        this.callBack = callBack;
        return this;
    }

    public DirtyImpl<T> build() {
        Assert.isTrue(key != null && !dirtyMap.isEmpty(), "params miss !");
        return new DirtyImpl<T>(key, dirtyMap, callBack);
    }

    public static String buildDirtyKey(String appId, String regionId, String logicType, String ownerId, String field) {
        return appId + ":" + regionId + ":" + logicType + ":" + ownerId + ":" + field;
    }

    public static class DirtyImpl<T> implements Dirty<T> {
        public String key;
        public Map<String, String> dirtyMap;
        public CallBack<T> callBack;

        private DirtyImpl(String key, Map<String, String> dirtyMap, CallBack<T> callBack) {
            this.key = key;
            this.dirtyMap = dirtyMap;
            this.callBack = callBack;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Map<String, String> dirtyMap() {
            return dirtyMap;
        }

        @Override
        public CallBack<T> callBack() {
            return callBack;
        }
    }

    enum Type {
        UPDATE, INCR, REMOVE
    }
}
