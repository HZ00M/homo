package com.homo.core.configurable.zipkin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ZipKinProperties {
    @Value(value = "homo.service.monitor.traces.report.addr")
    private String reportAddr ;
    @Value(value = "homo.service.monitor.traces.supportsJoin")
    private boolean supportsJoin ;
    @Value(value = "homo.service.monitor.traces.one.second.count")
    private int tracesPerSecond;
    @Value(value = "homo.service.monitor.traces.open")
    private boolean isOpen  = false;
}
