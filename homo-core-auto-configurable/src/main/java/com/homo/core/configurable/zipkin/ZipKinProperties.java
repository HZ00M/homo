package com.homo.core.configurable.zipkin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class ZipKinProperties {
    @Value(value = "${homo.zipkin.namespace:homo_zipkin_config}")
    public String zipikinNamespace ;
    /**
     * 链路追踪上报地址
     */
    @Value(value = "${homo.zipkin.server.addr:127.0.0.1}")
    public String reportAddr ;
    /**
     * True表示跟踪系统支持在span.Kind之间共享span ID
     */
    @Value(value = "${homo.zipkin.server.supportsJoin:true}")
    public boolean supportsJoin ;
    /**
     * 每秒采样数
     */
    @Value(value = "${homo.zipkin.client.trace.perSecond:10}")
    public int tracesPerSecond;
    /**
     * 链路追踪开关
     */
    @Value(value = "${homo.zipkin.client.trace.open:false}")
    public boolean isOpen  = false;
}
