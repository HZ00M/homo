package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.rpc.base.trace.TraceAble;
import lombok.Builder;

@Builder
public class BytesArrayRpcContent implements RpcContent<byte[][]>, TraceAble<Span> {
    byte[][] data;
    Span span;

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

    @Override
    public void setTraceInfo(Span span) {
        this.span = span;
    }

    @Override
    public Span getTraceInfo() {
        return span;
    }
}
