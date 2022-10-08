package com.homo.core.rpc.base.exception;

import com.homo.core.facade.excption.HomoException;

public class CallEmptyException extends HomoException {

    public CallEmptyException(String msg) {
        super(102, msg);
    }
}
