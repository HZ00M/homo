package com.homo.core.facade.relational.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.ANNOTATION_TYPE})
public @interface HomoId {
    /**
     * 是否自动生成 ，仅string有效
     * @return
     */
    boolean autoGenerate() default false;
}
