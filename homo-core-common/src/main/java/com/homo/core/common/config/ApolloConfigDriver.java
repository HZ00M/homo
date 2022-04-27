package com.homo.core.common.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class ApolloConfigDriver implements ConfigDriver {
    private Map<String, Config> configMap = new ConcurrentHashMap<>();
    private Runnable onUpdate;

    @Override
    public void init(String[] namespaces, Runnable onUpdate) {
        for (int i = 0; i < namespaces.length; i++) {
            Config config = ConfigService.getConfig(namespaces[i]);
            configMap.put(namespaces[i], config);
        }

        this.onUpdate = onUpdate;
    }

    @Override
    public void listenerNamespace(String namespace, Consumer<ConfigChangeEvent> newValueConsumer) {
        Objects.requireNonNull(configMap.computeIfPresent(namespace, (k, v) -> v)).addChangeListener(newValueConsumer::accept);
    }

    @Override
    public void afterKeyRefresh(String namespace, String key, Consumer<ConfigChangeEvent> newValueConsumer) {
    }

    @Override
    public String getProperty(String namespace, String key, String defaultValue) {
        return configMap.get(namespace).getProperty(key, defaultValue);
    }

    @Override
    public Integer getIntProperty(String namespace, String key, Integer defaultValue) {
        return configMap.get(namespace).getIntProperty(key, defaultValue);
    }

    @Override
    public Boolean getBoolProperty(String namespace, String key, Boolean defaultValue) {
        return configMap.get(namespace).getBooleanProperty(key, defaultValue);
    }


}
