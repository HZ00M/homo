package com.homo.core.rpc.base.state;

import com.homo.concurrent.queue.CallQueueMgr;
import com.homo.concurrent.schedule.HomoTimerMgr;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 缓存服务器状态信息
 */
@Slf4j
public class ServiceStateHandlerImpl implements ServiceStateHandler, TaskFun0 {
    private Map<String, List<Integer>> goodServiceMap = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> availableServiceMap = new ConcurrentHashMap<>();
    private ServerStateProperties serverStateProperties;
    private ServiceStateMgr stateMgr;
    private Map<String, Integer> serviceRangeConfig;


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

    public ServiceStateHandlerImpl(ServiceStateMgr stateMgr, ServerStateProperties serverStateProperties) {
        this.stateMgr = stateMgr;
        this.serverStateProperties = serverStateProperties;
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        CallQueueMgr.getInstance().frameTask(new Runnable() {
            @Override
            public void run() {
                HomoTimerMgr.getInstance().schedule(ServiceStateHandlerImpl.this, 0,
                        serverStateProperties.getServiceStateUpdatePeriodSeconds(),
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
            updateService(serviceName);
        }
    }

    private void updateService(String serviceName) {
        stateMgr.getServiceAllStateInfo(serviceName)
                .consumerValue(map -> {
                    if (map == null || map.size() == 0) {
                        goodServiceMap.put(serviceName, new ArrayList<>());
                    } else {
                        //缓存符合条件的service
                        int range = serverStateProperties.getGoodStateRange().getOrDefault(serviceName,
                                serverStateProperties.getDefaultRange());
                        ArrayList<Map.Entry<Integer, Integer>> stateList = new ArrayList<>(map.entrySet());
                        stateList.sort(Comparator.comparingInt(Map.Entry::getValue));
                        //寻找goodServices，取负载最优的服务加上基数作为比较值，小于这个值表示服务状态良好
                        List<Integer> goodServices = new ArrayList<>();
                        int limit = stateList.get(0).getValue() + range;
                        for (Map.Entry<Integer, Integer> entry : stateList) {
                            if (entry.getValue() <= limit) {
                                goodServices.add(entry.getKey());
                            } else {
                                break;
                            }
                        }
                        goodServiceMap.put(serviceName, goodServices);
                        List<Integer> aliveServices = stateList.stream().map(Map.Entry::getKey).collect(Collectors.toList());
                        availableServiceMap.put(serviceName, aliveServices);
                    }
                }).start();
    }


    @Override
    public Homo<String> getServiceNameByTag(String tag) {
        return null;
    }

    @Override
    public Homo<Boolean> setServiceNameTag(String tag, String serviceName) {
        return null;
    }

    @Override
    public void setLocalServiceNameTag(String tag, String serviceName) {

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
        return Homo.result(choiceFun.apply(serviceName, goodServiceMap.get(serviceName)));
    }

    @Override
    public Homo<Map<Integer, Integer>> getServiceAllStateInfo(String serviceName) {
        return stateMgr.getServiceAllStateInfo(serviceName);
    }

    public void setChoiceFun(BiFunction<String, List<Integer>, Integer> choiceFun) {
        this.choiceFun = choiceFun;
    }
}
