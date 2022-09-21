package com.core.rpc.client;

import brave.Span;
import brave.Tracer;
import com.google.protobuf.ByteString;
import com.homo.concurrent.schedule.HomoTimerMgr;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.rpc.JsonReq;
import io.homo.proto.rpc.Req;
import io.homo.proto.rpc.StreamReq;
import io.homo.proto.rpc.TraceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class RpcAgentClientImpl implements RpcAgentClient {

    private final RpcClient rpcClient;
    private final String srcServiceName;
    private final String targetServiceName;
    HomoTimerMgr homoTimerMgr;
    Boolean srcIsStateFul;
    Boolean targetIsStateful;

    public RpcAgentClientImpl(String hostname, int port, String srcServiceName, String targetServiceName, RpcClient client) {
        this.rpcClient = client;
        homoTimerMgr = HomoTimerMgr.getInstance();
        this.srcServiceName = srcServiceName;
        this.targetServiceName = targetServiceName;
    }

    @Override
    public String getSrcServiceName() {
        return srcServiceName;
    }

    @Override
    public String getTargetServiceName() {
        return targetServiceName;
    }

    @Override
    public <RETURN, PARAM> Homo<RETURN> rpcCall(String funName, RpcContent<PARAM> param) {
        try (Tracer.SpanInScope ignored = ZipkinUtil.getTracing().tracer().withSpanInScope(ZipkinUtil.currentSpan())) {
            if (param.getType().equals(RpcContentType.BYTES)) {
                byte[][] data = (byte[][]) param.getData();
                if (srcIsStateFul && targetIsStateful) {
                    log.debug("asyncBytesStreamCall {} {}", funName, param);
                    return asyncBytesStreamCall(funName, data);
                } else {
                    log.debug("asyncBytesCall {} {}", funName, param);
                    return asyncBytesCall(funName, data);
                }
            } else if (param.getType().equals(RpcContentType.JSON)) {
                String data = (String) param.getData();
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

    private <RETURN> Homo<RETURN> asyncBytesCall(String funName, byte[][] data) {
        Req.Builder builder = Req.newBuilder().setSrcService(srcServiceName).setMsgId(funName);
        if (data != null) {
            for (byte[] datum : data) {
                builder.addMsgContent(ByteString.copyFrom(datum));
            }
        }
        Req req = builder.build();
        return rpcClient.asyncBytesCall(req);
    }

    private <RETURN> Homo<RETURN> asyncBytesStreamCall(String funName, byte[][] data) {
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
        return rpcClient.asyncBytesStreamCall(reqId,streamReqWithReqId);
    }

    private <RETURN> Homo<RETURN> asyncJsonCall(String funName, String data) {
        JsonReq.Builder builder = JsonReq.newBuilder()
                .setSrcService(srcServiceName)
                .setMsgId(funName);
        if (!StringUtils.isEmpty(data)){
            builder.setMsgContent(data);
        }
        JsonReq jsonReq = builder.build();
        return rpcClient.asyncJsonCall(jsonReq);
    }

}
