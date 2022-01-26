package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "homo.jodis")
@Data
public class JodisProperties {
    @Value("${proxyDir:}")
    private String jodisProxyDir;
    @Value("${connectString:}")
    private String jodisConnectString;
    @Value("${auth:}")
    private String jodisAuth ;
}
