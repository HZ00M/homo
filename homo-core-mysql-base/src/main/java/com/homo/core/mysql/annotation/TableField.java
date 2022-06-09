package com.homo.core.mysql.annotation;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TableField {
    String value() default "";
    JdbcType type();
    String comment() default "";
}
