package com.core.ability.base.storage;

/**
 * 二级缓存时间
 */
public @interface CacheTime {
    long value() default 0;
}
