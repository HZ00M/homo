package com.homo.core.utils.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class ApolloConfigDriver implements ConfigDriver {
    private Map<String, Config> configMap = new ConcurrentHashMap<>();
    public Set<String> listenerKey = new HashSet<>();
    public Map<String,Consumer<String>> listenerKeyConsumerMap = new HashMap<>();

    public void init(Set<String> namespaces,  Runnable onUpdate) {
        for (String namespace : namespaces) {
            Config config = ConfigService.getConfig(namespace);
            config.addChangeListener(this::refresh);
            configMap.put(namespace, config);
        }
    }

    private void refresh(ConfigChangeEvent changeEvent){
        for (String changedKey : changeEvent.changedKeys()) {
            if (listenerKey.contains(changedKey)){
                listenerKeyConsumerMap.get(listenerKey).accept(changeEvent.getChange(changedKey).getNewValue());
            }
        }
    }

    @Override
    public Config registerNamespace(String namespace){
        Config config = ConfigService.getConfig(namespace);
        config.addChangeListener(this::refresh);
        configMap.put(namespace,config);
        return config;
    }

    @Override
    public void listenerNamespace(String namespace, Consumer<ConfigChangeEvent> newValueConsumer) {
        Objects.requireNonNull(configMap.computeIfPresent(namespace, (k, v) -> v)).addChangeListener(newValueConsumer::accept);
    }

    @Override
    public void listenerKey(String key, Consumer<String> newValueConsumer) {
        listenerKey.add(key);
        listenerKeyConsumerMap.put(key,newValueConsumer);
    }

    @Override
    public void afterKeyRefresh(String namespace, String key, Consumer<String> newValueConsumer) {
        registerNamespace(namespace).addChangeListener(changeEvent -> {
            if (changeEvent.changedKeys().contains(key)){
                newValueConsumer.accept(changeEvent.getChange(key).getNewValue());
            }
        });
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
