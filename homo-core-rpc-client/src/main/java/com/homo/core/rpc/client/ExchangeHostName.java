package com.homo.core.rpc.client;

import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.fun.MultiFun1PWithException;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public enum ExchangeHostName implements MultiFun1PWithException<ServiceInfo, Homo<String>> {
    not_wrap {
        @Override
        public Homo<String> apply(ServiceInfo serverInfo, Object... objs) throws Exception {
            String tagName = serverInfo.getServiceTag();
            return Homo.result(tagName);
        }
    },
    statefulLess {
        @Override
        public Homo<String> apply(ServiceInfo serverInfo, Object... o) throws Exception {
            String tagName = serverInfo.getServiceTag();
            if (!serverInfo.isStateful) {
                return Homo.result(tagName);
            }
            return Homo.result(null);
        }
    },
    statefulChoiceFirstParam {
        @Override
        public Homo<String> apply(ServiceInfo serverInfo, Object... objs) throws Exception {
            String tagName = serverInfo.getServiceTag();
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
        public Homo<String> apply(ServiceInfo serverInfo, Object... objs) throws Exception {
            String tagName = serverInfo.getServiceTag();
            String serverHost = serverInfo.getServerHost();
            if (objs.length > 1 && objs[1] instanceof ParameterMsg) {
                ParameterMsg parameterMsg = (ParameterMsg) objs[1];
                return GetBeanUtil.getBean(ServiceStateMgr.class)
                        .computeUserLinkedPodIfAbsent(parameterMsg.getUserId(), serverHost, false)
                        .nextDo(podId -> {
                            if (podId == null) {
                                log.error("statefulChoiceForParamMsgrLink error podId == null serverInfo {} objs {}", serverInfo, objs);
                            }
                            String hostName = ServiceUtil.formatStatefulName(tagName, podId);
                            return Homo.result(hostName);
                        });

            }
            return Homo.result(null);
        }
    },
    statefulChoiceForEntityLink {
        @Override
        public Homo<String> apply(ServiceInfo serverInfo, Object... objs) throws Exception {
            String tagName = serverInfo.getServiceTag();
            String serverHost = serverInfo.getServerHost();
            for (Object obj : objs) {
                if (obj instanceof EntityRequest) {
                    EntityRequest request = (EntityRequest) obj;
                    return GetBeanUtil.getBean(ServiceStateMgr.class)
                            .computeUserLinkedPodIfAbsent(request.getId(), serverHost, false)
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

    public static List<MultiFun1PWithException<ServiceInfo, Homo<String>>> registerFun = new ArrayList<>();

    static {
        registerFun.add(statefulLess);
        registerFun.add(statefulChoiceFirstParam);
        registerFun.add(statefulChoiceForParamMsgrLink);
        registerFun.add(statefulChoiceForEntityLink);
        registerFun.add(not_wrap);
    }

    public static Homo<String> exchange(ServiceInfo tagName, Object... objs) {
        Homo<String> chain = Homo.result(null);
        Iterator<MultiFun1PWithException<ServiceInfo, Homo<String>>> iterator = registerFun.iterator();
        try {
            while (iterator.hasNext()) {
                MultiFun1PWithException<ServiceInfo, Homo<String>> next = iterator.next();
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

    public static void register(MultiFun1PWithException<ServiceInfo, Homo<String>> fun) {
        registerFun.add(fun);
    }

    public static void fillPodParam(Integer realPod, Object[] params) {
        params[0] = realPod;
    }
}
