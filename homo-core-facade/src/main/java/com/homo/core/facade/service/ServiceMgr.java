package com.homo.core.facade.service;

/**
 * 服务信息管理器，管理本进程所有服务
 */
public interface ServiceMgr {

    /**
     * 检查是否是本地服务器
     * @param serviceName
     * @param podIndex
     * @return
     */
    boolean isLocalService(String serviceName,Integer podIndex);

    /**
     * 获取状态管理器
     * @return
     */
    StateMgr getStateMgr();

    /**
     * 获取主服务名
     * @return
     */
    Service getMainService();
}
