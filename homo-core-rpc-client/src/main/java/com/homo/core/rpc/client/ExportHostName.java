package com.homo.core.rpc.client;

import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.fun.MultiFunA;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import io.homo.proto.client.ParameterMsg;

import java.util.ArrayList;
import java.util.List;

public enum ExportHostName implements MultiFunA<String, Homo<String>> {
    DEFAULT{
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            return Homo.result(ServiceUtil.formatStatefulName(tagName, 0));
        }
    },
    STRATEGY0 {
        @Override
        public Homo<String> apply(String tagName, Object... o) throws Exception {
            boolean stateful = GetBeanUtil.getBean(ServiceMgr.class).getServiceExportInfo(tagName).isStateful();
            if (stateful) {
                return STRATEGY1.apply(tagName, o);
            } else {
                return Homo.result(ServiceUtil.getServiceHostName(tagName));
            }
        }
    },
    STRATEGY1 {
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            if (objs.length > 0 && objs[0] instanceof Integer && (Integer) objs[0] != -1) {
                Integer podId = (Integer) objs[0];
                String hostName = ServiceUtil.formatStatefulName(tagName, podId);
                return Homo.result(hostName);
            }
            return STRATEGY2.apply(tagName, objs);
        }
    },
    STRATEGY2 {
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            if (objs.length > 1 && objs[1] instanceof ParameterMsg) {
                ParameterMsg parameterMsg = (ParameterMsg) objs[1];
                return GetBeanUtil.getBean(ServiceStateMgr.class)
                        .computeUserLinkedPodIfAbsent(parameterMsg.getUserId(), tagName, false)
                        .nextDo(podId -> {
                            String hostName = ServiceUtil.formatStatefulName(tagName, podId);
                            return Homo.result(hostName);
                        });

            }
            return DEFAULT.apply(tagName, objs);
        }
    },

    ;

    public static List<MultiFunA<String, Homo<String>>> registerFun = new ArrayList<>();

    public static void register(MultiFunA<String, Homo<String>> fun){
        registerFun.add(fun);
    }

    public static void fillPodParam(Integer realPod, Object[] params) {
        params[0] = realPod;
    }
}
