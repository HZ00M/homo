package com.homo.core.rpc.base.state;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.homo.concurrent.queue.CallQueueMgr;
import com.homo.concurrent.schedule.HomoTimerMgr;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.core.common.module.Module;
import com.homo.core.common.module.RootModule;
import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.facade.service.StatefulDriver;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 管理服务器状态实现类
 */
@Slf4j
public class ServiceStateMgrImpl implements ServiceStateMgr, Module {
    @Autowired(required = false)
    private ServerStateProperties serverStateProperties;
    @Autowired(required = false)
    private ServiceMgr serviceMgr;
    @Autowired(required = false)
    private StatefulDriver statefulDriver;
    @Autowired(required = false)
    private ServiceStateHandler serviceStateHandler;
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
     */
    private Cache<String,Integer> localUserServicePodCache;
    private  Supplier<Integer> loadFun = new Supplier<Integer>() {
        @Override
        public Integer get() {
            return new Float(serverStateProperties.getCpuFactor() * CallQueueMgr.getInstance().getAllWaitCount()+(1-serverStateProperties.getCpuFactor()*load)).intValue();
        }
    };

    @Override
    public void init(RootModule rootModule){
        this.rootModule = rootModule;
        isStateful = getServerInfo().isStateful;
        load = 0;
        if (!isStateful){
            // 不是有状态服务器，不需要初始化状态管理
            return;
        }
        localUserServicePodCache = Caffeine.newBuilder()
                .expireAfterWrite(serverStateProperties.getLocalUserServicePodCacheSecond(), TimeUnit.SECONDS)
                .build();
        exportPodInfo();
        checkProperties();
        scheduleUpdate();
    }

    public void setLoadFun(Supplier<Integer> loadFun) {
        this.loadFun = loadFun;
    }

