package com.homo.core.rpc.base.trace;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.*;
import io.homo.proto.rpc.StreamReq;
import io.homo.proto.rpc.TraceInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpanInterceptor implements ServerInterceptor {
    static Span span;

    @Override
    public <IN, OUT> SpanCallListener<IN> interceptCall(ServerCall<IN, OUT> call, Metadata headers, ServerCallHandler<IN, OUT> next) {
        return new SpanCallListener<IN>(next.startCall(new SpanServerCall<>(call), headers));
    }

    static class SpanCallListener<IN> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<IN> {
        protected SpanCallListener(ServerCall.Listener<IN> delegate) {
            super(delegate);
        }

        @Override
        public void onMessage(IN message) {
            log.info("SpanInterceptor message IN");
            String msgId = "";
            if (message instanceof StreamReq) {
                StreamReq req = (StreamReq) message;
                span = getSpan(req.getMsgId(), req.getTraceInfo());
            } else {
                span = ZipkinUtil.getTracing().tracer().currentSpan();
            }
            TraceLogUtil.setTraceIdBySpan(span, msgId);
            try (Tracer.SpanInScope scope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
                delegate().onMessage(message);
            } finally {
                TraceLogUtil.clearTrace();//是否需要
            }
        }
    }

    static class SpanServerCall<IN, OUT> extends ForwardingServerCall.SimpleForwardingServerCall<IN, OUT> {

        protected SpanServerCall(ServerCall<IN, OUT> delegate) {
            super(delegate);
        }

        @Override
        public void sendMessage(OUT message) {
            log.info("SpanInterceptor message OUT");
            //发送到客户端
            span.tag(ZipkinUtil.FINISH_TAG, "sendMessage")
                    .tag(ZipkinUtil.SERVER_SEND_TAG, "SpanInterceptor send")
                    .annotate(ZipkinUtil.SERVER_SEND_TAG)
                    .finish();
            delegate().sendMessage(message);
        }
    }

    public static Span getSpan(String msgId, TraceInfo traceInfo) {
        TraceContext traceContext =
                TraceContext.newBuilder()
                        .spanId(traceInfo.getSpanId())
                        .traceId(traceInfo.getTraceId())
                        .sampled(traceInfo.getSample())
                        .build();
        Span span = ZipkinUtil.getTracing()
                .tracer()
                .newChild(traceContext)
                .annotate(ZipkinUtil.SERVER_RECEIVE_TAG)
                .name(msgId)
                .start();
        log.info(
                "SpanInterceptor getSpan traceId {}, sampled {} parent_spanId {} spanId {} msgId {}",
                traceContext.traceId(),
                traceContext.sampled(),
                traceContext.spanId(),
                span.context().spanId(),
                msgId);
        return span;
    }
}

