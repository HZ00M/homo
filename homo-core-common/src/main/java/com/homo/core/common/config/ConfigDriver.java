package com.homo.core.common.config;

import com.homo.core.common.facade.Driver;

import java.util.function.Consumer;

/**
 * 配置中心驱动
 */
public interface ConfigDriver extends Driver {

    /**
     *
     * @param namespace
     * @param onUpdate
     */
    void init(String namespace, Runnable onUpdate);

    /**
     * 监听key 当key改变时执行 Consumer逻辑
     * @param key
     * @param newValueConsumer
     * @param <T>
     */
    <T> void registerKey(String key, Consumer<T> newValueConsumer);

    /**
     * 监听key 当key改变后执行 Consumer逻辑
     * @param key
     * @param newValueConsumer
     * @param <T>
     */
    <T> void afterKeyRefresh(String key,Consumer<T> newValueConsumer);

    /**
     * 获取配置信息
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    <T> T getProperty(String namespace,String key,T defaultValue);
}
