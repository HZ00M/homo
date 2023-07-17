package com.homo.core.facade.module;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ServerInfo {
    /**
     * 应用id
     */
    public String appId = "1";
    /**
     * 区服id
     */
    public String regionId = "1";
    /**
     * 命名空间
     */
    public String namespace = "1";
    /**
     * 渠道id
     */
    public String channel = "*";
    /**
     * 服务名
     */
    public String serverName ;
    /**
     * 是否是有状态服务器
     */
    public boolean isStateful;
}