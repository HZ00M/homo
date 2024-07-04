package com.homo.core.mq.base;

import com.homo.core.facade.mq.MQCodeC;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

/**
 * 默认编码解码器实现 使用FST框架进行序列化操作
 */
public class FSTMessageCodec implements MQCodeC<Serializable, byte[]> {
    final FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

    @Override
    public byte[] encode(Serializable message) throws Exception {
        return fst.asByteArray(message);
    }

    @Override
    public Serializable decode(byte[] bytes) throws Exception {
        return (java.io.Serializable) fst.asObject(bytes);
    }

}
