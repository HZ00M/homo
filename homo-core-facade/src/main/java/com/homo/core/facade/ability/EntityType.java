package com.homo.core.facade.ability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * entity Type
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityType {
    String type();

    /**
     * 是否只能本地调用
     * @return
     */
    boolean isLocalService() default false;
}
