package com.homo.core.mq.consumer.route;

import com.homo.core.facade.mq.consumer.ConsumerCallback;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import com.homo.core.facade.mq.consumer.SinkHandler;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个消息分发路由管理器。
 * 只提供注册功能。保存每一个真实的topic对应的处理函数。
 * 这些函数有从sink中来，有些从handler中的注解来 对外提供router函数，
 * 返回某个topic的所有订阅方法列表
 */
@Slf4j
public class RouterMgr {
    /**
     * 缓存真实的Topic，避免重复创建
     */
    final Map<String, String> originToRealTopicCacheMap = new ConcurrentHashMap<>();
    /**
     * 保存所有订阅该topic的方法引用，key为真是的Topic
     */
    final Map<String, List<RouteTarget>> topicRouter = new ConcurrentHashMap<>();
//    /**
//     *缓存消息的class匹配的订阅函数，避免每条消息到达时都要路由
//     */
//    final Map<String,Map<Class<?>,List<RouteInfo>>> topicMessageClazzRouter = new ConcurrentHashMap<>();

    public synchronized <T extends Serializable> void register(@NotNull String realTopic, @NotNull ReceiverSink<T> sink) throws Exception {
        List<RouteTarget> routeTargets = topicRouter.computeIfAbsent(realTopic, k -> new ArrayList<>());
        routeTargets.add(new RouteTarget(sink));
    }

    public synchronized void register(@NotNull String realTopic, @NotNull SinkHandler handler, @NotNull Method func) {
        List<RouteTarget> routeTargets = topicRouter.computeIfAbsent(realTopic, k -> new ArrayList<>());
        routeTargets.add(new RouteTarget(handler, func));
    }

    /**
     * 获取topic，以及签名匹配消息的订阅函数
     *
     * @param realTopic
     * @param message
     * @param <T>
     * @return
     */
    public  <T extends java.io.Serializable> @NotNull void topicRouter(@NotNull String realTopic, @NotNull T message, ConsumerCallback callback) {
        List<RouteTarget> topicRouteList = topicRouter.get(realTopic);
        for (RouteTarget routeTarget : matchMessage(topicRouteList, message.getClass())) {
            CallQueueMgr.getInstance().task(()->{
                try {
                    routeTarget.invoke(realTopic, message, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        callback.confirm();
                    }
                    log.error("topicRouter message route invoke has error realTopic {} routeTarget {} {}",
                            realTopic, routeTarget, e);
                }
            });
        }
    }

    /**
     * 返回匹配消息类型的路由信息
     *
     * @param topicRouteList
     * @param messageClazz
     * @return
     */
    private List<RouteTarget> matchMessage(List<RouteTarget> topicRouteList, Class<? extends Serializable> messageClazz) {
        List<RouteTarget> matchRouteList = new ArrayList<>();
        if (topicRouteList != null) {
            for (RouteTarget routeTarget : topicRouteList) {
                if (routeTarget.getMessageClazz().isAssignableFrom(messageClazz)) {
                    matchRouteList.add(routeTarget);
                }
            }
        }
        return matchRouteList;
    }

}
