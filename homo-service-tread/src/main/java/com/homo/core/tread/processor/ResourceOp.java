package com.homo.core.tread.processor;

import com.homo.core.facade.tread.processor.OpPoint;
import com.homo.core.tread.processor.exception.CheckOpException;
import com.homo.core.tread.processor.exception.GetOpException;
import com.homo.core.tread.processor.exception.ProcessOpException;
import com.homo.core.tread.processor.exception.SetOpException;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 资源操作的基类
 * 操作流程: getMethod -> afterGetCheck -> computer -> beforeSetCheck -> checkMethod -> setMethod
 */
@Slf4j
public abstract class ResourceOp<T> implements OpPoint {

    ResourceProcesses resourceProcesses;
    String resourceType;
    Object[] resourceInfo;
    T opValue;
    ResourceOpType opType;
    boolean executed = false;

    void setOpValue(Object opValue) {
        this.opValue = (T) opValue;
    }


    public void init(ResourceOpType opType, ResourceProcesses resourceProcesses, String resourceType, Object[] resourceInfo, T opValue) {
        this.opType = opType;
        this.resourceProcesses = resourceProcesses;
        this.resourceType = resourceType;
        this.resourceInfo = resourceInfo;
        this.opValue = opValue;
    }

    public void init(ResourceOpType opType) {
        init(opType, null, null, resourceInfo, opValue);
    }

    public void setResourceProcesses(ResourceProcesses resourceProcesses) {
        this.resourceProcesses = resourceProcesses;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    Homo<T> get() throws RuntimeException {
        return (Homo<T>) ResourceMgr.getResource(resourceProcesses.getOwner(opType, resourceType), resourceType, resourceInfo);
    }

    Homo<Boolean> set(T newValue) throws RuntimeException {
        return ResourceMgr.setResource(newValue, resourceProcesses.getOwner(opType, resourceType), resourceType, resourceInfo);
    }

    Homo<Boolean> check(T opValue, T newValue) throws RuntimeException {
        return ResourceMgr.checkResource(opValue, newValue, resourceProcesses.getOwner(opType, resourceType), resourceType, resourceInfo);
    }

    /**
     * 计算函数
     * @param old get获取的值
     * @param opValue 操作值
     * @return
     */
    protected abstract T computer(T old, T opValue);

    /**
     * get方法执行后检查函数
     * @param old get获取的值
     * @param opValue 操作值
     * @return
     */
    protected boolean afterGetCheck(T old, T opValue) {
        return true;
    }

    /**
     * set方法执行前检查函数
     * @param old get获取的值
     * @param opValue 操作值
     * @return
     */
    protected boolean beforeSetCheck(T old, T opValue, T newValue) {
        return true;
    }

    @Override
    public Homo<Boolean> exec() throws RuntimeException {
        if (executed) {
            throw new ProcessOpException("repeat exec op!");
        }
        executed = true;
        return get()
                .nextDo(old -> {
                    if (!afterGetCheck(old, opValue)) {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, old, opValue, ResourceProcesses.Record.missQuote, OpRet.afterGetCheckFail);
                        return Homo.result(false);
                    }
                    T newValue = computer(old, opValue);
                    if (!beforeSetCheck(old, opValue, newValue)) {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, old, opValue, ResourceProcesses.Record.missQuote, OpRet.beforeSetCheckFail);
                        return Homo.result(false);
                    }
                    return check(opValue, newValue)
                            .nextDo(checkRel -> {
                                if (checkRel) {
                                    return set(newValue).consumerValue(setRel -> {
                                        resourceProcesses.record.record(resourceType,resourceInfo, opType, old, opValue, newValue, OpRet.ok);
                                    });
                                } else {
                                    resourceProcesses.record.record(resourceType, resourceInfo, opType, old, opValue, newValue, OpRet.checkFail);
                                    return Homo.result(false);
                                }
                            });
                })
                .onErrorContinue(throwable -> {
                    log.error("exec error opType {} opValue {} e", opType, opValue, throwable);
                    if (throwable instanceof SetOpException) {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, ResourceProcesses.Record.missQuote, opValue, ResourceProcesses.Record.missQuote, OpRet.setError);
                    } else if (throwable instanceof GetOpException) {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, ResourceProcesses.Record.missQuote, opValue, ResourceProcesses.Record.missQuote, OpRet.getError);
                    } else if (throwable instanceof CheckOpException) {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, ResourceProcesses.Record.missQuote, opValue, ResourceProcesses.Record.missQuote, OpRet.checkError);
                    } else {
                        resourceProcesses.record.record(resourceType, resourceInfo, opType, ResourceProcesses.Record.missQuote, opValue, ResourceProcesses.Record.missQuote, OpRet.sysError);
                    }
                    return Homo.result(false);
                });
    }

    @Override
    public String toString() {
        return "ResourceOp{" +
                "resourceType='" + resourceType + '\'' +
                ", resourceInfo=" + Arrays.toString(resourceInfo) +
                ", opType=" + opType +
                ", opValue=" + opValue +
                '}';
    }
}
