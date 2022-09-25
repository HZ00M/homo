package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.rpc.base.trace.TraceAble;
import lombok.Builder;

@Builder
public class TraceRpcContent<T> implements RpcContent<T>, TraceAble<Span> {
    T data;
    Span span;

    @Override
    public RpcContentType getType() {
        return RpcContentType.BYTES;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void setData(T data) {
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
