package com.homo.core.tread.processor.op;

import com.homo.core.tread.processor.ResourceOp;

/**
 * boolean类型 Add 操作
 */
public class Bool_Resource_Add extends ResourceOp<Boolean> {
    public Boolean computer(Boolean old, Boolean opValue){
        return opValue;
     }

}
