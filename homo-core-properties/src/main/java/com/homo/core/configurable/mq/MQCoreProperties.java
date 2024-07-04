package com.homo.core.configurable.mq;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class MQCoreProperties {
    @Value("${homo.mq.consumer.auto.start:FALSE}")
    private Boolean autoStart;
    @Value("${homo.mq.topic.resolve.strategy:DEFAULT}")
    private String topicResolveStrategy;
}
