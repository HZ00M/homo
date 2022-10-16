package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.utils.trace.TraceAble;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceRpcContent<T> implements RpcContent<T>, TraceAble<Span> {
    T data;
    RpcContentType type;
    Span span;


    @Override
    public RpcContentType getType() {
        return type;
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

    public void setType(RpcContentType type) {
        this.type = type;
    }
}
