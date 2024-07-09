package com.homo.service.state.driver.redis;

import brave.Span;
import com.homo.core.facade.service.LoadInfo;
import com.homo.core.facade.service.StatefulDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
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
    public Homo<Boolean> setLinkedPod(String appId, String regionId, String logicType, String ownerId, String serviceName, int podId, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, ownerId, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, ownerId);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(podId), String.valueOf(persistSeconds)};
        String statefulSetLink = LuaScriptHelper.statefulSetLink;
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("setLinkedPod").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Boolean> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(statefulSetLink, keys, args)
                        .subscribe(ret -> {
                            Long rel = (Long) ((List) ret).get(0);
                            log.trace("setLinkedPod  appId {} regionId {} logicType {} ownerId {} serviceName {} podId {} persistSeconds {} rel {}", appId, regionId, logicType, ownerId, serviceName, podId, persistSeconds, rel);
                            span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                            homoSink.success(rel == 1L);
                        }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Integer> setLinkedPodIfAbsent(String appId, String regionId, String logicType, String ownerId, String serviceName, int podId, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, ownerId, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, ownerId);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(podId), String.valueOf(persistSeconds)};
        String statefulSetLinkIfAbsent = LuaScriptHelper.statefulSetLinkIfAbsent;
        log.info("setLinkedPodIfAbsent start appId {} regionId {} logicType {} serviceName {} ownerId {} podId {} persistSeconds {} ", appId, regionId, logicType, serviceName, ownerId, podId, persistSeconds);
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("setLinkedPodIfAbsent").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Integer> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(statefulSetLinkIfAbsent, keys, args)
                        .subscribe(ret -> {
                            String rel = (String) ((List) ret).get(0);
                            log.info("setLinkedPodIfAbsent end appId {} regionId {} logicType {} serviceName {} ownerId {} podId {} persistSeconds {} rel {}", appId, regionId, logicType, serviceName, ownerId, podId, persistSeconds, rel);
                            span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                            homoSink.success(Integer.parseInt(rel));
                        }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Integer> getLinkedPod(String appId, String regionId, String logicType, String ownerId, String serviceName) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, ownerId, serviceName);
        String[] keys = {uidSvcKey};
        String[] args = nullArgs;
        String statefulGetLink = LuaScriptHelper.statefulGetLink;
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("getLinkedPod").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Integer> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(statefulGetLink, keys, args)
                        .subscribe(ret -> {
                            String podIdStr = (String) ((List) ret).get(0);
                            Integer rel = Integer.valueOf(podIdStr);
                            log.trace("getLinkedPod  appId {} regionId {} logicType {} serviceName {} ownerId {} rel {}", appId, regionId, logicType, serviceName, ownerId, rel);
                            span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                            homoSink.success(rel);
                        }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Boolean> removeLinkedPod(String appId, String regionId, String logicType, String ownerId, String serviceName, int persistSeconds) {
        String uidSvcKey = String.format(USER_SERVICE_TEMP, appId, regionId, logicType, ownerId, serviceName);
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, ownerId);
        String[] keys = {uidSvcKey, uidKey};
        String[] args = {String.valueOf(serviceName), String.valueOf(persistSeconds)};
        String statefulRemoveLink = LuaScriptHelper.statefulRemoveLink;
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("removeLinkedPod").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Boolean> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(statefulRemoveLink, keys, args)
                        .subscribe(ret -> {
                            List<Object> resultList = (List<Object>) ret;
                            Long rel = (Long) resultList.get(0);
                            span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                            if (rel > 0) {
                                homoSink.success(true);
                            } else {
                                homoSink.success(false);
                            }
                            log.info("removeLinkedPod  appId {} regionId {} logicType {} serviceName {} ownerId {} rel {}", appId, regionId, logicType, serviceName, ownerId, rel);

                        }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Map<String, Integer>> getAllLinkService(String appId, String regionId, String logicType, String ownerId) {
        String uidKey = String.format(USER_TEMP, appId, regionId, logicType, ownerId);
        String[] keys = {uidKey};
        String[] args = nullArgs;
        String getAllLinkService = LuaScriptHelper.getAllLinkService;
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("getAllLinkService").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Map<String, Integer>> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(getAllLinkService, keys, args)
                        .subscribe(ret -> {
                            List<String> rel = (List<String>) ret;
                            Map<String, Integer> map = new HashMap<>(8);
                            for (int i = 0; i < rel.size(); i += 2) {
                                map.put(rel.get(i), Integer.valueOf(rel.get(i + 1)));
                            }
                            span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                            homoSink.success(map);
                            log.trace("getAllLinkService  appId {} regionId {} logicType {}  ownerId {} rel {}", appId, regionId, logicType, ownerId, rel);
                        }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Boolean> setServiceState(String appId, String regionId, String logicType, String serviceName, int podId, int load, int state) {
        log.debug("setServiceState start appId {} regionId {} logicType {} serviceName {} podId {} load {} state {}",
                appId, regionId, logicType, serviceName, podId, load, state);
        String stateQueryKey = String.format(SERVICE_STATE_TEMP, appId, regionId, logicType, serviceName);
        String podNumStr = String.valueOf(podId);
        String loadTimestamp = LoadInfo.join(load, System.currentTimeMillis(), state);
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(podNumStr, loadTimestamp);
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("setServiceState").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Boolean> warp = Homo.warp(
                        asyncRedisPool.hsetAsyncReactive(stateQueryKey, dataMap))
                .nextDo(ret -> {
                    log.trace("setServiceState ret appId {} regionId {} logicType {} serviceName {} podId {} load {} state {} loadTimestamp {} ret {}",
                            appId, regionId, logicType, serviceName, podId, load, state, loadTimestamp, ret
                    );
                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                    if (ret != null) {
                        return Homo.result(true);
                    } else {
                        log.error("setServiceState error appId {} regionId {} logicType {} serviceName {} podId {} load {} state {} loadTimestamp {}",
                                appId, regionId, logicType, serviceName, podId, load, state, loadTimestamp
                        );
                        return Homo.result(false);
                    }
                });
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

    @Override
    public Homo<Map<Integer, LoadInfo>> getServiceState(String appId, String regionId, String logicType, String serviceName, long beginTimeMillis) {
        String serviceKey = String.format(SERVICE_STATE_TEMP, appId, regionId, logicType, serviceName);
        String[] keys = {serviceKey};
        String[] args = nullArgs;
        String getServiceState = LuaScriptHelper.getServiceState;
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("getServiceState").tag("type","state").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Map<Integer, LoadInfo>> warp = Homo.warp(homoSink ->
                asyncRedisPool.evalAsyncReactive(getServiceState, keys, args)
                .subscribe(ret -> {
                    List<String> rel = (List<String>) ret;
                    Map<Integer, LoadInfo> map = new HashMap<>(8);
                    for (int i = 0; i < rel.size(); i += 2) {
                        LoadInfo loadInfo = LoadInfo.build(rel.get(i), rel.get(i + 1));
                        Long timestamp = loadInfo.timestamp;
                        if (timestamp >= beginTimeMillis) {
                            map.put(Integer.valueOf(rel.get(i)), loadInfo);
                        }
                    }
                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                    homoSink.success(map);
                    log.trace("getServiceState  appId {} regionId {} logicType {}  serviceName {} rel {}", appId, regionId, logicType, serviceName, rel);
                }));
        return warp.switchThread(callQueue, span).consumerValue(ret -> span.finish());
    }

}
