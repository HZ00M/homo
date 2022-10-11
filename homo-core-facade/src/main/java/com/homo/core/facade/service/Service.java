package com.homo.core.facade.service;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.utils.rector.Homo;

public interface Service extends Module {
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
    RpcType getType();

    /**
     * 是否是有状态服
     * @return
     */
    boolean isStateful();

    <RETURN> Homo<RETURN> callFun(String srcService, String funName, RpcContent param) throws Exception;

    public ServiceExport getServiceExport();
}
