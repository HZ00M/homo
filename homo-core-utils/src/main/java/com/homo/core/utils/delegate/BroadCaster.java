package com.homo.core.utils.delegate;

import com.alibaba.fastjson.annotation.JSONField;
import com.homo.core.utils.exception.HomoError;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 广播者  用于在同一线程内广播消息到目标
 *
 * @param <Target>目标
 */
@Log4j2
public abstract class BroadCaster<Target> {
    public Integer minOrder = 0;
    public Integer maxOrder = 1;
    /**
     * 有序的目标列表 key-优先级 value-目标列表 key值低的优先执行
     */
    @JSONField(ordinal = 1)// 保证序列化顺序，含有set的字段必须先序列化，不然序列化会有信息丢失（应该是fastjson的bug）
    private final Map<Integer, Set<Target>> orderTargetsMap = Collections.synchronizedSortedMap(new TreeMap<>());

    /**
     * 目标字典 key-目标 value-优先级 用于快速定位 target在orderTargetsMap 的槽位
     */
    @JSONField(ordinal = 2)
    private final Map<Target, Integer> bindTargets = new ConcurrentHashMap<>();

    /**
     * 新绑定的目标列表  key-目标 value-优先级
     */
    @JSONField(ordinal = 3)
    private final Map<Target, Integer> newBindTargets = new ConcurrentHashMap<>();

    /**
     * 需要解绑的目标列表
     */
    @JSONField(ordinal = 4)
    public Map<Target, Target> unBindTargets = new ConcurrentHashMap<>();

    abstract boolean run(Target target, Object... objects) throws Exception;


    /**
     * 广播消息
     *
     * @param objects 广播内容
     * @return 返回是否处理了消息
     * @throws Exception
     */
    public boolean broadcast(Object... objects) throws Exception {
        //先将内容广播给已绑定的目标处理，处理期间可能会有新的绑定目标
        broadcastToBind(objects);
        //将事件交给前面产生的新绑定目标处理，直到不再有新的绑定目标
        broadcastToNewBind(objects);
        processUnbindTargets();
        return true;
    }

    private void broadcastToBind(Object... objects) throws Exception {
        newBindTargets.forEach((target, order) -> {
            orderTargetsMap.computeIfAbsent(order, o -> new HashSet<>()).add(target);
        });
        newBindTargets.clear();
        processBroadcast(orderTargetsMap,objects);
    }

    private void processBroadcast(Map<Integer, Set<Target>> processMap,Object[] objects) throws Exception {
        for (Set<Target> targetSet : processMap.values()) {
            for (Target target : targetSet) {
                boolean done = doBroadcast(target, objects);
                if (!done) {
                    log.info("broadcast error {}", target);
                }
            }
        }
    }

    private void processUnbindTargets() {
        unBindTargets.values().forEach(this::unbind);
    }

    private void broadcastToNewBind(Object... objects) throws Exception{
        int count = 0;

        while (!newBindTargets.isEmpty() && count < 10000) {
            Map<Integer, Set<Target>> processNewMap = new TreeMap<>();
            for (Map.Entry<Target, Integer> entry : newBindTargets.entrySet()) {
                Integer order = entry.getValue();
                Target target = entry.getKey();
                unBindTargets.remove(target);
                bindTargets.put(target, order);
                orderTargetsMap.computeIfAbsent(order, order1 -> new HashSet<>()).add(target);
                processNewMap.computeIfAbsent(order,order1->new HashSet<>()).add(target);
            }
            count += newBindTargets.size();//限制传播次数
            newBindTargets.clear();//处理新绑定目标时可能又会产生更新目标，处理时需要先将newBindTargets清空
            processBroadcast(processNewMap,objects);
        }

    }

    private boolean doBroadcast(Target target, Object... objects) throws Exception {
        if (!unBindTargets.containsKey(target)) {
            run(target, objects);
            if (!loop()) {//如果只需广播一次 ，将该方法覆写为false
                unbind(target);
            }
        }
        return false;
    }

    protected boolean loop() {
        return true;
    }


    public void bindToHead(Target target) {
        bind(target, minOrder);
    }

    public void bindToTail(Target target) {
        bind(target, maxOrder);
    }

    /**
     * 注意，只传递函数签名时每次都是new一个对象，也就是说会重复订阅，如不重复订阅请声明一个lambda函数对象传入
     * @param target 目标对象
     * @param order 优先级
     */
    public void bind(Target target, int order) {
        if (bindTargets.containsKey(target)){
            log.info("repeat bind {}",target);
            return;
        }
        if (order <= minOrder) {
            minOrder = order - 1;
        }else if (order >= maxOrder) {
            maxOrder = order + 1;
        }
        unBindTargets.remove(target);
        bindTargets.put(target, order);
        //orderTargetsMap.computeIfAbsent(order, o -> new HashSet<>()).add(target);     //不能在这里直接绑定，广播时有可能出现并发修改异常
        newBindTargets.put(target,order);
    }

    public void unbind(Target target) {
        Optional.ofNullable(bindTargets.get(target))
                .ifPresent(order -> {
                    bindTargets.remove(target);
                    Optional.ofNullable(orderTargetsMap.get(order))
                            .ifPresent(targets -> {
                                targets.remove(target);
                            });
                });
    }

    public boolean isEmpty() {
        return newBindTargets.isEmpty() && bindTargets.isEmpty();
    }

    public void append(BroadCaster<Target> other) {
        if (other == this) {
            throw HomoError.throwError(HomoError.broadcastError, "replace self error");
        }
        other.newBindTargets.forEach(this::bind);
        other.bindTargets.forEach(this::bind);
    }

    public void replace(Target target, Integer order) {
        clear();
        bind(target, order);
    }

    public void clear() {
        minOrder = 0;
        maxOrder = 0;
        newBindTargets.clear();
        bindTargets.clear();
        orderTargetsMap.clear();
        unBindTargets.clear();
    }
}
