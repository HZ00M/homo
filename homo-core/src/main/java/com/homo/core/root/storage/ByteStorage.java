package com.homo.core.root.storage;

import com.homo.core.common.module.Module;
import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ByteStorage implements Module {
    @Autowired(required = false)
    StorageDriver storage;

    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String logicType, String ownerId, Map<String, byte[]> keyList) {
        return update(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId,keyList);
    }

    public Homo<Pair<Boolean, Map<String, byte[]>>> update(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> keyList) {
        return storage.asyncUpdate(appId,regionId,logicType,ownerId,keyList).errorContinue(Homo::error);
    }

    public Homo<byte[]> save(String logicType, String ownerId, String key, byte[] data) {
        return save(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId,key,data);
    }

    public Homo<byte[]> save(String appId, String regionId, String logicType, String ownerId, String key, byte[] data) {
        Map<String,byte[]> map = new HashMap<>();
        map.put(key,data);
        return storage.asyncUpdate(appId,regionId,logicType,ownerId,map)
                .nextDo(ret->{
                    if (ret.getKey()){
                        return Homo.result(data);
                    }else {
                        return Homo.error(new Exception(String.format("save error, logicType_%s, ownerId_%s, key_%s", logicType, ownerId, key)));
                    }
                })
                .errorContinue(throwable -> Homo.error(new Exception(String.format("save error, logicType_%s, ownerId_%s, key_%s", logicType, ownerId, key))));
    }

    public Homo<byte[]> get(String logicType, String ownerId, String key) {
        return get(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId,key);
    }

    public Homo<byte[]> get(String appId, String regionId, String logicType, String ownerId, String key) {
        List<String> list = new ArrayList<>();
        list.add(key);
        return storage.asyncGetByFields(appId,regionId,logicType,ownerId,list)
                .nextDo(ret->{
                    if (ret==null||!ret.containsKey(key)){
                        return Homo.result(null);
                    }else {
                        return Homo.result(ret.get(key));
                    }
                })
                .errorContinue(throwable->{
                    return Homo.error(throwable);
                });
    }

    public Homo<Map<String, byte[]>> get(String logicType, String ownerId, List<String> keyList) {
        return get(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId,keyList);
    }

    public Homo<Map<String, byte[]>> get(String appId, String regionId, String logicType, String ownerId, List<String> keyList) {
        return storage.asyncGetByFields(appId,regionId,logicType,ownerId,keyList)
                .nextDo(Homo::result)
                .errorContinue(Homo::error);
    }

    public Homo<Map<String, byte[]>> getAll(String logicType, String ownerId) {
        return getAll(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId);
    }

    public Homo<Map<String, byte[]>> getAll(String appId, String regionId, String logicType, String ownerId) {
        return storage.asyncGetAll(appId,regionId,logicType,ownerId)
                .nextDo(Homo::result)
                .errorContinue(Homo::error);
    }

    public Homo<List<String>> removeKeys(String logicType, String ownerId, List<String> keys) {
        return removeKeys(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType,ownerId,keys);
    }

    public Homo<List<String>> removeKeys(String appId, String regionId, String logicType, String ownerId, List<String> keys) {
        return storage.asyncRemoveKeys(appId,regionId,logicType,ownerId,keys)
                .nextDo(ret->{
                    if (ret){
                        return Homo.result(keys);
                    }
                    return Homo.error(new Exception(String.format("removeKeys failed, appId_%s regionId_%s logicType_%s, ownerId_%s, keys_%s", appId, regionId, logicType, ownerId, keys)));
                })
                .errorContinue(throwable -> Homo.error(new Exception(String.format("removeKeys failed, appId_%s regionId_%s logicType_%s, ownerId_%s, keys_%s", appId, regionId, logicType, ownerId, keys))));
    }

    public Homo<Long> incr(String appId, String regionId, String logicType, String ownerId, String incrKey) {
        Map<String,Long> map = new HashMap<>();
        map.put(incrKey,1L);
        return incr(appId,regionId,logicType,ownerId,map)
                .nextDo(ret-> Homo.result(ret.get(incrKey)))
                .errorContinue(Homo::error);
    }

    public Homo<Map<String, Long>> incr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData) {
        return storage.asyncIncr(appId,regionId,logicType,ownerId,incrData)
                .nextDo(ret->{
                    if (ret.getKey()){
                        return Homo.result(ret.getValue());
                    }
                    return Homo.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%s, ownerId_%s, incrData_%s", appId, regionId, logicType, ownerId, incrData)));
                })
                .errorContinue(throwable -> Homo.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%s, ownerId_%s, incrData_%s", appId, regionId, logicType, ownerId, incrData))))
                .errorContinue(Homo::error);
    }

}
