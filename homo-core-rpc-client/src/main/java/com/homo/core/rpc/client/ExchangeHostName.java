package com.homo.core.rpc.client;

import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.fun.MultiFunA;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public enum ExchangeHostName implements MultiFunA<String, Homo<String>> {
    not_wrap {
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            return Homo.result(tagName);
        }
    },
    statefulLess {
        @Override
        public Homo<String> apply(String tagName, Object... o) throws Exception {
            ServiceExport serviceExportInfo = GetBeanUtil.getBean(ServiceMgr.class).getServiceExportInfo(tagName);
            boolean stateful = false;
            if (serviceExportInfo!= null){
                stateful = serviceExportInfo.isStateful();
            }
            if (!stateful) {
                return Homo.result(tagName);
            }
            return Homo.result(null);
        }
    },
    statefulChoiceFirstParam {
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            if (objs.length > 0 && objs[0] instanceof Integer && (Integer) objs[0] != -1) {
                Integer podId = (Integer) objs[0];
                String hostName = ServiceUtil.formatStatefulName(tagName, podId);
                return Homo.result(hostName);
            }
            return Homo.result(null);
        }
    },
    statefulChoiceForParamMsgrLink {
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
            return Homo.result(null);
        }
    },
    statefulChoiceForEntityLink {
        @Override
        public Homo<String> apply(String tagName, Object... objs) throws Exception {
            for (Object obj : objs) {
                if (obj instanceof EntityRequest) {
                    EntityRequest request = (EntityRequest) obj;
                    return GetBeanUtil.getBean(ServiceStateMgr.class)
                            .computeUserLinkedPodIfAbsent(request.getId(), tagName, false)
                            .nextDo(podId -> {
                                if (podId == null) {
                                    log.error("statefulChoiceForEntityLink tagName {} objs {} podId is null,exchange to 0 !", tagName, objs);
                                    podId = 0;
                                }
                                String hostName = ServiceUtil.formatStatefulName(tagName, podId);
                                return Homo.result(hostName);
                            });
                }
            }
            return Homo.result(null);
        }
    },
    ;

    public static List<MultiFunA<String, Homo<String>>> registerFun = new ArrayList<>();

    static {
        registerFun.add(statefulLess);
        registerFun.add(statefulChoiceFirstParam);
        registerFun.add(statefulChoiceForParamMsgrLink);
        registerFun.add(statefulChoiceForEntityLink);
        registerFun.add(not_wrap);
    }

    public static Homo<String> exchange(String tagName, Object... objs) {
        Homo<String> chain = Homo.result(null);
        Iterator<MultiFunA<String, Homo<String>>> iterator = registerFun.iterator();
        try {
            while (iterator.hasNext()) {
                MultiFunA<String, Homo<String>> next = iterator.next();
                chain = chain.nextDo(ret -> {
                    if (ret == null) {
                        return next.apply(tagName, objs);
                    } else {
                        return Homo.result(ret);
                    }
                });
            }
            return chain;
        } catch (Exception e) {
            log.error("ExchangeHostName error tagName {} objs {} e", tagName, objs, e);
            return Homo.result(null);
        }
    }

    public static void register(MultiFunA<String, Homo<String>> fun) {
        registerFun.add(fun);
    }

    public static void fillPodParam(Integer realPod, Object[] params) {
        params[0] = realPod;
    }
}
