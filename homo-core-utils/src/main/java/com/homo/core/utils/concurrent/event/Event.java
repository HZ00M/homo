package com.homo.core.utils.concurrent.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件接口  用于异步事件
 */
public interface Event {
    Logger log = LoggerFactory.getLogger(Event.class);

    default String id(){
        return "";
    }
    default void preProcess(){}

    void process();

    default boolean traceEnable(){
        return true;
    }

    default void afterProcess(){}

    default void doProcess(){
        preProcess();
        process();
        afterProcess();
    }

    /**
     * 获取processed状态
     */
    default boolean processed(){
        throw new RuntimeException("event not override method: cancel");
    }

    /**
     * 设置processed状态
     * @param processed
     */
    default void setProcessed(boolean processed){
        throw new RuntimeException("event not override method: cancel");
    }
}
