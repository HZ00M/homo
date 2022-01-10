package com.homo.core.facade.module;

/**
 * 进程内共享游戏信息
 */
public interface ServerInfo {

    String getAppId();

    String getRegionId();

    String getChannel();
}
