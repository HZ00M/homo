package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * String类型 Sub 操作
 */
public class String_Resource_Sub extends ResourceOp<String> {
    public String computer(String old, String opValue){
        return opValue;
    }
}
