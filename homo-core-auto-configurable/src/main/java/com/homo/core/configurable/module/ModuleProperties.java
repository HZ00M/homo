package com.homo.core.configurable.module;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
@ToString
public class ModuleProperties {
    @Value("${server.info.appId:1}")
    public  String appId ;
    @Value("${server.info.regionId:1}")
    public  String regionId ;
    @Value("${server.info.namespace:1}")
    public  String namespace ;
    @Value("${server.info.channel:*}")
    public  String channel ;
    @Value("${server.info.serverName:}")
    public  String serverName;
    @Value("${server.info.isTestEnv:false}")
    public  Boolean isTestEnv = false;
}
