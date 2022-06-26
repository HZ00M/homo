package com.homo.core.configurable.zipkin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class ZipKinProperties {
    @Value(value = "${homo.zipkin.namespace:homo_zipkin_config}")
    public String zipikinNamespace ;
    @Value(value = "${homo.zipkin.server.addr:127.0.0.1}")
    public String reportAddr ;
    @Value(value = "${homo.zipkin.server.port:9411}")
    public String reportPort ;
    @Value(value = "${homo.zipkin.server.supportsJoin:true}")
    public boolean supportsJoin ;
    @Value(value = "${homo.zipkin.client.trace.perSecond:10}")
    public int tracesPerSecond;
    @Value(value = "${homo.zipkin.client.trace.open:false}")
    public boolean isOpen  = false;
}
