package com.homo.core.rpc.base;

import com.homo.core.utils.concurrent.event.AbstractTraceEvent;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.CallQueueProducer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.TraceLogUtil;
import io.homo.proto.client.ParameterMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

@Slf4j
public class CallEvent extends AbstractTraceEvent implements CallQueueProducer {
    private CallData callData;
    private final HomoSink sink;

    public CallEvent(CallData callData, HomoSink<?> homoSink) {
        this.id = String.format("CallEvent_%s_%s", callData.getSrcName(), callData.getMethodDispatchInfo().getMethod().getName());
        this.callData = callData;
        this.sink = homoSink;
        this.span = callData.getSpan();
    }

    @Override
    public void process() {
        try {
            TraceLogUtil.setTraceIdBySpan(span, id);
            if (Homo.class.isAssignableFrom(callData.getMethodDispatchInfo().getMethod().getReturnType())) {
                Homo<?> homo = (Homo<?>) callData.invoke(callData.getO(), callData.getParams());
                homo.consumerEmpty(() -> {
                            sink.error(HomoError.throwError(HomoError.callEmpty));
                        })
                        .consumerValue(ret -> {
//                            Object[] resParam = new Object[]{ret};
                            Object serializeParamForBack = callData.getMethodDispatchInfo().serializeForReturn(ret);
                            sink.success(serializeParamForBack);
                        })
                        .catchError(throwable -> {
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
