package com.homo.core.facade.tread.processor.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注该方法是一个检查资源属性方法
 *
 * @author dubian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceCheckMethod {
    /**
     * 资源名
     *
     * @return
     */
    String value() ;

    /**
     * 检查失败提示信息
     */
    String checkInfo() default "";

}
