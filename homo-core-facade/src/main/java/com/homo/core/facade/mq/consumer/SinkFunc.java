package com.homo.core.facade.mq.consumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SinkFunc {
    /**
     * 消费的topic列表
     * @return
     */
    String[] topics();
}
