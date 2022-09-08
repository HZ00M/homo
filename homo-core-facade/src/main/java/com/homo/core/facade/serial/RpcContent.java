package com.homo.core.facade.serial;

public interface RpcContent {
    RpcContentType getType();

    enum RpcContentType{
        /**
         *  字节数据
         */
        BYTES,
        /**
         * json数据
         */
        JSON,
    }
}
