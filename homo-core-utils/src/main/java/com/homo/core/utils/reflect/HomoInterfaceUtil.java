package com.homo.core.utils.reflect;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class HomoInterfaceUtil {
    private final static Map<Class<?>,Set<Class<?>>> interfaceCaches = new ConcurrentHashMap<>();
    /**
     * 获取此类及其父类实现的所有接口（不重复）
     * @param clazz 需要获取的类
     * @return 接口类集合
     */
    public Set<Class<?>> getAllInterfaces(Class<?> clazz){
        Set<Class<?>> caches = interfaceCaches.get(clazz);
        if (caches!=null){
            return caches;
        }
        Class<?>[] directImplementedInterfaces = clazz.getInterfaces();
        Set<Class<?>> interfaces = Arrays.stream(directImplementedInterfaces).collect(Collectors.toSet());
        for (Class<?> inf : interfaces) {
            //递归查找当前接口继承的所有接口
            interfaces.addAll(getAllInterfaces(inf));
        }
        //查找父类的所有接口
        Class<?> superclass = clazz.getSuperclass();
        if (superclass==null){
            interfaceCaches.put(clazz,interfaces);
            return interfaces;
        }
        interfaces.addAll(getAllInterfaces(superclass));
        interfaceCaches.put(clazz,interfaces);
        return interfaces;
    }
}
