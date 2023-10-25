package com.homo.service.state.driver.redis;

import com.homo.core.facade.service.StatefulDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.fun.ConsumerEx;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 有状态driver
 * 不落地
 * 保证一些步骤的原子性
 */
@Slf4j
public class StatefulDriverRedisImpl implements StatefulDriver {

    @Autowired(required = false)
    @Qualifier("homoRedisPool")
    private HomoAsyncRedisPool asyncRedisPool;

    private static final String SERVICE_STATE_TEMP = "slug-state:{%s:%s:%s:%s}";//服务器负载信息
    private static final String USER_TEMP = "slug-lk:{%s:%s:%s:%s}";//用户与所有服务连接信息
    private static final String USER_SERVICE_TEMP = "slug-lk:{%s:%s:%s:%s:%s}";//用户与服务连接信息
    public static String[] nullArgs = new String[]{};

    @Override
    public Homo<Boolean> setLinkedPod(String appId, String regionId, String logicType, String uid, String serviceName, int podId, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, uid, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, uid);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(podId), String.valueOf(persistSeconds)};
        String statefulSetLink = LuaScriptHelper.statefulSetLink;
        return Homo.warp(new ConsumerEx<HomoSink<Boolean>>() {
            @Override
            public void accept(HomoSink<Boolean> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(statefulSetLink, keys, args)
                        .subscribe(ret -> {
                            Long rel = (Long) ((List) ret).get(0);
                            log.trace("setLinkedPod  appId {} regionId {} logicType {} uid {} serviceName {} podId {} persistSeconds {} rel {}", appId, regionId, logicType, uid, serviceName, podId, persistSeconds, rel);
                            homoSink.success(rel==1L);
                        });
            }
        });
    }

    @Override
    public Homo<Integer> setLinkedPodIfAbsent(String appId, String regionId, String logicType, String uid, String serviceName, int podId, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, uid, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, uid);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(podId), String.valueOf(persistSeconds)};
        String statefulSetLinkIfAbsent = LuaScriptHelper.statefulSetLinkIfAbsent;
        log.info("setLinkedPodIfAbsent start appId {} regionId {} logicType {} serviceName {} uid {} podId {} persistSeconds {} ", appId, regionId, logicType, serviceName, uid, podId, persistSeconds);

        return Homo.warp(new ConsumerEx<HomoSink<Integer>>() {
            @Override
            public void accept(HomoSink<Integer> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(statefulSetLinkIfAbsent, keys, args)
                        .subscribe(ret -> {
                            String rel = (String) ((List) ret).get(0);
                            log.info("setLinkedPodIfAbsent end appId {} regionId {} logicType {} serviceName {} uid {} podId {} persistSeconds {} rel {}", appId, regionId, logicType, serviceName, uid, podId, persistSeconds, rel);
                            homoSink.success(Integer.parseInt(rel));
                        });
            }
        });
    }

    @Override
    public Homo<Integer> getLinkedPod(String appId, String regionId, String logicType, String uid, String serviceName) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, uid, serviceName);
        String[] keys = {uidSvcKey};
        String[] args = nullArgs;
        String statefulGetLink = LuaScriptHelper.statefulGetLink;
        return Homo.warp(new ConsumerEx<HomoSink<Integer>>() {
            @Override
            public void accept(HomoSink<Integer> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(statefulGetLink, keys, args)
                        .subscribe(ret -> {
                            String podIdStr = (String) ((List) ret).get(0);
                            Integer rel = Integer.valueOf(podIdStr);
                            log.trace("getLinkedPod  appId {} regionId {} logicType {} serviceName {} uid {} rel {}", appId, regionId, logicType, serviceName, uid, rel);
                            homoSink.success(rel);
                        });
            }
        });
    }

    @Override
    public Homo<Boolean> removeLinkedPod(String appId, String regionId, String logicType, String uid, String serviceName, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, uid, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, uid);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(persistSeconds)};
        String statefulRemoveLink = LuaScriptHelper.statefulRemoveLink;
        return Homo.warp(new ConsumerEx<HomoSink<Boolean>>() {
            @Override
            public void accept(HomoSink<Boolean> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(statefulRemoveLink, keys, args)
                        .subscribe(ret -> {
                            List<Object> resultList = (List<Object>) ret;
                            Long rel = (Long) resultList.get(0);
                            if (rel > 0) {
                                homoSink.success(true);
                            } else {
                                homoSink.success(false);
                            }
                            log.info("removeLinkedPod  appId {} regionId {} logicType {} serviceName {} uid {} rel {}", appId, regionId, logicType, serviceName, uid, rel);

                        });
            }
        });
    }

    @Override
    public Homo<Map<String, Integer>> getAllLinkService(String appId, String regionId, String logicType, String uid) {
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, uid);
        String[] keys = {uidKey};
        String[] args = nullArgs;
        String getAllLinkService = LuaScriptHelper.getAllLinkService;
        return Homo.warp(new ConsumerEx<HomoSink<Map<String, Integer>>>() {
            @Override
            public void accept(HomoSink<Map<String, Integer>> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(getAllLinkService, keys, args)
                        .subscribe(ret -> {
                            List<String> rel = (List<String>) ret;
                            Map<String, Integer> map = new HashMap<>(8);
                            for (int i = 0; i < rel.size(); i += 2) {
                                map.put(rel.get(i), Integer.valueOf(rel.get(i + 1)));
                            }
                            homoSink.success(map);
                            log.trace("getAllLinkService  appId {} regionId {} logicType {}  uid {} rel {}", appId, regionId, logicType, uid, rel);
                        });
            }
        });
    }

    @Override
    public Homo<Boolean> setServiceState(String appId, String regionId, String logicType, String serviceName, int podId, int load) {
        String stateQueryKey = String.format(SERVICE_STATE_TEMP, appId, regionId, logicType, serviceName);
        String podNumStr = String.valueOf(podId);
        String loadTimestamp = LoadTimestamp.join(load, System.currentTimeMillis());
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(podNumStr, loadTimestamp);
        return Homo.warp(asyncRedisPool.hsetAsyncReactive(stateQueryKey, dataMap))
                .nextDo(ret -> {
                    log.trace("setServiceState ret appId {} regionId {} logicType {} serviceName {} podId {} load {} loadTimestamp {} ret {}",
                            appId, regionId, logicType, serviceName, podId, load,loadTimestamp,ret
                    );
                    if (ret != null) {
                        return Homo.result(true);
                    } else {
                        log.error("setServiceState error appId {} regionId {} logicType {} serviceName {} podId {} load {} loadTimestamp {}",
                                appId, regionId, logicType, serviceName, podId, load,loadTimestamp
                        );
                        return Homo.result(false);
                    }
                });
    }

    @Override
    public Homo<Map<Integer, Integer>> getServiceState(String appId, String regionId, String logicType, String serviceName, long beginTimeMillis) {
        String serviceKey = String.format(SERVICE_STATE_TEMP,appId,regionId,logicType,serviceName);
        String[] keys = {serviceKey};
        String[] args = nullArgs;
        String getServiceState = LuaScriptHelper.getServiceState;
        return Homo.warp(new ConsumerEx<HomoSink<Map<Integer, Integer>>>() {
            @Override
            public void accept(HomoSink<Map<Integer, Integer>> homoSink) throws Exception {
                asyncRedisPool.evalAsyncReactive(getServiceState, keys, args)
                        .subscribe(ret -> {
                            List<String> rel = (List<String>) ret;
                            Map<Integer, Integer> map = new HashMap<>(8);
                            for (int i = 0; i < rel.size(); i += 2) {
                                LoadTimestamp loadTimestamp = LoadTimestamp.split(rel.get(i + 1));
                                Long timestamp = loadTimestamp.timestamp;
                                if (timestamp>=beginTimeMillis){
                                    map.put(Integer.valueOf(rel.get(i)), loadTimestamp.load);
                                }
                            }
                            homoSink.success(map);
                            log.trace("getServiceState  appId {} regionId {} logicType {}  serviceName {} rel {}", appId, regionId, logicType, serviceName, rel);
                        });
            }
        });
    }

}
