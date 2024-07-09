package com.homo.core.utils.module;

import lombok.ToString;

@ToString
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
    public String serverName;
    /**
     * 是否是有状态服务器
     */
    public boolean isStateful;

    public String getAppId() {
        return appId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getChannel() {
        return channel;
    }

    public String getServerName() {
        return serverName;
    }

    public boolean isStateful() {
        return isStateful;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setStateful(boolean stateful) {
        isStateful = stateful;
    }
}
