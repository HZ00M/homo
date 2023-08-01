package com.homo.core.mysql.annotation;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TableField {
    String value() default "";
    JdbcType type();
    int length() default 255;
    String comment() default "";
    boolean notNull() default false;
    boolean autoInr() default false;
    boolean id() default false;
    String defaultValue() default "";
}
