package com.homo.core.rpc.base.serial;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import lombok.Builder;

@Builder
public class BytesArrayRpcContent implements RpcContent<byte[][]> {
    byte[][] data;
    @Override
    public RpcContentType getType() {
        return RpcContentType.BYTES;
    }
    @Override
    public byte[][] getData() {
        return data;
    }

    @Override
    public void setData(byte[][] data) {
        this.data = data;
    }

}
