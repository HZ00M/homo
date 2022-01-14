package com.homo.core.facade.config;

import com.homo.core.common.faccade.Driver;;

import java.util.function.Consumer;

/**
 * 配置中心驱动
 */
public interface ConfigDriver extends Driver {

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
}
