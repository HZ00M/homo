package com.homo.core.utils.reflect;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

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

    public <T extends Annotation> Set<Annotation> findAnnotations(Class<?> typeClazz) {
        if (annotationCache.containsKey(typeClazz)) {
            return (Set<Annotation>)annotationCache.get(typeClazz).values();
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
        return (Set<Annotation>) annotationClassMap.values();
    }
}
