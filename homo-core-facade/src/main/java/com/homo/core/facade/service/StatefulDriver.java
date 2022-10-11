package com.homo.core.facade.service;

import com.homo.core.utils.rector.Homo;

import java.util.Map;

/**
 * 有状态存储driver
 */
public interface StatefulDriver {
    int PERSIST_FOREVER = -1;

    /**
     * 立即删除
     */
    int DELETE_NOW = -1;

    /**
     * 设置服务状态
     * @param appId 游戏id
     * @param regionId 区服id
     * @param logicType logic 分区
     * @param serviceName 服务名
     * @param podId 服务实例index
     * @param state 状态 越大状态越差
     * @return true成功,false 失败
     */
    Homo<Boolean> setServiceState(String appId, String regionId, String logicType, String serviceName, int podId, int state);

    /**
     * 设置连接信息
     * @param appId 游戏id
     * @param regionId 区服id
     * @param logicType 类型
     * @param uid 用户唯一标识
     * @param serviceName 用户连接的服务名
     * @param podId 用户连接的服务实例id
     * @param persistSeconds 连接状态过期时间, {@link StatefulDriver#PERSIST_FOREVER} 即为永久有效.
     * @return true 成功,false 失败
     */
    Homo<Integer> setLinkedPod(String appId, String regionId, String logicType,String uid,String serviceName,int podId,int persistSeconds);

    /**
     * 无对应服务连接信息情况下设置连接信息
     * @param appId 游戏id
     * @param regionId 区服id
     * @param logicType 类型
     * @param uid 用户唯一标识
     * @param serviceName 用户连接的服务名
     * @param podId 用户连接的服务实例id
     * @param persistSeconds 连接状态过期时间, {@link StatefulDriver#PERSIST_FOREVER} 即为永久有效.NOTE: 此参数只在连接信息不存在的情况下有效
     * @return 实际设置的podId
     */
    Homo<Integer> setLinkedPodIfAbsent(String appId, String regionId, String logicType,String uid,String serviceName,int podId,int persistSeconds);

    /**
     * 获取uid下的连接信息
     * @param appId 游戏id
     * @param regionId 区服id
     * @param logicType 类型
     * @param uid 用户唯一标识
     * @param serviceName 用户需要连接的服务名
     * @return podId, 无对应连接信息则返回 null
     */
    Homo<Integer> getLinkedPod(String appId, String regionId, String logicType, String uid, String serviceName);

    /**
     * 去除服务连接信息
     * @param appId 游戏id
     * @param regionId 区服id
     * @param logicType 类型
     * @param uid 用户唯一标识
     * @param serviceName 用户已连接的服务名
     * @param persistSeconds 连接状态删除时间, {@link StatefulDriver#DELETE_NOW} 即为立即，指定时间则为过期时间
     * @return true 移除成功,false 失败
     */
    Homo<Boolean> removeLinkedPod(String appId, String regionId, String logicType, String uid, String serviceName, int persistSeconds);

    /**
     * 获取用户连接的所有服务
     * @param appId        游戏id
     * @param regionId     区服id
     * @param logicType    类型
     * @param uid          用户唯一标识
     * @return 该用户所有连接信息
     */
    Homo<Map<String, Integer>> getAllLinkService(String appId, String regionId, String logicType, String uid);

    /**
     * 获取Service所有pod的状态
     * @param appId 游戏is
     * @param regionId 区服id
     * @param logicType 类型
     * @param serviceName 服务名
     * @param beginTimeMillis 有效起始时间
     * @return rel Map<podId,state>
     */
    Homo<Map<Integer, Integer>> getServiceState(String appId,String regionId, String logicType,String serviceName,long beginTimeMillis);

}