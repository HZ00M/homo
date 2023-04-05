package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * int类型 Add 操作
 */
public class Int_Resource_Add extends ResourceOp<Integer> {
    public Integer computer(Integer old, Integer opValue){
        if (old == null)
            return opValue;
        return old + opValue;
    }

    @Override
    protected boolean afterGetCheck(Integer old, Integer opValue) {
        if (opValue < 0){
            return false;
        }
        return true;
    }
}
