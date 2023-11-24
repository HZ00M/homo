package com.homo.core.rpc.base;

import brave.Span;
import com.homo.core.utils.concurrent.event.AbstractBaseEvent;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.CallQueueProducer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.client.ParameterMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

@Slf4j
public class CallEvent extends AbstractBaseEvent implements CallQueueProducer {
    private CallData callData;
    private final HomoSink sink;

    public CallEvent(CallData callData, HomoSink<?> homoSink) {
        this.id = String.format("CallEvent_%s_%s", callData.getSrcName(), callData.getMethodDispatchInfo().getMethod().getName());
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
        TraceLogUtil.setTraceIdBySpan(span, "CallEvent process");
        String methodName = callData.getMethodDispatchInfo().getMethod().getName();
        Class<?> handlerClazz = callData.getO() == null ? null : callData.getO().getClass();
        try {
            if (Homo.class.isAssignableFrom(callData.getMethodDispatchInfo().getMethod().getReturnType())) {
                Homo<?> homo = (Homo<?>) callData.invoke(callData.getO(), callData.getParams());
                homo.consumerEmpty(() -> {
                            log.debug("CallEvent consumerEmpty method ret, take {} milliseconds, o {}, methodName {}", System.currentTimeMillis() - start, handlerClazz, methodName);

                            sink.error(HomoError.throwError(HomoError.callEmpty));
                        })
                        .consumerValue(ret -> {
                            log.debug("CallEvent consumerValue method ret, take {} milliseconds, o {}, methodName {}", System.currentTimeMillis() - start, handlerClazz, methodName);
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
                            log.debug("CallEvent catchError method ret, take {} milliseconds, o {}, methodName {}", System.currentTimeMillis() - start, handlerClazz, methodName);
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
            log.error("CallEvent precess finished with error, o {}, methodName {} milliseconds {}", handlerClazz, methodName, System.currentTimeMillis() - start, e);
            sink.error(e);
        }
    }

    @Override
    public Integer getQueueId() {
        if (callData.getQueueId() != null) {//如果有指定队列，就用指定队列
            return callData.getQueueId();
        }
        Object[] params = callData.getParams();
        if (params != null && params.length >= 2 && params[1] != null && params[1] instanceof ParameterMsg) {
            ParameterMsg parameterMsg = (ParameterMsg) params[1];
            return CallQueueMgr.getInstance().choiceQueueIdBySeed(parameterMsg.getUserId().hashCode());
        }
        if (callData.getO() instanceof CallQueueProducer) {
            return ((CallQueueProducer) callData.getO()).getQueueId();
        }
        int randomQueueId = CallQueueMgr.getInstance().choiceQueueIdBySeed(RandomUtils.nextInt());
        log.info("CallEvent getQueueId randomQueueId {} callData.getO {}  params {}", randomQueueId, callData.getO(), callData.getParams());
        return randomQueueId;
    }


}
