package com.homo.core.rpc.grpc;

import brave.Span;
import brave.Tracer;
import com.google.protobuf.ByteString;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.rpc.JsonReq;
import io.homo.proto.rpc.Req;
import io.homo.proto.rpc.StreamReq;
import io.homo.proto.rpc.TraceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import reactor.util.function.Tuple2;

import java.io.IOException;

@Slf4j
public class GrpcRpcAgentClient implements RpcAgentClient {

    private final RpcClient rpcClient;
    private final String srcServiceName;
    private final String targetServiceName;
    private boolean srcIsStateFul;
    private boolean targetIsStateful;
    private HomoTimerMgr homoTimerMgr = HomoTimerMgr.getInstance();


    public GrpcRpcAgentClient(String srcServiceName, String targetServiceName, RpcClient client, boolean srcIsStateFul, boolean targetIsStateful) {
        this.rpcClient = client;
        this.srcServiceName = srcServiceName;
        this.targetServiceName = targetServiceName;
        this.srcIsStateFul = srcIsStateFul;
        this.targetIsStateful = targetIsStateful;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Homo rpcCall(String funName, RpcContent content) {
        Span span = ZipkinUtil.getTracing().tracer()
                .nextSpan()
                .name(funName)
                .tag("type", "rpcCall")
                .tag("srcServiceName", srcServiceName)
                .tag("targetServiceName", targetServiceName)
                .tag("funName", funName);
        try (Tracer.SpanInScope spanInScope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
            Homo rpcResult;
            if (content.getType().equals(RpcContentType.BYTES)) {
                ByteRpcContent byteRpcContent = (ByteRpcContent) content;
                byte[][] paramData = byteRpcContent.getParam();
                if (srcIsStateFul && targetIsStateful) {
                    log.debug("asyncBytesStreamCall {} {}", funName, content);
                    rpcResult = asyncBytesStreamCall(funName, paramData)
                            .consumerValue(ret->{
                                Tuple2<String,byte[][]> result = ret;
                                byteRpcContent.setReturn(result.getT2()[0]);
                            });
                } else {
                    //无状态客户端访问有状态服务端 或 有状态客户端访问无状态服务端 都不需要建立长连接
                    log.debug("asyncBytesCall {} {}", funName, content);
                    rpcResult = asyncBytesCall(funName, paramData)
                            .consumerValue(ret->{
                                Tuple2<String,byte[][]> result = ret;
                                byteRpcContent.setReturn(result.getT2()[0]);
                            });
                }
            } else if (content.getType().equals(RpcContentType.JSON)) {
                JsonRpcContent jsonRpcContent = (JsonRpcContent) content;
                String data = jsonRpcContent.getParam();
                rpcResult = asyncJsonCall(funName, data)
                        .consumerValue(ret->{
                            Tuple2<String,String> result = (Tuple2<String, String>) ret;
                            jsonRpcContent.setReturn(result.getT2());
                        })
                ;
            } else {
                log.error("asyncCall contentType unknown, targetServiceName {} funName {} contentType {}", targetServiceName, funName, content.getType());
                rpcResult = Homo.error(new RuntimeException("rpcCall contentType unknown"));
            }
            return rpcResult.consumerValue(ret -> {
                span.finish();
            });
        } catch (Exception e) {
            return Homo.error(e);
        }

    }

    private Homo<Tuple2<String, byte[][]>> asyncBytesCall(String funName, byte[][] data) {
        Span span = ZipkinUtil.currentSpan();
        Req.Builder builder = Req.newBuilder().setSrcService(srcServiceName).setMsgId(funName);
        if (data != null) {
            for (byte[] datum : data) {
                builder.addMsgContent(ByteString.copyFrom(datum));
            }
        }
        Req req1 = builder.build();
        TraceInfo traceInfo = TraceInfo.newBuilder()
                .setTraceId(span.context().traceId())
                .setSpanId(span.context().spanId())
                .setSample(span.context().sampled())
                .build();
        String reqId = new StringBuilder()
                .append(req1.hashCode())
                .append(":")
                .append(System.currentTimeMillis())
                .append(":")
                .append(RandomUtils.nextInt()).toString();
        Req req = builder.setReqId(reqId).setTraceInfo(traceInfo).build();
        return rpcClient.asyncBytesCall(req);
    }

    private Homo<Tuple2<String, byte[][]>> asyncBytesStreamCall(String funName, byte[][] paramData) throws IOException {
        Span span = ZipkinUtil.currentSpan();
        StreamReq.Builder builder = StreamReq.newBuilder()
                .setSrcService(srcServiceName)
                .setMsgId(funName)
                .setTraceInfo(
                        TraceInfo.newBuilder()
                                .setTraceId(span.context().traceId())
                                .setSpanId(span.context().spanId())
                                .setSample(span.context().sampled())
                                .build()
                );

        if (paramData != null) {
            for (byte[] datum : paramData) {
                builder.addMsgContent(ByteString.copyFrom(datum));
            }
        }
        StreamReq streamReq = builder.build();
        String reqId = new StringBuilder()
                .append(streamReq.hashCode())
                .append(":")
                .append(System.currentTimeMillis())
                .append(":")
                .append(RandomUtils.nextInt()).toString();
        StreamReq streamReqWithReqId = builder.setReqId(reqId).build();
        return rpcClient.asyncBytesStreamCall(reqId, streamReqWithReqId);
    }

    private Homo asyncJsonCall(String funName, String data) {
        Span span = ZipkinUtil.currentSpan();
        JsonReq.Builder builder = JsonReq.newBuilder()
                .setSrcService(srcServiceName)
                .setMsgId(funName)
                .setMsgContent(data);
        TraceInfo traceInfo = TraceInfo.newBuilder()
                .setTraceId(span.context().traceId())
                .setSpanId(span.context().spanId())
                .setSample(span.context().sampled())
                .build();
        JsonReq jsonReq1 = builder.build();
        String reqId = new StringBuilder()
                .append(jsonReq1.hashCode())
                .append(":")
                .append(System.currentTimeMillis())
                .append(":")
                .append(RandomUtils.nextInt()).toString();
        JsonReq jsonReq = builder.setReqId(reqId).setTraceInfo(traceInfo).build();
        return rpcClient.asyncJsonCall(jsonReq);
    }

}
