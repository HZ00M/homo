package com.homo.core.utils.reflect;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
@UtilityClass
public class HomoAnnotationUtil {

    public  Map<Class<?>, Map<Class<?>,Annotation>> annotationCache = new HashMap<>();


    public <T extends Annotation> T findAnnotation(Class<?> typeClazz, Class<T> annotationClazz) {
        Map<Class<?>, Annotation> annotationClassMap = annotationCache.computeIfAbsent(typeClazz, k -> new HashMap<>());

        if (annotationClassMap.containsKey(annotationClazz)){
            return (T)annotationClassMap.get(annotationClazz);
        }
        T annotation = typeClazz.getAnnotation(annotationClazz);
        if (annotation != null) {
            log.trace("getAnnotation typeClass {} {} find !", typeClazz, annotationClazz);
            return annotation;
        }
        for (Class<?> anInterface : typeClazz.getInterfaces()) {
            annotation = findAnnotation(anInterface, annotationClazz);
            if (annotation != null) {
                log.trace("getAnnotation typeClass {} {} find !", typeClazz, annotationClazz);
                return annotation;
            }
        }
        Class<?> supper = typeClazz.getSuperclass();
        if (supper != null) {
            return findAnnotation(supper, annotationClazz);
        }
        log.trace("getAnnotation typeClass {} {} not find !", typeClazz, annotationClazz);
        return null;
    }

    public <T extends Annotation> Map<Class<?>, Annotation> findAnnotations(Class<?> typeClazz) {
        if (annotationCache.containsKey(typeClazz)) {
            return  annotationCache.get(typeClazz);
        }
        Map<Class<?>, Annotation> annotationClassMap = annotationCache.computeIfAbsent(typeClazz, k -> new HashMap<>());

        Annotation[] annotations = typeClazz.getAnnotations();
        for (Annotation annotation : annotations) {
            annotationClassMap.put(annotation.annotationType(),annotation);
        }
        for (Class<?> anInterface : typeClazz.getInterfaces()) {
            annotations = anInterface.getAnnotations();
            for (Annotation annotation : annotations) {
                annotationClassMap.put(annotation.annotationType(),annotation);
            }
        }
        Class<?> supper = typeClazz.getSuperclass();
        if (supper != null) {
            annotations = supper.getAnnotations();
            for (Annotation annotation : annotations) {
                annotationClassMap.put(annotation.annotationType(),annotation);
            }
        }
        log.info("findAnnotations typeClass {} annotationSet{} !", typeClazz, annotationClassMap.values());
        return annotationClassMap;
    }

    /**
     * 获取指定类型的注解
     * @param annotationClazz 注解类型
     * @return 注解实例
     */
    public static Class<?> findAnnotationInterface(Class<?> typeClass, Class<? extends Annotation> annotationClazz){
        if (typeClass == null){
            return null;
        }
        if (typeClass.isAnnotationPresent(annotationClazz)){
            return typeClass;
        }
        Class<?>[] interfaces = typeClass.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            Class<?> rel = findAnnotationInterface(anInterface, annotationClazz);
            if (rel != null){
                return rel;
            }
        }
        Class<?> superclass = typeClass.getSuperclass();
        if (superclass != null){
            return findAnnotationInterface(superclass, annotationClazz);
        }
        return null;
    }
}
