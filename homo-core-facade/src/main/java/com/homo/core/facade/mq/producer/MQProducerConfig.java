package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.strategy.TopicResolveStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class MQProducerConfig {
    MQType type;
    String appId;
    String regionId;
    TopicResolveStrategy topicResolveStrategy;
}
