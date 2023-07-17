package com.homo.core.rpc.base;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.utils.concurrent.event.AbstractBaseEvent;
import com.homo.core.utils.concurrent.queue.CallQueueProducer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CallEvent extends AbstractBaseEvent implements CallQueueProducer {
    private CallData callData;
    private final HomoSink sink;

    public CallEvent(CallData callData, HomoSink<?> homoSink) {
        this.callData = callData;
        this.sink = homoSink;
    }

    @Override
    public void process() {
        final long start = System.currentTimeMillis();
        Span span = ZipkinUtil.getTracing().tracer().newChild(callData.getSpan().context()).start();
        if (span == null) {
            log.warn("o {} method {} form {} currentSpan is null!", callData.getO().getClass(), callData.getMethodDispatchInfo().getMethod(), callData);
        } else {
            span.name("process").annotate(ZipkinUtil.SERVER_RECEIVE_TAG).tag("callMethod", callData.getMethodDispatchInfo().getMethod().getName());
        }
        String methodName = callData.getMethodDispatchInfo().getMethod().getName();
        Class<?> handlerClazz = callData.getO() == null ? null : callData.getO().getClass();
        try {
            if (Homo.class.isAssignableFrom(callData.getMethodDispatchInfo().getMethod().getReturnType())) {
                Homo<?> homo = (Homo<?>) callData.invoke(callData.getO(), callData.getParams());
                homo.consumerEmpty(() -> {
                            log.debug("CallEvent consumerEmpty method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start, handlerClazz, methodName);

                            sink.error(HomoError.throwError(HomoError.callEmpty));
                        })
                        .consumerValue(ret -> {
                            log.debug("CallEvent consumerValue method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start, handlerClazz, methodName);
                            Object[] resParam = new Object[]{ret};
                            Object serializeParamForBack = callData.getMethodDispatchInfo().serializeForReturn(resParam);
                            if (span == null) {
                                log.warn("o {} method {} form {} currentSpan is null!", callData.getO().getClass(), callData.getMethodDispatchInfo().getMethod(), callData);
                            } else {
                                span.finish();
                            }
                            sink.success(serializeParamForBack);
                        })
                        .catchError(throwable -> {
                            log.debug("CallEvent catchError method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start, handlerClazz, methodName);
                            span.error(throwable);
                            sink.error(throwable);
                        })
                        .start();
            } else {
                try {
                    Object invoke = callData.invoke(callData.getO(), callData.getParams());
                    if (sink != null) sink.success(invoke);
                } catch (Exception e) {
                    if (sink != null) sink.error(e);
                }
            }
        } catch (Throwable e) {
            log.error("CallEvent precess finished with error, o_{}, methodName_{} milliseconds_{}", handlerClazz, methodName, System.currentTimeMillis() - start, e);
            sink.error(e);
        }
    }

    @Override
    public Integer getQueueId() {
        return callData.getQueueId();
    }


}
