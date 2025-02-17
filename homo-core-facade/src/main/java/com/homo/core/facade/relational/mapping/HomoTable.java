package com.homo.core.facade.relational.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface HomoTable {

    /**
     * 表名 空时默认类名
     */
    String value() default "";

    /**
     * 自动生成表
     * @return
     */
    boolean generate() default true;

    /**
     * 索引
     * @return
     */
    HomoIndex[] indices() default {};

    /**
     * 分表策略
     * @return
     */
    Class<? extends HomoTableDivideStrategy> nameStrategy() default DefaultHomoTableDivideStrategy.class;

    String driverName() default "";
}
