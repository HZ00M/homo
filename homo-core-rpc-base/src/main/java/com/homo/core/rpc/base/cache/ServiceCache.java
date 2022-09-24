package com.homo.core.rpc.base.cache;

import com.homo.core.utils.rector.Homo;

/**
 * 缓存服务信息，分离rpc client对 rpc server的依赖
 */
public interface ServiceCache {
    Homo<String> getServiceNameByTag(String tag) ;

    Homo<Boolean> setServiceNameTag(String tag, String serviceName);


    void setLocalServiceNameTag(String tag, String serviceName);
}
