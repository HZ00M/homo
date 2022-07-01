package com.homo.core.root.storage;

import com.homo.core.common.module.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ObjStorage implements Module {
    public static final String OBJECT_KEY = "data";
    /**
     * 缓存序列化类信息，避免每次保存的时候都需要序列化
     */
    Map<Class<?>,Class<?>> serializationMap = new HashMap<>();
    @Autowired(required = false)
    ByteStorage storage;


}
