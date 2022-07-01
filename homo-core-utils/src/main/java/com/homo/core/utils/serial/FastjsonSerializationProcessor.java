package com.homo.core.utils.serial;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 基于fastjson序列化和反序列化处理器
 * 适用于小对象
 * @apiNote   https://github.com/FasterXML/jackson-docs/wiki/Finding-Javadoc
 */
public class FastjsonSerializationProcessor implements HomoSerializationProcessor{
    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }
    @Override
    public byte[] writeByte(Object obj) {
        if (obj instanceof SubtypeSerialization){
            return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
        }
        return JSON.toJSONBytes(obj);
    }

    @Override
    public String writeString(Object obj) {
        if (obj instanceof SubtypeSerialization){
            return JSON.toJSONString(obj, SerializerFeature.WriteClassName);
        }
        return JSON.toJSONString(obj);
    }

    @Override
    public <T> T readValue(byte[] obj, Class<T> clazz) {
        return JSON.parseObject(obj,clazz);
    }

    @Override
    public <T> T readValue(byte[] obj, HomoTypeReference<T> reference) {
        return JSON.parseObject(obj,reference.type);
    }

    @Override
    public <T> T readValue(String obj, Class<T> clazz) {
        return JSON.parseObject(obj,clazz);
    }

    @Override
    public <T> T readValue(String obj, HomoTypeReference reference) {
        return JSON.parseObject(obj,reference.type);
    }
}
