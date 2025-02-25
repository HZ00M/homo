package com.homo.core.rpc.base.service;

import brave.Span;
import com.homo.core.facade.ability.ICallSystem;
import com.homo.core.facade.ability.IEntityService;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.RpcInterceptor;
import com.homo.core.rpc.base.serial.RpcHandlerInfoForServer;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.module.ServiceModule;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import io.homo.proto.entity.Ping;
import io.homo.proto.entity.Pong;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 提供获取服务器信息及服务调用的基本能力
 */
public class BaseService implements Service, IEntityService, ServiceModule {
    // 默认是 private，改为 protected 或 public
    protected static final Logger log = LoggerFactory.getLogger(BaseService.class);
    @Autowired(required = false)
    @Lazy
    ICallSystem callSystem;

    protected ServiceMgr serviceMgr;
    private String tagName;
    private int port;
    private String hostName;
    private RpcType driverType;
    private boolean stateful;
    private CallDispatcher callDispatcher;
    private RpcHandlerInfoForServer rpcHandleInfo;
    public void init() {
        ServiceExport serviceExport = getServiceExport();
        tagName = serviceExport.tagName();
        hostName = ServiceUtil.getServiceHostNameByTag(tagName);
        port = ServiceUtil.getServicePortByTag(tagName);
        driverType = serviceExport.driverType();
        stateful = serviceExport.isStateful();
        rpcHandleInfo = new RpcHandlerInfoForServer(this.getClass());
        callDispatcher = new CallDispatcher(rpcHandleInfo);
    }
    @Override
    public void afterAllModuleInit() {
        preServerInit();
        GetBeanUtil.getBean(ServiceStateMgr.class).setServiceInfo(hostName, new ServiceInfo(tagName, hostName, port, stateful,driverType.ordinal())).start();
        afterServerInit();
    }
    @Override
    public String getTagName() {
        return tagName;
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
    public RpcType getType() {
        return driverType;
    }

    @Override
    public boolean isStateful() {
        return stateful;
    }

    /**
     * 委派调用请求
     *
     * @param srcService
     * @param funName
     * @param param
     * @return
     */
    @Override
    public Homo callFun(String srcService, String funName, RpcContent param) {
        return callDispatcher.callFun(this, srcService, funName, param);
    }

    /**
     * 注册调用拦截
     *
     * @param interceptor
     */
    public void setCallInspector(RpcInterceptor interceptor) {
        callDispatcher.setInterceptor(interceptor);
    }

    public ServiceExport getServiceExport() {
        return AnnotationUtils.findAnnotation(getClass(), ServiceExport.class);
    }

    @Override
    public boolean isLocalService() {
        return true;
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
    public String getPodName() {
        return serviceMgr.getStateMgr().getPodName();
    }

    @Override
    public Homo<EntityResponse> entityCall(Integer podIndex, EntityRequest request) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("entityCall podIndex {} type {} id {} request {}", podIndex, request.getType(), request.getId(), request);
        }
        return callSystem.call(request.getSrcName(), request, podIndex, null);
    }

    @Override
    public Homo<EntityResponse> entityCallForProxy(Integer podIndex, ParameterMsg parameterMsg, EntityRequest request) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("entityCallForProxy podIndex {} type {} id {} request {}", podIndex, request.getType(), request.getId(), request);
        }
        return callSystem.call(request.getSrcName(), request, podIndex, parameterMsg);
    }

    @Override
    public Homo<Pong> ping(Integer podIndex, ParameterMsg parameterMsg, Ping request) {
        if (log.isDebugEnabled()) {
            log.debug("ping podIndex {} request time {}", podIndex, request.getTime());
        }
        return Homo.result(Pong.newBuilder().setTime(System.currentTimeMillis()).build());
    }
}
