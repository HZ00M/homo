package com.homo.core.facade.ability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定时存储
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageTime {
    long value() default 0;
}
