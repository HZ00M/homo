package com.homo.core.configurable.ability;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class AbilityProperties {
    /**
     * 定时更新频率 默认1秒
     */
    @Value("${homo.ability.storage.landing.interval.secondMillis:1000}")
    private long intervalSecondMillis;
    /**
     * 默认entity扫描路径
     */
    @Value("${homo.ability.storage.scan.entity.path:com.homo}")
    private String entityScanPath;
    /**
     * 默认缓存时间
     */
    @Value("${homo.ability.storage.default.cache.time.secondMillis:0}")
    private long cacheTimeSecondMillis;
    /**
     * 默认存储时间
     */
    @Value("${homo.ability.storage.default.save.time.secondMillis:60000}")
    private long saveTimeSecondMills;
}
