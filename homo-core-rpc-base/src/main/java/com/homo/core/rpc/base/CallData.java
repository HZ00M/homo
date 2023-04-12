package com.homo.core.rpc.base;

import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.facade.rpc.RpcInterceptor;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.utils.fun.ConsumerEx;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.Data;

@Data
public class CallData implements RpcInterceptor {
    private final Object o;
    private final MethodDispatchInfo methodDispatchInfo;
    private final Object[] params;
    private final Integer queueId;
    private final String srcName;

    public CallData(Object o, MethodDispatchInfo methodDispatchInfo, Object[] params, Integer queueId, String srcName) {
        this.o = o;
        this.methodDispatchInfo = methodDispatchInfo;
        this.params = params;
        this.queueId = queueId;
        this.srcName = srcName;
    }

    public Object invoke(Object o, Object[] param) throws Throwable {
        return methodDispatchInfo.getMethod().invoke(o, param);
    }

    @Override
    public Homo onCall(Object handle, String funName, Object[] params) {
        return Homo.warp(new ConsumerEx<HomoSink<Homo>>() {
            @Override
            public void accept(HomoSink<Homo> homoSink) throws Exception {
                CallEvent callEvent = new CallEvent(CallData.this, homoSink);
                if (queueId != null) {
                    CallQueueMgr.getInstance().addEvent(queueId, callEvent);
                } else {
                    callEvent.process();
                }
            }
        });
    }
}
