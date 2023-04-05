package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * long类型 Sub 操作
 */
public class Long_Resource_Sub_Allow_Zero extends ResourceOp<Long> {
    public Long computer(Long old, Long opValue){
        if (old == null)
            return null;
        return old - opValue;
    }

    @Override
    protected boolean afterGetCheck(Long old, Long opValue) {
        if (old == null){
            return false;
        }
        return opValue != null && opValue >= 0;
    }

    @Override
    protected boolean beforeSetCheck(Long old, Long opValue, Long newValue) {
        return newValue >= 0;
    }
}
