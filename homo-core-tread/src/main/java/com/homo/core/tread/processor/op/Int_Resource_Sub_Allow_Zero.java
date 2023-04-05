package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * int类型 Sub 操作
 */
public class Int_Resource_Sub_Allow_Zero extends ResourceOp<Integer> {
    public Integer computer(Integer old, Integer opValue){
        if (old == null)
            return null;
        return old - opValue;
    }

    @Override
    protected boolean afterGetCheck(Integer old, Integer opValue) {
        if (old == null){
            return false;
        }
        return opValue != null && opValue >= 0;
    }

    @Override
    protected boolean beforeSetCheck(Integer old, Integer opValue, Integer newValue) {
        return newValue >= 0;
    }
}
