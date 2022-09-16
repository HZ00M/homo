package com.homo.core.rpc.server;

import com.homo.core.facade.rpc.RpcInterceptor;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcHandleInfo;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceMgr;
import com.homo.core.rpc.base.cache.ServerCache;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 提供获取服务器信息及服务调用的基本能力
 */
@Slf4j
public class BaseService implements Service {
    protected ServiceMgr serviceMgr;
    private String serviceName;
    private int port;
    private String hostName;
    private String driverType;
    private boolean stateful;
    private CallDispatcher callDispatcher;
    private RpcHandleInfo rpcHandleInfo;
    private ServerCache serverCache;

    public void init(ServiceMgr serviceMgr, ServerCache serverCache,RpcHandleInfo rpcHandleInfo, CallDispatcher callDispatcher){
        preInit();

        this.serviceMgr = serviceMgr;
        this.serverCache = serverCache;
        this.rpcHandleInfo = rpcHandleInfo;
        this.callDispatcher = callDispatcher;

        log.info("BaseService init !");
        ServiceExport serviceExport = getServiceExport();
        serviceName = serviceExport.ServiceName();
        String[] split = serviceName.split(":");
        hostName = split[0];
        port = Integer.parseInt(split[1]);
        driverType = serviceExport.DriverType().name();
        stateful = serviceExport.isStateful();

        serverCache.setServiceNameTag(serviceName,serviceName);

        postInit();
    }


    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getType() {
        return driverType;
    }

    @Override
    public boolean isStateful() {
        return stateful;
    }

    /**
     * 委派调用请求
     * @param srcService
     * @param funName
     * @param param
     * @return
     */
    @Override
    public Homo callFun(String srcService, String funName, RpcContent param) {
        return callDispatcher.callFun(srcService,funName,param);
    }

//    /**
//     * 委派处理异常
//     * @param msgId
//     * @param e
//     * @return
//     */
//    @Override
//    public Homo processError(String msgId, Throwable e) {
//        return callDispatcher.processError(msgId,e);
//    }

    /**
     * 注册调用拦截
     * @param interceptor
     */
    public void setCallInspector(RpcInterceptor interceptor){
        callDispatcher.setInterceptor(interceptor);
    }

    public ServiceExport getServiceExport(){
        return AnnotationUtils.getAnnotation(getClass(),ServiceExport.class);
    }

    /**
     * 返回pod的序号
      */
    public Integer getPodIndex() {
        return serviceMgr.getStateMgr().getPodIndex();
    }

    /**
     * 返回pod的名字
     */
    public String getPodName(){
        return serviceMgr.getStateMgr().getPodName();
    }

    /**
     * 初始化时给子类的回调
     */
    protected void preInit(){}

    /**
     * 初始化时给子类的回调
     */
    protected void postInit(){};

}
