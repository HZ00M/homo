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

@Slf4j
public class RpcAgentClientImpl implements RpcAgentClient {

    private final RpcClient rpcClient;
    private final String srcServiceName;
    private final String targetServiceName;
    private boolean srcIsStateFul;
    private boolean targetIsStateful;
    private HomoTimerMgr homoTimerMgr = HomoTimerMgr.getInstance();


    public RpcAgentClientImpl(String srcServiceName, String targetServiceName, RpcClient client, boolean srcIsStateFul, boolean targetIsStateful) {
        this.rpcClient = client;
        this.srcServiceName = srcServiceName;
        this.targetServiceName = targetServiceName;
        this.srcIsStateFul = srcIsStateFul;
        this.targetIsStateful = targetIsStateful;
    }

    public String getSrcServiceName() {
        return srcServiceName;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Homo rpcCall(String funName, RpcContent param) {
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name(funName).tag("type","rpcCall").tag("funName",funName);
        try(Tracer.SpanInScope spanInScope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)){
            Homo rpcResult;
            if (param.getType().equals(RpcContentType.BYTES)) {
                ByteRpcContent byteRpcContent = (ByteRpcContent) param;
                byte[][] data = byteRpcContent.getData();
                if (srcIsStateFul && targetIsStateful) {
                    log.debug("asyncBytesStreamCall {} {}", funName, param);
                    rpcResult = asyncBytesStreamCall(funName, data);
                } else {
                    //无状态客户端访问有状态服务端 或 有状态客户端访问无状态服务端 都不需要建立长连接
                    log.debug("asyncBytesCall {} {}", funName, param);
                    rpcResult = asyncBytesCall(funName, data);
                }
            } else if (param.getType().equals(RpcContentType.JSON)) {
                JsonRpcContent jsonRpcContent = (JsonRpcContent) param;
                String data = jsonRpcContent.getData();
                rpcResult = asyncJsonCall(funName, data);
            } else {
                log.error("asyncCall contentType unknown, targetServiceName {} funName {} contentType {}", targetServiceName, funName, param.getType());
                rpcResult = Homo.error(new RuntimeException("rpcCall contentType unknown"));
            }
            return rpcResult.consumerValue(ret->span.finish());
        }catch (Exception e){
            return Homo.error(e);
        }

    }

    private  Homo asyncBytesCall(String funName, byte[][] data) {
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

    private  Homo asyncBytesStreamCall(String funName, byte[][] data) {
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

        if (data != null) {
            for (byte[] datum : data) {
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

    private  Homo asyncJsonCall(String funName, String data) {
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