    private void scheduleUpdate() {
        CallQueueMgr.getInstance().frameTask(new Runnable() {
            @Override
            public void run() {
                HomoTimerMgr.getInstance().schedule(new TaskFun0() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if (lastUpdateStateTime>0 && (currentTime -lastUpdateStateTime) >= serverStateProperties.getServiceStateExpireSeconds()*1000){
                            log.error("setState period larger than expireSeconds_{}!lastTime_{}, currentTime_{}",
                                    serverStateProperties.getServiceStateExpireSeconds(), lastUpdateStateTime,currentTime);
                        }
                        lastUpdateStateTime =currentTime;
                        String serviceName = serviceMgr.getMainService().getServiceName();
                        int weightLoad = loadFun.get();
                        String appId = rootModule.getServerInfo().appId;
                        String regionId = rootModule.getServerInfo().namespace;
                        statefulDriver.setServiceState(appId,regionId,stateLogicType,serviceName,podIndex,weightLoad)
                                .consumerValue(ret->{
                                    if(ret) {
                                        log.trace("set service state success, service_{}, load_{} weightLoad_{}", serviceName, load,weightLoad);
                                    } else {
                                        log.error("set service state fail, service_{}, load_{} weightLoad_{}",serviceName, load,weightLoad);
                                    }
                                })
                                .catchError(throwable -> {
                                    log.error("set service state error, service_{}, load_{} weightLoad_{}",serviceName, load,weightLoad, throwable);

                                })
                                .start();
                    }
                },1000,serverStateProperties.getServiceStateUpdatePeriodSeconds(),HomoTimerMgr.UNLESS_TIMES);
            }
        });
    }

    /**
     * 初始化服务器状态,定时更新状态
     */
    private void checkProperties() {
        if (serverStateProperties.getCpuFactor()<0||serverStateProperties.getCpuFactor()>1){
            log.error("homo.service.state.cpu.factor config need between 0 - 1");
            System.exit(-1);
        }
        if (serverStateProperties.getServiceStateUpdatePeriodSeconds()>serverStateProperties.getServiceStateExpireSeconds() ||
                serverStateProperties.getServiceStateUpdatePeriodSeconds()<=0||
                serverStateProperties.getServiceStateExpireSeconds()<0
        ){
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
            log.error("cant get pod index frome podName! please check the POD_NAME env variable!");
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
     * @param uid 唯一id
     * @param serviceName 服务名
     */
    @Override
    public Homo<Integer> setUserLinkedPod(String uid, String serviceName, Integer podIndex, boolean persist) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        Integer expireSecond ;
        if (persist){
            expireSecond = StatefulDriver.PERSIST_FOREVER;
        }else {
            expireSecond = serverStateProperties.getRemoteUserServicePodCacheSecond();
        }
        return statefulDriver.setLinkedPod(appId,regionId,logicType,uid,serviceName,podIndex,expireSecond);
    }

    @Override
    public Homo<Integer> getLinkedPod(String uid, String serviceName) {
        String key = String.format(POD_INDEX_CACHE, uid, serviceName);
        Integer userPodIndex = localUserServicePodCache.getIfPresent(key);
        if (userPodIndex!=null){
            return Homo.result(userPodIndex);
        }
        return getUserLinkedPodNoCache(uid,serviceName);
    }

    @Override
    public Homo<Integer> getUserLinkedPodNoCache(String uid, String serviceName) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        return statefulDriver.getLinkedPod(appId,regionId,logicType,uid,serviceName)
                .consumerValue(ret->{
                    log.trace("getUserLinkedPodNoCache uid {} serviceName {} ret {}",uid,serviceName,ret);
                });
    }

    /**
     * 获取唯一id下指定服务的连接index
     * 如果不存在,路由一个可用pod
     * @param uid 唯一id
     * @param serviceName 服务名
     * @param persist 是否需要永久保留连接信息,false只保留连接信息一段时间.
     * (只有在对应连接信息不存在时此参数才有效,一般RRC请求方置为false即可)
     */
    @Override
    public Homo<Integer> computeUserLinkedPodIfAbsent(String uid, String serviceName, Boolean persist) {
        String key = String.format(POD_INDEX_CACHE, uid, serviceName);
        String tag = persist ? key + persist:key;
        Integer userPodIndex = localUserServicePodCache.getIfPresent(key);
        if (userPodIndex!=null){
            if (!serviceStateHandler.isPodAvailable(serviceName,userPodIndex)){
                log.error("computeLinkedPodIfAbsent 0 uid_{}, serviceName_{} pod_{} is DEAD!, alive pods: {}",
                        uid, serviceName, userPodIndex, JSON.toJSONString(serviceStateHandler.alivePods(serviceName)));
                return Homo.result(null);
            }
            return Homo.result(userPodIndex);
        }
        return serviceStateHandler.choiceBestPod(serviceName)
                .nextDo(choicePodIndex->{
                    if (choicePodIndex!=null){
                        return setUserLinkedPod(uid,serviceName,choicePodIndex,persist)
                                .nextDo(currentPodIndex->{
                                    //返回的pod已经不可用了, 返回空
                                    if (!serviceStateHandler.isPodAvailable(serviceName,currentPodIndex)){
                                        log.error("computeUserLinkedPodIfAbsent uid_{}, serviceName_{} pod_{} is DEAD!, alive pods: {}",
                                                uid, serviceName, currentPodIndex, JSON.toJSONString(serviceStateHandler.alivePods(serviceName)));
                                        return Homo.result(null);
                                    }else {
                                        //返回的pod和选取的pod一致
                                        localUserServicePodCache.put(key,currentPodIndex);
                                        return Homo.result(currentPodIndex);
                                    }
                                });
                    }else {
                        //没有可用的pod
                        log.error("no best pod for uid_{}, serviceName_{}", uid, serviceName);
                        return Homo.result(null);
                    }
                }).zipCalling(tag);
    }

    /**
     * 移除唯一id下指定服务的连接index
     * @param uid 唯一id
     * @param serviceName 服务名
     * @param immediately 是否立即删除连接信息,true立即删除(false设置过期时间，默认1分钟，有的场景不应该立即删除，比如并发情况)
     */
    @Override
    public Homo<Boolean> removeUserLinkedPod(String uid, String serviceName, Boolean immediately) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        int persistTime = immediately?StatefulDriver.DELETE_NOW: serverStateProperties.getRemoteUserServicePodDelayRemoveSecond();
        return statefulDriver.removeLinkedPod(appId,regionId,logicType,uid,serviceName,persistTime)
                .consumerValue(ret->{
                    log.trace("removeUserLinkedPod uid {} serviceName {} ret {}",uid,serviceName,ret);
                });
    }

    /**
     * 获取唯一id下所有连接的服务
     * @param uid
     * @return
     */
    @Override
    public Homo<Map<String, Integer>> getAllUserLinkInfo(String uid) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        return statefulDriver.getAllLinkService(appId,regionId,logicType,uid)
                .consumerValue(ret->{
                    log.trace("getAllUserLinkInfo uid {}  ret {}",uid,ret);
                })
                ;
    }


    @Override
    public Homo<Map<Integer, Integer>> getServiceAllStateInfo(String serviceName) {
        String appId = rootModule.getServerInfo().appId;
        String regionId = rootModule.getServerInfo().namespace;
        String logicType = stateLogicType;
        return statefulDriver.getServiceState(appId,regionId,logicType,serviceName,System.currentTimeMillis()-serverStateProperties.getServiceStateExpireSeconds()*1000)
                .consumerValue(ret->{
                    log.trace("getServiceAllStateInfo serviceName {}  ret {}",serviceName,ret);
                });
    }
}
