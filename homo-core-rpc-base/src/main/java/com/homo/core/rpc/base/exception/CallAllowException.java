package com.homo.core.rpc.base.exception;

import com.homo.core.facade.excption.HomoException;

public class CallAllowException extends HomoException {

    public CallAllowException(String msg) {
        super(101, msg);
    }
}
