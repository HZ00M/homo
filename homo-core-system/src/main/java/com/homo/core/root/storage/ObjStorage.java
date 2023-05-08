package com.homo.core.root.storage;

import com.homo.core.common.module.Module;
import com.homo.core.facade.storege.SaveObject;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

;

@Log4j2
public class ObjStorage implements Module {
    public static final String OBJECT_KEY = "data";
    @Autowired(required = false)
    ByteStorage storage;
    @Autowired(required = false)
    HomoSerializationProcessor serializationProcessor;

    public String warpAppId(String appId){
        return appId;
    }

    public <T extends SaveObject> Homo<Boolean> save(T obj){
        byte[] bytes = serializationProcessor.writeByte(obj);
        Map<String,byte[]> map = new HashMap<>();
        map.put(OBJECT_KEY,bytes);
        return storage.update(getServerInfo().getAppId(),getServerInfo().getRegionId(),obj.getLogicType(),obj.getOwnerId(),map)
                .nextDo(ret-> Homo.result(ret.getKey()));
    }

    public <T extends Serializable> Homo<Boolean> save(String appId, String regionId, String logicType, String ownerId, String key, byte[] objByte){

        Map<String,byte[]> map = new HashMap<>();
        map.put(key,objByte);
        return storage.update(appId,regionId,logicType,ownerId,map)
                .nextDo(ret-> Homo.result(ret.getKey()));
    }

    public <T extends Serializable> Homo<Boolean> save(String appId, String regionId, String logicType, String ownerId, String key, T obj){
        byte[] bytes = serializationProcessor.writeByte(obj);
        return save(appId,regionId,logicType,ownerId,key,bytes);
    }

    public <T extends Serializable> Homo<Pair<Boolean, Map<String, byte[]>>> update(String appId, String regionId, String logicType, String ownerId, Map<String,byte[]> map){
        return storage.update(warpAppId(appId),regionId,logicType,ownerId,map);
    }


    public <T extends SaveObject> Homo<T> load(String logicType, String ownerId,Class<T> clazz) {
        return load(warpAppId(getServerInfo().getAppId()), getServerInfo().getRegionId(), logicType, ownerId, OBJECT_KEY,clazz);
    }

    public <T extends Serializable> Homo<T> load(
            String appId,
            String regionId,
            String logicType,
            String ownerId,
            String key,
            Class<T> clazz) {

       return storage.get( appId, regionId, logicType, ownerId, key)
               .nextDo(ret-> {
                   if (ret == null){
                       return Homo.empty();
                   }else {
                       return Homo.result(serializationProcessor.readValue(ret,clazz));
                   }

               });
    }
}
