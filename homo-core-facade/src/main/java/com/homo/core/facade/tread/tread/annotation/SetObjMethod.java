package com.homo.core.facade.tread.tread.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注该方法是一个设置资源对象的方法
 *
 * @author dubian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SetObjMethod {
    /**
     * 资源名
     *
     * @return
     */
    String[] value();

    /**
     * 设置的资源对象类型，用于合法性校验
     *
     * @return
     */
    Class<?> type() default Object.class;
}
