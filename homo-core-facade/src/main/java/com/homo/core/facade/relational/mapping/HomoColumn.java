package com.homo.core.facade.relational.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.ANNOTATION_TYPE})
public @interface HomoColumn {
    /**
     * column name
     */
    String value() default "";

    /**
     * column length
     */
    int length() default 255;

    /**
     * column default value
     */
    String defaultValue() default "";

    /**
     * column is nullable
     */
    boolean nullable() default true;

    /**
     * double时表示数值的总长度
     */
    int precision() default 0;

    /**
     * float时表示小数点所占的位数
     */
    int scale() default 0;
}
