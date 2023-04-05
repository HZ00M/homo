package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * long类型 Add 操作
 */
public class Long_Resource_Add extends ResourceOp<Long> {
    public Long computer(Long old, Long opValue){
        if (old == null)
            return opValue;
        return old + opValue;
    }

    @Override
    protected boolean afterGetCheck(Long old, Long opValue) {
        if (opValue < 0){
            return false;
        }
        return true;
    }
}
