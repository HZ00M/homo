package com.homo.core.facade.ability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StorageTime {
    long value() default 0;
}
