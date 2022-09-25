package com.homo.core.rpc.base.serial;

import com.homo.core.utils.serial.FSTSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.ProtoSerializationProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class SerializeInfo {
    public Class<?> paramType;
    public HomoSerializationProcessor processor;

    private SerializeInfo(Class<?> paramType) {
        this.paramType = paramType;
    }

    public static SerializeInfo create(Class<?> clazz, HomoSerializationProcessor serializationProcessor) {
        SerializeInfo serializeInfo = new SerializeInfo(clazz);
        if (serializationProcessor != null) {
            serializeInfo.processor = serializationProcessor;
        } else {
            serializeInfo.processor = matchProcess(clazz);
        }
        return serializeInfo;
    }

    public static HomoSerializationProcessor matchProcess(Class<?> clazz) {
        if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
            // 记录protobuf解压函数
            return new ProtoSerializationProcessor();
        } else {
            // 如果是普通类就用FST来解压
            if (!Serializable.class.isAssignableFrom((clazz))) {
                return null;
            }
            return new FSTSerializationProcessor();
        }
    }
}
