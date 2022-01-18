package com.homo.core.configurable.zipkin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ZipKinProperties {
    @Value(value = "homo.service.monitor.traces.report.addr")
    public String reportAddr ;
    @Value(value = "homo.service.monitor.traces.supportsJoin")
    public boolean supportsJoin ;
    @Value(value = "homo.service.monitor.traces.one.second.count")
    public int tracesPerSecond;
    @Value(value = "homo.service.monitor.traces.open")
    public boolean isOpen  = false;
}
