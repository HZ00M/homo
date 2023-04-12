package com.homo.core.tread.processor;

import com.homo.core.facade.tread.processor.OpPoint;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源操作管理器
 */
@Log4j2
public class ResourceProcesses extends OpQueue {

    boolean executed = false;
    private final Map<ResourceOpType, Integer> opTypeToIndex = new HashMap<>();
    Map<ResourceOpType, Object> opTypeToOwner;
    Map<Class<?>, Object> resourceOwners = new HashMap<>(2);
    Record record = new Record();

    protected ResourceProcesses(Object... resourceOwners) {
        for (Object resourceOwner : resourceOwners) {
            Class<?> ownerClazz = resourceOwner.getClass();
            if (this.resourceOwners.containsKey(ownerClazz)) {
                throw new RuntimeException("repeat ownerClazz declare is ownerType identical,please use setAddOwner() and setSubOwner()!");
            }
            this.resourceOwners.put(ownerClazz, resourceOwner);
        }
    }

    public ResourceProcesses setTraceInfo(String traceInfo) {
        record.traceId = traceInfo;
        return this;
    }

    protected ResourceProcesses setOpTypeOwner(ResourceOpType resourceOpType, Object owner) {
        if (opTypeToOwner == null) {
            opTypeToOwner = new HashMap<>();
        }
        opTypeToOwner.put(resourceOpType, owner);
        return this;
    }


    public Object getOwner(ResourceOpType opType, String resourceType) {
        if (opTypeToOwner == null) {
            return getOwner(resourceType);
        }
        Object owner = opTypeToOwner.get(opType);
        if (owner == null) {
            owner = getOwner(resourceType);
        }
        return owner;
    }


    public Object getOwner(String resourceType) {
        Class<?> ownerClazz = ResourceMgr.getResourceOwnerClazz(resourceType);
        Object owner = resourceOwners.get(ownerClazz);
        return owner;
    }

    protected ResourceProcesses doOp(ResourceOpType resourceOpType, OpPoint opPoint) {
        int index = opTypeToIndex.computeIfAbsent(resourceOpType, s -> {
            int newIndex = opTypeToIndex.size();
            add(new ResourceOpQueue(resourceOpType));
            return newIndex;
        });
        OpQueue opqueue = (OpQueue) getOp(index);
        opqueue.add(opPoint);
        return this;
    }




    /**
     * 添加一个操作
     *
     * @param opValue        操作值
     * @param resourceOpType 操作类型
     * @param resourceType   资源类型
     * @param resourceInfo   资源信息
     * @throws Exception 异常
     */
    protected ResourceProcesses doOp(Object opValue, ResourceOpType resourceOpType, String resourceType, Object... resourceInfo) {
        ResourceOp<?> resourceOp = ResourceMgr.newOpPoint(
                resourceOpType,
                ResourceMgr.getResourceClazz(resourceType));
        initResourceOp(resourceOp,opValue, resourceType,  resourceInfo);
        return doOp(resourceOpType, resourceOp);
    }

    protected void initResourceOp(ResourceOp<?> resourceOp,Object opValue, String resourceType,  Object[] resourceInfo) {
        resourceOp.setOpValue(opValue);
        resourceOp.resourceType = resourceType;
        resourceOp.resourceProcesses = this;
        resourceOp.resourceInfo = resourceInfo;
    }


    @Override
    public Homo<Boolean> exec() throws RuntimeException {
        if (executed) {
            throw new RuntimeException("ResourceProcesses repeat exec!");
        }
        executed = true;
        return super.exec().consumerValue(rel -> {
            record.print();
        });
    }

    @Log4j2
    static class Record {
        ResourceMgr resourceMgr = GetBeanUtil.getBean(ResourceMgr.class);
        public static String leftQuote = "(";
        public static String splitQuote = ":";
        public static String rightQuote = ")";
        public static String nextQuote = "->";
        public static String missQuote = "[/]";
        public String traceId = missQuote;
        StringBuilder recordBuilder;

        public void record(Object... recordInfo) {
            if (resourceMgr.traceEnable) {
                if (recordBuilder == null) {
                    recordBuilder = new StringBuilder();
                }
                recordBuilder.append(nextQuote);
                recordBuilder.append(leftQuote);
                for (Object o : recordInfo) {
                    if (o instanceof Object[]){
                        Object[] objs = (Object[]) o;
                        if (objs.length >0){
                            recordBuilder.append(objs[0]);
                        }else {
                            recordBuilder.append(missQuote);
                        }
                    }else {
                        recordBuilder.append(o);
                    }
                    recordBuilder.append(splitQuote);
                }
                recordBuilder.append(rightQuote);
            }
        }

        public void print() {
            if (resourceMgr.traceEnable) {
                log.info("exec traceInfo {} opInfo {}", traceId, recordBuilder.toString());
            }
        }
    }

}
