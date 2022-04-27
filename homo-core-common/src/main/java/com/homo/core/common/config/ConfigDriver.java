package com.homo.core.common.config;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.homo.core.common.facade.Driver;

import java.util.function.Consumer;

/**
 * 配置中心驱动
 */
public interface ConfigDriver extends Driver {

    /**
     * @param namespaces
     * @param onUpdate
     */
    void init(String[] namespaces, Runnable onUpdate);

    /**
     * 监听key 当key改变时执行 Consumer逻辑
     * @param namespace
     * @param newValueConsumer
     */
    void listenerNamespace(String namespace,  Consumer<ConfigChangeEvent> newValueConsumer);

    /**
     * 监听key 当key改变后执行 Consumer逻辑
     * @param namespace
     * @param key
     * @param newValueConsumer
     */
    void afterKeyRefresh(String namespace, String key, Consumer<ConfigChangeEvent> newValueConsumer);

    /**
     * 获取配置信息
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    String getProperty(String namespace, String key, String defaultValue);

    /**
     * 获取配置信息
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    Integer getIntProperty(String namespace, String key, Integer defaultValue);

    /**
     * 获取配置信息
     *
     * @param namespace
     * @param key
     * @param defaultValue
     * @return
     */
    Boolean getBoolProperty(String namespace, String key, Boolean defaultValue);
}
