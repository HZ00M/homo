package com.homo.core.rpc.base.state;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.facade.module.RootModule;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.facade.service.StatefulDriver;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 管理服务器状态实现类
 */
@Slf4j
public class ServiceStateMgrImpl implements ServiceStateMgr {
    @Autowired
    HomoSerializationProcessor homoSerializationProcessor;
    @Autowired(required = false)
    private ServerStateProperties serverStateProperties;
    @Autowired(required = false)
    private ServiceMgr serviceMgr;
    @Autowired(required = false)
    private StatefulDriver statefulDriver;
    @Autowired
    CacheDriver cacheDriver;
    @Lazy
    @Autowired(required = false)
    private ServiceStateMgr stateMgr;
    boolean isStateful = false;
    //有状态服务当前状态（值越高代表压力越大）
    private int load;
    private String podName;
    private Integer podIndex;
    private RootModule rootModule;
    private long lastUpdateStateTime;
    private final String stateLogicType = "state";
    private final String POD_INDEX_CACHE = "%s-%s";
    /**
     * (user+service) : podIndex
     * 请求用户服务的用户和服务对应的podIndex
     */
    private Cache<String, Integer> localUserServicePodCache;
    private Supplier<Integer> loadFun = new Supplier<Integer>() {
        @Override
        public Integer get() {
            Float floatValue = new Float(serverStateProperties.getCpuFactor() * CallQueueMgr.getInstance().getAllWaitCount() + (1 - serverStateProperties.getCpuFactor() * load));
            return floatValue.intValue();
        }
    };

    @Override
    public void init(RootModule rootModule) {
        this.rootModule = rootModule;
        isStateful = getServerInfo().isStateful;
        load = 0;
        localUserServicePodCache = Caffeine.newBuilder()
                .expireAfterWrite(serverStateProperties.getLocalUserServicePodCacheSecond(), TimeUnit.SECONDS)
                .build();
        if (!isStateful) {
            // 不是有状态服务器，不需要初始化状态管理
            return;
        }
        exportPodInfo();
        checkProperties();
        scheduleUpdateLoad();
        scheduleUpdateService();
    }

    public void setLoadFun(Supplier<Integer> loadFun) {
        this.loadFun = loadFun;
    }

