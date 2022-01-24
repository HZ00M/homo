package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "homo.jodis")
@Configuration
@Data
public class JodisProperties {
    @Value("${proxyDir}")
    private String jodisProxyDir;
    @Value("${connectString}")
    private String jodisConnectString;
    @Value("${auth}")
    String jodisAuth ;
}
