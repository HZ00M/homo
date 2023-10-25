package com.homo.core.utils.serial;

import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class ProtoSerializationProcessor implements HomoSerializationProcessor {
    public static Map<Class<?>, Method> cacheUnSerialMap = new HashMap<>();

    @Override
    public byte[] writeByte(Object obj) {
        GeneratedMessageV3 protoObj = (com.google.protobuf.GeneratedMessageV3) obj;
        return protoObj.toByteArray();
    }

    @Override
    public String writeString(Object obj) {
        GeneratedMessageV3 protoObj = (com.google.protobuf.GeneratedMessageV3) obj;
        return protoObj.toString();
    }

    @Override
    public <T> T readValue(byte[] obj, Class<T> clazz) {
        Object result = null;
        try {
            if (!cacheUnSerialMap.containsKey(clazz)) {
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                    Method unSerialMethod = clazz.getMethod("parseFrom", byte[].class);
                    result = unSerialMethod.invoke(null, obj);
                    cacheUnSerialMap.put(clazz, unSerialMethod);
                }
            } else {
                result = cacheUnSerialMap.get(clazz).invoke(null, obj);
            }
        } catch (Exception e) {
            log.error("ProtoSerializationProcessor obj {} error {}", obj, e);
        }
        return (T) result;
    }

    @Override
    public <T> T readValue(byte[] obj, HomoTypeReference<T> reference) {
        Object result = null;
        Class<?> clazz = reference.type.getClass();
        try {
            if (!cacheUnSerialMap.containsKey(clazz)) {
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                    Method unSerialMethod = clazz.getMethod("parseFrom", byte[].class);
                    result = unSerialMethod.invoke(null, obj);
                    cacheUnSerialMap.put(clazz, unSerialMethod);
                }
            } else {
                cacheUnSerialMap.get(clazz).invoke(null, obj);
            }
        } catch (Exception e) {
            log.error("ProtoSerializationProcessor obj {} error {}", obj, e);
        }
        return (T) result;
    }

    @Override
    public <T> T readValue(String obj, Class<T> clazz) {
        Object result = null;
        try {
            if (!cacheUnSerialMap.containsKey(clazz)) {
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                    Method unSerialMethod = clazz.getMethod("parseFrom", byte[].class);
                    result = unSerialMethod.invoke(null, obj);
                    cacheUnSerialMap.put(clazz, unSerialMethod);
                }
            } else {
                cacheUnSerialMap.get(clazz).invoke(null, obj);
            }
        } catch (Exception e) {
            log.error("ProtoSerializationProcessor obj {} error {}", obj, e);
        }
        return (T) result;
    }

    @Override
    public <T> T readValue(String obj, HomoTypeReference<T> reference) {
        throw new RuntimeException("Not support");
    }
}
