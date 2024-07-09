package com.homo.core.mq.base;

import com.homo.core.facade.mq.MQCodeC;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CodecRegister<DEST> {
    MQCodeC<?, ?> defaultCodec = new FSTMessageCodec();
    final ConcurrentHashMap<String, MQCodeC<?, DEST>> codecMap = new ConcurrentHashMap<>();
    
    public void setCodecs(Map<String, MQCodeC<?, DEST>> map) {
        map.forEach((topic,codec)->{
            if(topic!=null && codec!=null){
                codecMap.put(topic,codec);
            }
        });
    }

    public Map<String, MQCodeC<?, DEST>> getCodecs() {
        return codecMap;
    }

    public <T extends java.io.Serializable> void setCodec(@NotNull String topic, @NotNull MQCodeC<T,DEST> codec){
        codecMap.put(topic,codec);
    }
    @SuppressWarnings("unchecked")
    public  <T extends java.io.Serializable> MQCodeC<T,DEST> getCodec(@NotNull String topic){
        MQCodeC<T,DEST> codec= (MQCodeC<T,DEST>)codecMap.get(topic);
        if(codec==null){
            codec=(MQCodeC<T,DEST>)defaultCodec;
            if(log.isTraceEnabled()){
                log.trace("未找到 {} 特定的编码器，返回默认编码器",topic);
            }
        }
        return codec;
    }

    public <T extends java.io.Serializable> void setDefaultCodec(@NotNull MQCodeC<T,DEST> codec) {
        this.defaultCodec = codec;
    }
    @SuppressWarnings("unchecked")
    public <T extends java.io.Serializable> MQCodeC<T,DEST> getDefaultCodec() {
        return  (MQCodeC<T,DEST>)this.defaultCodec;
    }

    public boolean existsCodec(@NotNull String topic){
        return codecMap.containsKey(topic);
    }
}
