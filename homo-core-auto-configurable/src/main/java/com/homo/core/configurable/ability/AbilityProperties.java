package com.homo.core.configurable.ability;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class AbilityProperties {
    @Value("${homo.ability.storage.landing.interval.secondMillis:1000}")
    private long tableNum;
    @Value("${homo.ability.storage.scan.entity.path:com.homo}")
    private String entityScanPath;
}