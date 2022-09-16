package com.homo.core.facade.service;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.utils.rector.Homo;

public interface Service {
    /**
     * 获取服务标识符  带端口号
     *
     * @return
     */
    String getServiceName();

    /**
     * 获取服务域名
     *
     * @return
     */
    String getHostName();

    /**
     * 获取服务端口号
     *
     * @return
     */
    int getPort();

    /**
     * 获取服务类型
     * @return
     */
    String getType();

    /**
     * 是否是有状态服
     * @return
     */
    boolean isStateful();

    <RETURN> Homo<RETURN> callFun(String srcService, String funName, RpcContent param);

//    <ERROR> Homo<ERROR> processError(String msgId, Throwable e);
}
