package com.homo.core.rpc.base.exception;

import com.homo.core.facade.excption.HomoException;

public class CallException extends HomoException {

    public CallException(String msg) {
        super(100, msg);
    }
}
