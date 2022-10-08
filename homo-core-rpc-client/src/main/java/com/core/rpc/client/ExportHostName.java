package com.core.rpc.client;

import com.homo.core.common.module.Module;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.fun.MultiFunA;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import io.homo.proto.client.ParameterMsg;

public enum ExportHostName implements MultiFunA<String,Homo<String>>, Module {
    STRATEGY0{
        @Override
        public Homo<String> apply(String serviceName, Object... o) throws Exception {
            boolean stateful = serviceMgr.getService(serviceName).isStateful();
            if (stateful){
                return STRATEGY1.apply(serviceName,o);
            }else {
                return Homo.result(serviceName);
            }
        }
    },
    STRATEGY1{
        @Override
        public Homo<String> apply(String serviceName,Object... objs) throws Exception {
            if ( objs[0] instanceof Integer && (Integer) objs[0]!=-1){
                Integer podId = (Integer) objs[0];
                String hostName  = ServiceUtil.formatStatefulName(serviceName, podId);
                return Homo.result(hostName);
            }
            return STRATEGY2.apply(serviceName,objs);
        }
    },
    STRATEGY2{
        @Override
        public Homo<String> apply(String serviceName,Object... objs) throws Exception {
            if (objs[1] instanceof ParameterMsg){
                ParameterMsg parameterMsg = (ParameterMsg) objs[1];
                return GetBeanUtil.getBean(ServiceStateMgr.class)
                        .computeUserLinkedPodIfAbsent(parameterMsg.getUserId(),serviceName,false)
                        .nextDo(podId->{
                            String hostName  = ServiceUtil.formatStatefulName(serviceName, podId);
                            return Homo.result(hostName);
                        });

            }
            return Homo.result(null);
        }
    }
    ;
    private static ServiceMgr serviceMgr;
    @Override
    public void init(){
        serviceMgr = GetBeanUtil.getBean(ServiceMgr.class);
    }
    public static void fillPodParam(Integer realPod,Object[] params){
        params[0] = realPod;
    }
}
