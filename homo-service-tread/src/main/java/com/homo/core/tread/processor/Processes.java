package com.homo.core.tread.processor;

import com.homo.core.tread.processor.exception.ProcessOpException;
import com.homo.core.tread.processor.op.Int_Resource_Sub_Allow_Zero;
import com.homo.core.tread.processor.op.Long_Resource_Sub_Allow_Zero;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Processes extends ResourceProcesses {

    public static Processes create(Object... resourceOwners) {
        Processes processes = new Processes(resourceOwners);
        return processes;
    }

    private Processes(Object... resourceOwners) {
        super(resourceOwners);
    }

    public Processes setTraceInfo(String traceInfo) {
        super.setTraceInfo(traceInfo);
        return this;
    }

    public Processes setAddOwner(Object owner) {
        setOpTypeOwner(ResourceOpType.ADD, owner);
        return this;
    }

    public Processes setSubOwner(Object owner) {
        setOpTypeOwner(ResourceOpType.SUB, owner);
        return this;
    }


    public Processes add(Object opValue, String resourceType, Object... resourceInfo) {
        doOp(opValue, ResourceOpType.ADD, resourceType, resourceInfo);
        return this;
    }

    public Processes sub(Object opValue, String resourceType, Object... resourceInfo) {
        doOp(opValue, ResourceOpType.SUB, resourceType, resourceInfo);
        return this;
    }

    public Processes subAllowZero(Integer opValue, String resourceType, Object... resourceInfo) {
        ResourceOp<?> resourceOp;
        try {
            resourceOp = Int_Resource_Sub_Allow_Zero.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("call subAllowZero error opValue {} resourceType {} resourceInfo {}", opValue, resourceType, resourceInfo, e);
            throw new ProcessOpException("call subAllowZero error");
        }
        resourceOp.init(ResourceOpType.SUB);
        initResourceOp(resourceOp,opValue,resourceType,resourceInfo);
        doOp(ResourceOpType.SUB, resourceOp);
        return this;
    }

    public Processes subAllowZero(Long opValue, String resourceType, Object... resourceInfo) {
        ResourceOp<?> resourceOp;
        try {
            resourceOp = Long_Resource_Sub_Allow_Zero.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("call subAllowZero error opValue {} resourceType {} resourceInfo {}", opValue, resourceType, resourceInfo, e);
            throw new ProcessOpException("call subAllowZero error");
        }
        resourceOp.init(ResourceOpType.SUB);
        initResourceOp(resourceOp,opValue,resourceType,resourceInfo);
        doOp(ResourceOpType.SUB, resourceOp);
        return this;
    }

}
