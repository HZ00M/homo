package com.homo.core.rpc.base;

import brave.Span;
import com.homo.concurrent.event.AbstractBaseEvent;
import com.homo.concurrent.queue.CallQueueProducer;
import com.homo.core.facade.excption.HomoError;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        Span span = ZipkinUtil.currentSpan();
        if (span == null) {
            log.warn("o {} method {} form {} currentSpan is null!", callData.getO().getClass(), callData.getMethodDispatchInfo().getMethod(), callData);
        } else {
            span.tag("callMethod", callData.getMethodDispatchInfo().getMethod().getName());
        }
        String methodName = callData.getMethodDispatchInfo().getMethod().getName();
        Class<?> handlerClazz = callData.getO() == null ? null : callData.getO().getClass();
        try {
            if (Homo.class.isAssignableFrom(callData.getMethodDispatchInfo().getMethod().getReturnType())){
                Homo<?> homo = (Homo<?>) callData.invoke(callData.getO(),callData.getParams());
                homo.consumerEmpty(()->{
                    log.debug("CallEvent consumerEmpty method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start,  handlerClazz, methodName);

                    sink.error(HomoError.throwError(HomoError.callEmpty));
                })
                .consumerValue(ret->{
                    log.debug("CallEvent consumerValue method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start,  handlerClazz, methodName);
                    sink.success(ret);
                })
                .catchError(throwable -> {
                    log.debug("CallEvent catchError method ret, take {} milliseconds, o_{}, methodName_{}", System.currentTimeMillis() - start,  handlerClazz, methodName);
                    sink.error(throwable);
                })
                .start();
            }else {
                throw HomoError.throwError(HomoError.callError);
            }
        }catch (Throwable e){
            log.error("CallEvent precess finished with error, o_{}, methodName_{} milliseconds_{}",  handlerClazz, methodName, System.currentTimeMillis() - start, e);
            sink.error(e);
        }
    }

    @Override
    public Integer getQueueId() {
        return callData.getQueueId();
    }
}
