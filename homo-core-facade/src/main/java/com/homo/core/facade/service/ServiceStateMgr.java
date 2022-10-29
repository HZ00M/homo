package com.homo.core.facade.service;

import com.homo.core.common.module.ServiceModule;
import com.homo.core.utils.rector.Homo;

import java.util.Map;

/**
 * 服务器状态信息管理器
 */
public interface ServiceStateMgr extends ServiceModule {

    /**
     * 判断本进程是否是有状态进程
     * 这个本质是判断本进程的所有服务中是不是包含有状态服务
     * @return
     */
    boolean isStateful();

    /**
     * 设置服务器状态
     * @param load
     */
    void setLoad(Integer load);

    /**
     * 获取服务器状态
     * @return
     */
    Integer getLoad();

    /**
     * 获取pod名称
     * 格式： 服务名-index
     * @return
     */
    String getPodName();

    /**
     * 获取本服务的podIndex
     * @return
     */
    Integer getPodIndex();

    /**
     * 设置唯一id下某服务的连接Index
     * @param uid
     * @param serviceName
     * @param podIndex
     * @return
     */
    Homo<Integer> setUserLinkedPod(String uid, String serviceName, Integer podIndex, boolean persist);

    /**
     * 获取唯一id下指定服务的连接index(会先去拿本地缓存)
     * @param uid
     * @param serviceName
     * @return
     */
    Homo<Integer> getLinkedPod(String uid,String serviceName);

    /**
     * 获取唯一id下指定服务的连接index(直接从数据库获取)
     * @param uid
     * @param serviceName
     * @return
     */
    Homo<Integer> getUserLinkedPodNoCache(String uid, String serviceName);

    /**
     * 获取唯一id下指定的连接Index
     * 如果不存在，路由一个可用的pod
     * @param uid
     * @param serviceName
     * @param persist  是否需要永久保留连接信息,false只保留连接信息一段时间.(只有在对应连接信息不存在时此参数才有效,一般RRC请求方置为false即可)
     * @return
     */
    Homo<Integer> computeUserLinkedPodIfAbsent(String uid, String serviceName, Boolean persist);

    /**
     * 移除唯一id下指定服务的连接index
     * @param uid
     * @param serviceName
     * @param immediately 是否立即删除连接信息,true立即删除(false设置过期时间，默认1分钟，有的场景不应该立即删除，比如并发情况)
     * @return
     */
    Homo<Boolean> removeUserLinkedPod(String uid, String serviceName, Boolean immediately);

    /**
     * 获取唯一id下所有连接的服务
     * @param uid
     * @return
     */
    Homo<Map<String,Integer>> getAllUserLinkInfo(String uid);


    /**
     * 获取目标服务所有Pod状态
     * @param serviceName
     * @return
     */
    Homo<Map<Integer,Integer>> geAllStateInfo(String serviceName);
}
