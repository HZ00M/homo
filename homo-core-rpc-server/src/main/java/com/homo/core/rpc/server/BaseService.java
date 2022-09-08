package com.homo.core.rpc.server;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcHandleInfo;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.util.function.Tuple2;

/**
 * 提供获取服务器信息及服务调用的基本能力
 */
@Slf4j
public class BaseService implements Service<Tuple2<String, RpcContent>,byte[]> {
    protected ServiceMgr serviceMgr;
    private String serviceName;
    private int port;
    private String hostName;
    private String driverType;
    private boolean stateful;
    private CallDispatcher callDispatcher;
    private RpcHandleInfo<Object[],Tuple2<String,RpcContent>> rpcHandleInfo;

    public void init(ServiceMgr serviceMgr,RpcHandleInfo rpcHandleInfo,CallDispatcher callDispatcher){
        preInit();

        this.serviceMgr = serviceMgr;
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

    @Override
    public Homo<Tuple2<String, RpcContent>> callFun(String srcService, String funName, RpcContent param) {
        return null;
    }

    @Override
    public Homo<byte[]> processError(String msgId, Throwable e) {
        return null;
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
