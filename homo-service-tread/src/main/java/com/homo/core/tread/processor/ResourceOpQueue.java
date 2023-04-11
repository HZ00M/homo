package com.homo.core.tread.processor;

/**
 * 资源操作队列
 */
public class ResourceOpQueue extends OpQueue{
    ResourceOpType opQueueType;

    public ResourceOpQueue(ResourceOpType opQueueType) {
        this.opQueueType = opQueueType;
    }

    public int getOrder(){
        // 这里通过opType 得到操作顺序
        return opQueueType.ordinal();
    }

}
