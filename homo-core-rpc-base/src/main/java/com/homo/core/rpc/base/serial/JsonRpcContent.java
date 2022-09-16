package com.homo.core.rpc.base.serial;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import lombok.Builder;

@Builder
public class JsonRpcContent implements RpcContent<String> {
    String data;

    @Override
    public RpcContentType getType() {
        return RpcContentType.JSON;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }
}
