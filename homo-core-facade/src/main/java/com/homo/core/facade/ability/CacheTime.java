package com.homo.core.facade.ability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 二级缓存时间
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheTime {
    long value() default 0;
}
