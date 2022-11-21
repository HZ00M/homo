package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RpcHttpServerProperties {
    /**
     * http 最大消息大小
     */
    @Value("${homo.rpc.server.http.bytesLimit:614400}")//600 * 1024
    private int bytesLimit;
}
