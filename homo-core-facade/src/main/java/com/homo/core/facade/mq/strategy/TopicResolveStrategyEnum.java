package com.homo.core.facade.mq.strategy;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum TopicResolveStrategyEnum implements TopicResolveStrategy {
    DEFAULT{
        @Override
        public String getRealTopic(@NotNull String originTopic, @NotNull String appId, @NotNull String regionId) {
            return originTopic;
        }
    },
    APPEND_APP_ID{
        @Override
        public String getRealTopic(@NotNull String originTopic, @NotNull String appId, @NotNull String regionId) {
            return String.format("%s-%s",originTopic,appId);
        }
    },
    APPEND_APP_ID_SERVER_ID{
        @Override
        public String getRealTopic(@NotNull String originTopic, @NotNull String appId, @NotNull String regionId) {
            return String.format("%s-%s-%s",originTopic,appId,regionId);
        }
    }
    ;
    public static Map<String,TopicResolveStrategy> map = new HashMap<>();
    static {
        for (TopicResolveStrategyEnum topicResolveStrategy : values()) {
            map.put(topicResolveStrategy.name(),topicResolveStrategy);
        }
    }

    public static TopicResolveStrategy getTopicResolveStrategy(String name){
        return map.get(name);
    }
}
