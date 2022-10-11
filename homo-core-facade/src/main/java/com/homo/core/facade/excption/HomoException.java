package com.homo.core.facade.excption;

public class HomoException extends Exception{
    private int code;
    private String msg;
    public HomoException(int code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