    private void scheduleUpdateLoad() {//todo 服务端没有更新
        CallQueueMgr.getInstance().frameTask(new Runnable() {
            @Override
            public void run() {
                HomoTimerMgr.getInstance().schedule("scheduleUpdateLoad", new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if (lastUpdateStateTime > 0 && (currentTime - lastUpdateStateTime) >= serverStateProperties.getServiceStateExpireMillSeconds() * 1000) {
                            log.error("setState period larger than expireSeconds {}!lastTime {}, currentTime {}",
                                    serverStateProperties.getServiceStateExpireMillSeconds(), lastUpdateStateTime, currentTime);
                        }
                        lastUpdateStateTime = currentTime;
                        String serviceName = serviceMgr.getMainService().getHostName();
                        int weightLoad = loadFun.get();
                        String appId = rootModule.getServerInfo().appId;
                        String regionId = rootModule.getServerInfo().namespace;
                        statefulDriver.setServiceState(appId, regionId, stateLogicType, serviceName, podIndex, weightLoad)
                                .consumerValue(ret -> {
                                    if (ret) {
                                        log.info("set service state success, service {}, load {} weightLoad {}", serviceName, load, weightLoad);
                                    } else {
                                        log.error("set service state fail, service {}, load {} weightLoad {}", serviceName, load, weightLoad);
                                    }
                                })
                                .catchError(throwable -> {
                                    log.error("set service state error, service {}, load {} weightLoad {}", serviceName, load, weightLoad, throwable);

                                })
                                .start();
                    }
                }, 10000, serverStateProperties.getServiceStateUpdatePeriodMillSeconds(), HomoTimerMgr.UNLESS_TIMES);
            }
        });
    }

    /**
     * 初始化服务器状态,定时更新状态
     */
    private void checkProperties() {
        if (serverStateProperties.getCpuFactor() < 0 || serverStateProperties.getCpuFactor() > 1) {
            log.error("homo.service.state.cpu.factor config need between 0 - 1");
            System.exit(-1);
        }
        if (serverStateProperties.getServiceStateUpdatePeriodMillSeconds() > serverStateProperties.getServiceStateExpireMillSeconds() ||
                serverStateProperties.getServiceStateUpdatePeriodMillSeconds() <= 0 ||
                serverStateProperties.getServiceStateExpireMillSeconds() < 0
        ) {
            log.error("please check homo.service.state.expire.seconds and homo.service.state.update.seconds value is sense");
            System.exit(-1);
        }
    }

    private void exportPodInfo() {
        this.podName = rootModule.getPodName();
        if (podName != null) {
            String[] nameArray = this.podName.split("-");
            podIndex = Integer.parseInt(nameArray[nameArray.length - 1]);
        }
        if (podIndex == null) {
            log.error("can't get pod index from podName! please check the POD_NAME env variable!");
        }
    }

    @Override
    public boolean isStateful() {
        return isStateful;
    }

    @Override
    public void setLoad(Integer load) {
        this.load = load;
    }

    @Override
    public Integer getLoad() {
        return load;
    }

    @Override
    public String getPodName() {
        return podName;
    }

    @Override
    public Integer getPodIndex() {
        return podIndex;
    }

    /**
     * 获取唯一id下指定服务的连接index
     *
     * @param uid         唯一id
     * @param serviceName 服务名
     */
    @Override
    public Homo<Integer> setUserLinkedPod(String uid, String serviceName, Integer podIndex, boolean persist) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        Integer expireSecond;
        if (persist) {
            expireSecond = StatefulDriver.PERSIST_FOREVER;
        } else {
            expireSecond = serverStateProperties.getRemoteUserServicePodCacheSecond();
        }
        return statefulDriver.setLinkedPodIfAbsent(appId, regionId, logicType, uid, serviceName, podIndex, expireSecond);
    }

    @Override
    public Homo<Integer> getLinkedPod(String uid, String serviceName) {
        String key = String.format(POD_INDEX_CACHE, uid, serviceName);
        Integer userPodIndex = localUserServicePodCache.getIfPresent(key);
        if (userPodIndex != null) {
            log.info("getLinkedPod getCache uid {} serviceName {} index {}", uid, serviceName, userPodIndex);
            return Homo.result(userPodIndex);
        }
        return getUserLinkedPodNoCache(uid, serviceName)
                .consumerValue(ret -> {
                    log.info("getLinkedPod getCache uid {} serviceName {} index {}", uid, serviceName, ret);
                });
    }

    @Override
    public Homo<Integer> getUserLinkedPodNoCache(String uid, String serviceName) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        return statefulDriver.getLinkedPod(appId, regionId, logicType, uid, serviceName)
                .consumerValue(ret -> {
                    log.trace("getUserLinkedPodNoCache uid {} serviceName {} ret {}", uid, serviceName, ret);
                });
    }

    /**
     * 获取唯一id下指定服务的连接index
     * 如果不存在,路由一个可用pod
     *
     * @param uid         唯一id
     * @param serviceName 服务名
     * @param persist     是否需要永久保留连接信息,false只保留连接信息一段时间.
     *                    (只有在对应连接信息不存在时此参数才有效,一般RRC请求方置为false即可)
     */
    @Override
    public Homo<Integer> computeUserLinkedPodIfAbsent(String uid, String serviceName, Boolean persist) {
        String key = String.format(POD_INDEX_CACHE, uid, serviceName);
        String tag = persist ? key + persist : key;
        Integer userPodIndex = localUserServicePodCache.getIfPresent(key);
        if (userPodIndex != null) {
            if (!isPodAvailable(serviceName, userPodIndex)) {
                log.error("computeLinkedPodIfAbsent 0 uid {}, serviceName {} pod {} is DEAD!, alive pods: {}",
                        uid, serviceName, userPodIndex, JSON.toJSONString(alivePods(serviceName)));
                return Homo.result(null);
            }
            return Homo.result(userPodIndex);
        }
        return choiceBestPod(serviceName)
                .nextDo(choicePodIndex -> {
                    if (choicePodIndex != null) {
                        return setUserLinkedPod(uid, serviceName, choicePodIndex, persist)
                                .nextDo(currentPodIndex -> {
                                    //返回的pod已经不可用了, 返回空
                                    if (!isPodAvailable(serviceName, currentPodIndex)) {
                                        log.error("computeUserLinkedPodIfAbsent uid {}, serviceName {} pod {} is DEAD!, alive pods: {}",
                                                uid, serviceName, currentPodIndex, JSON.toJSONString(alivePods(serviceName)));
                                        return Homo.result(null);
                                    } else {
                                        //返回的pod和选取的pod一致
                                        localUserServicePodCache.put(key, currentPodIndex);
                                        return Homo.result(currentPodIndex);
                                    }
                                });
                    } else {
                        //没有可用的pod
                        log.error("no best pod for uid {}, serviceName {}", uid, serviceName);
                        return Homo.result(null);
                    }
                }).zipCalling(tag);
    }

    /**
     * 移除唯一id下指定服务的连接index
     *
     * @param uid         唯一id
     * @param serviceName 服务名
     * @param immediately 是否立即删除连接信息,true立即删除(false设置过期时间，默认1分钟，有的场景不应该立即删除，比如并发情况)
     */
    @Override
    public Homo<Boolean> removeUserLinkedPod(String uid, String serviceName, Boolean immediately) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        int persistTime = immediately ? StatefulDriver.DELETE_NOW : serverStateProperties.getRemoteUserServicePodDelayRemoveSecond();
        return statefulDriver.removeLinkedPod(appId, regionId, logicType, uid, serviceName, persistTime)
                .consumerValue(ret -> {
                    log.trace("removeUserLinkedPod uid {} serviceName {} ret {}", uid, serviceName, ret);
                });
    }

    /**
     * 获取唯一id下所有连接的服务
     *
     * @param uid
     * @return
     */
    @Override
    public Homo<Map<String, Integer>> getAllUserLinkInfo(String uid) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        return statefulDriver.getAllLinkService(appId, regionId, logicType, uid)
                .consumerValue(ret -> {
                    log.trace("getAllUserLinkInfo uid {}  ret {}", uid, ret);
                })
                ;
    }


    @Override
    public Homo<Map<Integer, Integer>> geAllStateInfo(String serviceName) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        long beginTimeMillis = System.currentTimeMillis() - serverStateProperties.getServiceStateExpireMillSeconds() * 1000;
        return statefulDriver.getServiceState(appId, regionId, logicType, serviceName, beginTimeMillis)
                .consumerValue(ret -> {
                    log.trace("getServiceAllStateInfo serviceName {}  ret {}", serviceName, ret);
                });
    }

    static final String SERVICE_INFO_LOGIC_TYPE = "state";
    static final String SERVICE_INFO_KEY = "serverInfo";
    private Map<String, List<Integer>> goodServiceMap = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> availableServiceMap = new ConcurrentHashMap<>();

    Map<String, ServiceInfo> tagToServiceInfoMap = new ConcurrentHashMap<>();


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


    private void scheduleUpdateService() {
        CallQueueMgr.getInstance().frameTask(new Runnable() {
            @Override
            public void run() {
                HomoTimerMgr.getInstance().schedule("scheduleUpdateService", new Runnable() {
                            @Override
                            public void run() {
                                log.debug("state cache update");
                                if (goodServiceMap.size() == 0) {
                                    return;
                                }
                                for (String serviceName : goodServiceMap.keySet()) {
                                    updateSGoodServiceCache(serviceName).start();
                                }
                            }
                        }, 0,
                        serverStateProperties.getServiceStateUpdatePeriodMillSeconds(),
                        HomoTimerMgr.UNLESS_TIMES);
            }
        });
    }


    private Homo<Boolean> updateSGoodServiceCache(String serviceName) {
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
                    log.error("updateService error ", throwable);
                    return Homo.result(false);
                });
    }


    @Override
    public Homo<ServiceInfo> getServiceInfo(String tag) {
        ServiceInfo inMen = tagToServiceInfoMap.get(tag);
        if (inMen != null) {
            return Homo.result(inMen);
        }
        CallQueue localQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan();
        Homo<ServiceInfo> warp = cacheDriver.asyncGetAll(getServerInfo().appId, getServerInfo().regionId, SERVICE_INFO_LOGIC_TYPE, SERVICE_INFO_KEY)
                .nextDo(ret -> {
                    Map<String, byte[]> stringMap = ret;

                    byte[] tagBytes = stringMap.get(tag);
                    if (tagBytes == null) {
                        // 如果找不到tag先按原tag返回
                        log.warn("tag {} of service not found! return tag!", tag);
                        return Homo.result(null);
                    }
                    ServiceInfo serviceInfo = homoSerializationProcessor.readValue(tagBytes, ServiceInfo.class);
                    tagToServiceInfoMap.put(tag, serviceInfo);
                    return Homo.result(serviceInfo);
                })
                .catchError(throwable -> {
                    log.error("get tag {} error!", tag, throwable);
                });
        return warp.switchThread(localQueue,span).consumerValue(ret->span.finish());
    }

    @Override
    public Homo<Boolean> setServiceInfo(String tag, ServiceInfo serviceInfo) {
        log.info("setServiceNameTag start tag {} serviceInfo {} ", tag, serviceInfo);
        setLocalServiceInfo(tag, serviceInfo);
        CallQueue localQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span storageSpan = ZipkinUtil.getTracing().tracer().nextSpan();
        return cacheDriver.asyncGetAll(getServerInfo().appId, getServerInfo().regionId, SERVICE_INFO_LOGIC_TYPE, SERVICE_INFO_KEY)
                .nextDo(map -> {
                    byte[] serverInfoBytes = homoSerializationProcessor.writeByte(serviceInfo);
                    map.put(tag, serverInfoBytes);
                    return cacheDriver.asyncUpdate(getServerInfo().appId, getServerInfo().regionId, SERVICE_INFO_LOGIC_TYPE, SERVICE_INFO_KEY, map)
                            .consumerValue(ret -> {
                                log.info("setServiceNameTag ret serviceInfo {} serviceName {} tag {} ret {}", SERVICE_INFO_KEY, serviceInfo, tag, ret);
                            });
                })
                .switchThread(localQueue,storageSpan);
    }

    @Override
    public void setLocalServiceInfo(String tag, ServiceInfo serviceInfo) {
        tagToServiceInfoMap.put(tag, serviceInfo);
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
            return updateSGoodServiceCache(serviceName)
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
    public Homo<Map<Integer, Integer>> getServiceAllStateInfo(String serviceName) {
        return stateMgr.geAllStateInfo(serviceName);
    }

    public void setChoiceFun(BiFunction<String, List<Integer>, Integer> choiceFun) {
        this.choiceFun = choiceFun;
    }
}
