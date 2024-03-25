package com.homo.core.facade.service;

import com.homo.core.utils.rector.Homo;

import java.util.List;
import java.util.Map;

/**
 * 缓存服务信息，分离rpc client对 rpc server的依赖
 */
public interface ServiceStateHandler {
    Homo<String> getServiceNameByTag(String tag) ;

    Homo<Boolean> setServiceNameTag(String tag, String serviceName);

    void setLocalServiceNameTag(String tag, String serviceName);

    boolean isPodAvailable(String serviceName, Integer userPodIndex);

    List<Integer> alivePods(String serviceName);

    Homo<Integer> choiceBestPod(String serviceName);

    Homo<Map<Integer, LoadInfo>> getServiceAllStateInfo(String serviceName);
}
