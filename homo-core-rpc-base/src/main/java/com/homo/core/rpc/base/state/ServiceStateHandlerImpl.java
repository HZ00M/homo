package com.homo.core.rpc.base.state;

import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.schedule.TaskFun0;
import com.homo.core.common.module.Module;
import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 缓存服务器状态信息
 */
@Log4j2
public class ServiceStateHandlerImpl implements ServiceStateHandler, TaskFun0 , Module {
    private Map<String, List<Integer>> goodServiceMap = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> availableServiceMap = new ConcurrentHashMap<>();
    @Autowired(required = false)
    private ServerStateProperties serverStateProperties;
    @Lazy
    @Autowired(required = false)
    private ServiceStateMgr stateMgr;

    @Override
    public void init() {
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
            updateService(serviceName).start();
        }
    }

    private Homo<Boolean> updateService(String serviceName) {
        return stateMgr.geAllStateInfo(serviceName)
                .nextDo(map -> {
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
                    return Homo.result(true);
                })
                .errorContinue(throwable -> {
                    log.error("updateService error ",throwable);
                    return Homo.result(false);
                });
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
        if (goodServiceMap.get(serviceName)==null){
            //从来没有获取过,去storage获取
            return updateService(serviceName)
                    .nextDo(ret->{
                        if (ret){
                            return Homo.result(choiceFun.apply(serviceName, goodServiceMap.get(serviceName)));
                        }else {
                            return Homo.error(HomoError.throwError(HomoError.choicePodNotFound,serviceName));
                        }
                    });
        }else {
            return Homo.result(choiceFun.apply(serviceName, goodServiceMap.get(serviceName)));
        }

    }

    @Override
    public Homo<Map<Integer, Integer>> getServiceAllStateInfo(String serviceName) {
        return stateMgr.geAllStateInfo(serviceName);
    }

    public void setChoiceFun(BiFunction<String, List<Integer>, Integer> choiceFun) {
        this.choiceFun = choiceFun;
    }


}
