package com.homo.core.rpc.base.state;

import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.facade.service.LoadInfo;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 缓存服务器状态信息
 */
@Slf4j
public class ServiceStateHandlerImpl implements ServiceStateHandler, Runnable, Module {
    static final String SERVICE_NAME_TAG = "serviceNameTag";
    private Map<String, List<Integer>> goodServiceMap = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> availableServiceMap = new ConcurrentHashMap<>();
    @Autowired(required = false)
    private ServerStateProperties serverStateProperties;
    @Autowired(required = false)
    CacheDriver cacheDriver;
    @Lazy
    @Autowired(required = false)
    private ServiceStateMgr stateMgr;
    Map<String, String> tagToServiceNameMap = new ConcurrentHashMap<>();
    @Autowired
    private RootModule rootModule;
    @Override
    public void afterAllModuleInit() {
        scheduleUpdate();
    }

    private BiFunction<String, List<Integer>, Integer> choiceFun = new BiFunction<String, List<Integer>, Integer>() {
        /**
         * 取模用service
         */
        private Map<String, Integer> serviceMode = new HashMap<>();

        @Override
        public Integer apply(String serviceName, List<Integer> goodService) {
            Integer currentCount = serviceMode.getOrDefault(serviceName, 0);
            serviceMode.put(serviceName, currentCount + 1);
            if (goodService.size() > 0) {
                return goodService.get(currentCount % goodService.size());
            }
            return null;
        }
    };


    private void scheduleUpdate() {
        CallQueueMgr.getInstance().frameTask(new Runnable() {
            @Override
            public void run() {
                HomoTimerMgr.getInstance().schedule("ServiceStateHandlerImpl scheduleUpdate",ServiceStateHandlerImpl.this, 0,
                        serverStateProperties.getServiceStateUpdatePeriodMillSeconds(),
                        HomoTimerMgr.UNLESS_TIMES);
            }
        });
    }

    @Override
    public void run() {
        log.debug("state cache update");
        if (goodServiceMap.size() == 0) {
            return;
        }
        for (String serviceName : goodServiceMap.keySet()) {
            updateService(serviceName).start();
        }
    }

    private Homo<Boolean> updateService(String serviceName) {
        return stateMgr.getAllStateInfo(serviceName)
                .nextDo(map -> {
                    if (map == null || map.size() == 0) {
                        goodServiceMap.put(serviceName, new ArrayList<>());
                    } else {
                        //缓存符合条件的service
                        int range = serverStateProperties.getGoodStateRange().getOrDefault(serviceName,
                                serverStateProperties.getDefaultRange());
                        List<LoadInfo> stateList = new ArrayList<>(map.values());
                        //寻找goodServices，取负载最优的服务加上基数作为比较值，小于这个值表示服务状态良好
                        List<Integer> goodServices = new ArrayList<>();
                        int limit = stateList.get(0).load + range;
                        for (LoadInfo loadInfo : stateList) {
                            if (loadInfo.load <= limit) {
                                goodServices.add(loadInfo.id);
                            } else {
                                break;
                            }
                        }
                        goodServiceMap.put(serviceName, goodServices);
                        List<Integer> aliveServices = stateList.stream().map(item->item.id).collect(Collectors.toList());
                        availableServiceMap.put(serviceName, aliveServices);
                    }
                    return Homo.result(true);
                })
                .errorContinue(throwable -> {
                    log.error("updateService error ", throwable);
                    return Homo.result(false);
                });
    }


    @Override
    public Homo<String> getServiceNameByTag(String tag) {
        String inMen = tagToServiceNameMap.get(tag);
        if (inMen != null) {
            return Homo.result(inMen);
        }
        return Homo.warp(homoSink -> {
            ArrayList<String> fields = new ArrayList<>();
            fields.add(tag);
            cacheDriver.asyncGetByFields(rootModule.getServerInfo().appId, rootModule.getServerInfo().regionId, SERVICE_NAME_TAG, tag, fields)
                    .consumerValue(ret -> {
                        byte[] bytes = ret.get(tag);
                        if (bytes == null) {
                            // 如果找不到tag先按原tag返回
                            log.warn("tag {} of service not found! return tag!", tag);
                            homoSink.success(tag);
                        }
                        String serviceName = new String(bytes, StandardCharsets.UTF_8);
                        tagToServiceNameMap.put(tag, serviceName);
                        homoSink.success(serviceName);
                    })
                    .catchError(throwable -> {
                        log.error("get tag {} error!", tag, throwable);
                    });
        });
    }

    @Override
    public Homo<Boolean> setServiceNameTag(String tag, String serviceName) {
        setLocalServiceNameTag(tag, serviceName);
        return Homo.warp(homoSink -> {
            Map<String, byte[]> saveMap = new HashMap<>();
            saveMap.put(tag, serviceName.getBytes(StandardCharsets.UTF_8));
            cacheDriver.asyncUpdate(rootModule.getServerInfo().appId, rootModule.getServerInfo().regionId, SERVICE_NAME_TAG, tag, saveMap);
        });
    }

    @Override
    public void setLocalServiceNameTag(String tag, String serviceName) {
        tagToServiceNameMap.put(tag, serviceName);
    }

    @Override
    public boolean isPodAvailable(String serviceName, Integer userPodIndex) {
        return availableServiceMap.getOrDefault(serviceName, new ArrayList<>()).contains(userPodIndex);
    }

    @Override
    public List<Integer> alivePods(String serviceName) {
        return availableServiceMap.getOrDefault(serviceName, new ArrayList<>());
    }

    @Override
    public Homo<Integer> choiceBestPod(String serviceName) {
        if (goodServiceMap.get(serviceName) == null) {
            //从来没有获取过,去storage获取
            return updateService(serviceName)
                    .nextDo(ret -> {
                        if (ret) {
                            return Homo.result(choiceFun.apply(serviceName, goodServiceMap.get(serviceName)));
                        } else {
                            return Homo.error(HomoError.throwError(HomoError.choicePodNotFound, serviceName));
                        }
                    });
        } else {
            return Homo.result(choiceFun.apply(serviceName, goodServiceMap.get(serviceName)));
        }

    }

    @Override
    public Homo<Map<Integer, LoadInfo>> getServiceAllStateInfo(String serviceName) {
        return stateMgr.getAllStateInfo(serviceName);
    }

    public void setChoiceFun(BiFunction<String, List<Integer>, Integer> choiceFun) {
        this.choiceFun = choiceFun;
    }


}
