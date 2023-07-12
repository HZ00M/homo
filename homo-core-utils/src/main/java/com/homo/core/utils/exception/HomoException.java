package com.homo.core.utils.exception;

import lombok.Data;

@Data
public class HomoException extends RuntimeException{
    private int code;
    private String msg;
    public HomoException(int code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
