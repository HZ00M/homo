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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;

@Log4j2
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

    @Override
    public Homo rpcCall(String funName, RpcContent param) {
        try (Tracer.SpanInScope ignored = ZipkinUtil.getTracing().tracer().withSpanInScope(ZipkinUtil.newCSSpan())) {
            if (param.getType().equals(RpcContentType.BYTES)) {
                ByteRpcContent byteRpcContent = (ByteRpcContent) param;
                byte[][] data = byteRpcContent.getData();
                if (srcIsStateFul && targetIsStateful) {
                    log.debug("asyncBytesStreamCall {} {}", funName, param);
                    return asyncBytesStreamCall(funName, data);
                } else {
                    //无状态客户端访问有状态服务端 或 有状态客户端访问无状态服务端 都不需要建立长连接
                    log.debug("asyncBytesCall {} {}", funName, param);
                    return asyncBytesCall(funName, data);
                }
            } else if (param.getType().equals(RpcContentType.JSON)) {
                JsonRpcContent jsonRpcContent = (JsonRpcContent) param;
                String data = jsonRpcContent.getData();
                return asyncJsonCall(funName, data);
            } else {
                log.error("asyncCall contentType unknown, targetServiceName {} funName {} contentType_{}", targetServiceName, funName, param.getType());
                return Homo.error(new RuntimeException("rpcCall contentType unknown"));
            }
        } catch (Exception e) {
            log.error("rpcCall {} {} error ", funName, param != null ? param.getType() : "null", e);
            return Homo.error(e);
        }
    }

    private  Homo asyncBytesCall(String funName, byte[][] data) {
        Span span = ZipkinUtil.currentSpan().name("asyncBytesCall");
        Req.Builder builder = Req.newBuilder().setSrcService(srcServiceName).setMsgId(funName);
        if (data != null) {
            for (byte[] datum : data) {
                builder.addMsgContent(ByteString.copyFrom(datum));
            }
        }
        Req req = builder.build();
        return rpcClient.asyncBytesCall(req);
    }

    private  Homo asyncBytesStreamCall(String funName, byte[][] data) {
        Span span = ZipkinUtil.currentSpan().name("asyncBytesStreamCall");
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
        Span span = ZipkinUtil.currentSpan().name("asyncJsonCall");
        JsonReq.Builder builder = JsonReq.newBuilder()
                .setSrcService(srcServiceName)
                .setMsgId(funName)
                .setMsgContent(data);

        JsonReq jsonReq = builder.build();
        return rpcClient.asyncJsonCall(jsonReq);
    }

}
