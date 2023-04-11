package com.homo.core.tread.processor.op;


import com.homo.core.tread.processor.ResourceOp;

/**
 * string类型 Add 操作
 */
public class String_Resource_Add extends ResourceOp<String> {
    public String computer(String old, String opValue){
        return opValue;
    }
}
