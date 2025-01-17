package com.homo.core.rpc.base;

import brave.Span;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.fun.ConsumerWithException;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CallData implements RpcInterceptor {
    private final Object o;
    private final MethodDispatchInfo methodDispatchInfo;
    private final Object[] params;
    private final Integer queueId;
    private final String srcName;
    private IdCallQueue idCallQueue;
    private Span span;
    private boolean needInsertQueue;
    private String choiceThreadStrategy;

    public CallData(Object o, MethodDispatchInfo methodDispatchInfo, Object[] params, Integer queueId, String srcName, Span span) {
        this(o, methodDispatchInfo, params, queueId, srcName, null, span, false, null);
    }


    public CallData(Object o, MethodDispatchInfo methodDispatchInfo, Object[] params, Integer queueId, String srcName, IdCallQueue idCallQueue, Span span, boolean needInsertQueue, String choiceThreadStrategyKey) {
        this.o = o;
        this.methodDispatchInfo = methodDispatchInfo;
        this.params = params;
        this.queueId = queueId;
        this.srcName = srcName;
        this.idCallQueue = idCallQueue;
        this.span = span;
        this.needInsertQueue = needInsertQueue;
        this.choiceThreadStrategy = choiceThreadStrategyKey;
    }


    public Object invoke(Object o, Object[] param) throws Throwable {
//        if (idCallQueue != null) {
//            return Homo.warp(homoSink -> {
//                idCallQueue.addIdTask(new Callable() {
//                    @Override
//                    public Object call() throws Exception {
//                        return methodDispatchInfo.getMethod().invoke(o, param);
//                    }
//                }, homoSink);
//            });
//        }
        return methodDispatchInfo.getMethod().invoke(o, param);
    }

    @Override
    public Homo onCall(Object handle, String funName, Object[] params, CallData callData) {
        return Homo.warp(new ConsumerWithException<HomoSink<Homo>>() {
            @Override
            public void accept(HomoSink<Homo> homoSink) throws Exception {
                CallEvent callEvent = new CallEvent(CallData.this, homoSink);
                if (queueId != null) {
                    CallQueueMgr.getInstance().addEvent(queueId, callEvent);
                } else if (needInsertQueue) {
                    if (!StringUtils.isEmpty(choiceThreadStrategy)){
                        CallQueueMgr.getInstance().addEvent(choiceThreadStrategy, callEvent);
                    }else {
                        CallQueueMgr.getInstance().addEvent(CallQueueMgr.DEFAULT_CHOICE_THREAD_STRATEGY, callEvent);
                    }
                } else {
                    callEvent.process();
                }
            }
        });
    }
}
