package com.homo.core.facade.mq.strategy;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TopicResolveStrategy{
    String getRealTopic(@NotNull String originTopic, @NotNull String appId, @NotNull String regionId);
}
