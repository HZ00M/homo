package com.homo.core.configurable.zipkin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "homo.zipkin")
@Data
public class ZipKinProperties {
    @Value(value = "service.monitor.traces.report.addr")
    public String reportAddr ;
    @Value(value = "service.monitor.traces.supportsJoin")
    public boolean supportsJoin ;
    @Value(value = "service.monitor.traces.one.second.count")
    public int tracesPerSecond;
    @Value(value = "service.monitor.traces.open")
    public boolean isOpen  = false;
}
