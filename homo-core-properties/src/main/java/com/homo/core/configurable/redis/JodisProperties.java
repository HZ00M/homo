package com.homo.core.configurable.redis;

import com.homo.core.configurable.NameSpaceConstant;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = NameSpaceConstant.JODIS)
@Data
public class JodisProperties {
    @Value("${proxyDir:}")
    private String jodisProxyDir;
    @Value("${connectString:}")
    private String jodisConnectString;
    @Value("${auth:}")
    private String jodisAuth ;
}
