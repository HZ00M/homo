package com.homo.core.rpc.grpc;

import brave.Span;
import com.google.protobuf.ByteString;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.rpc.base.trace.SpanInterceptor;
import com.homo.core.rpc.grpc.proccessor.CallErrorProcessor;
import com.homo.core.rpc.grpc.proccessor.JsonCallErrorProcessor;
import com.homo.core.rpc.grpc.proccessor.StreamCallErrorProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.stub.StreamObserver;
import io.homo.proto.rpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcRpcCallService extends RpcCallServiceGrpc.RpcCallServiceImplBase {
    private final RpcServer rpcServer;
    public GrpcRpcCallService(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }
    @Override
    public void rpcCall(Req req, StreamObserver<Res> responseObserver) {
        Span span = SpanInterceptor.getSpan(req.getMsgId(), req.getTraceInfo());
        String msgId = req.getMsgId();
        byte[][] params = new byte[req.getMsgContentCount()][];
        for (int i = 0; i < req.getMsgContentCount(); i++) {
            params[i] = req.getMsgContent(i).toByteArray();
        }
        try {
            ByteRpcContent rpcContent = new ByteRpcContent();
            rpcContent.setMsgId(msgId);
            rpcContent.setParam(params);
            rpcContent.setSpan(span);
            rpcServer.onCall(req.getSrcService(), req.getMsgId(), rpcContent)
                    .consumerValue(ret -> {
                        Res.Builder builder = Res.newBuilder().setMsgId(req.getMsgId());
                        builder.addMsgContent(ByteString.copyFrom(ret));
//                        if (ret != null) {
//                            for (byte[] resDatum : ret) {
//                                if (resDatum == null) {
//                                    //不支持包含间断的空返回
//                                    break;
//                                }
//                                builder.addMsgContent(ByteString.copyFrom(resDatum));
//                            }
//                        }
                        Res res = builder.build();
                        span.annotate(ZipkinUtil.SERVER_SEND_TAG).finish();
                        responseObserver.onNext(res);
                        responseObserver.onCompleted();
                    })
                    .catchError(throwable -> {
                        try {
                            span.error(throwable);
                            Res res = CallErrorProcessor.processError(req, throwable);
                            responseObserver.onNext(res);
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            responseObserver.onError(e);
                        }
                    }).start();
        } catch (Exception e) {
            span.error(e);
            log.error("rpcCall SrcService {} MsgId {} error e", req.getSrcService(), req.getMsgId(), e);
        }

    }

    @Override
    public StreamObserver<StreamReq> streamCall(StreamObserver<StreamRes> responseObserver) {
        return new StreamObserver<StreamReq>() {
            @Override
            public void onNext(StreamReq req) {
                try {
                    Span span = ZipkinUtil.currentSpan();
                    String msgId = req.getMsgId();
                    byte[][] params = new byte[req.getMsgContentCount()][];
                    for (int i = 0; i < req.getMsgContentCount(); i++) {
                        params[i] = req.getMsgContent(i).toByteArray();
                    }
                    ByteRpcContent rpcContent = new ByteRpcContent();
                    rpcContent.setMsgId(msgId);
                    rpcContent.setParam(params);
                    rpcContent.setSpan(span);
                    rpcServer.onCall(req.getSrcService(), req.getMsgId(), rpcContent)
                            .consumerValue(resData -> {
                                StreamRes.Builder builder = StreamRes.newBuilder().setMsgId(req.getMsgId());
                                if (resData != null) {
                                    builder.addMsgContent(ByteString.copyFrom(resData));
//                                    for (byte[] resDatum : resData) {
//                                        if (resDatum == null) {
//                                            break;
//                                        }
//                                        builder.addMsgContent(ByteString.copyFrom(resDatum));
//                                    }
                                }
                                builder.setReqId(req.getReqId());
                                StreamRes res = builder.build();
                                // 这里就是要对单个的responseObserver加锁
                                synchronized (responseObserver) {
                                    span.annotate(ZipkinUtil.SERVER_SEND_TAG).finish();
                                    responseObserver.onNext(res);
                                }
                            })
                            .catchError(throwable -> {
                                try {
                                    span.error(throwable);
                                    StreamRes res = StreamCallErrorProcessor.processError(req, throwable);
                                    synchronized (responseObserver) {
                                        responseObserver.onNext(res);
                                    }
                                } catch (Exception e) {
                                    synchronized (responseObserver) {
                                        responseObserver.onError(e);
                                    }
                                }
                            }).start();
                } catch (Exception e) {
                    log.error("streamCall SrcService {} MsgId {} error e", req.getSrcService(), req.getMsgId(), e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("RpcCallServiceImpl streamCall onError {}", responseObserver, throwable);
                ZipkinUtil.getTracing().tracer().currentSpan().tag(ZipkinUtil.FINISH_TAG, "streamCall_Error").error(throwable);
            }

            @Override
            public void onCompleted() {
                ZipkinUtil.getTracing().tracer().currentSpan().tag(ZipkinUtil.FINISH_TAG, "streamCall_onCompleted");
                log.error("RpcCallServiceImpl streamCall onCompleted responseObserver {}", responseObserver);
            }
        };
    }


    @Override
    public void jsonCall(JsonReq req, StreamObserver<JsonRes> responseObserver) {
        String msgId = req.getMsgId();
        String msgContent = req.getMsgContent();
        Span span = SpanInterceptor.getSpan(req.getMsgId(), req.getTraceInfo());
        try {
            JsonRpcContent rpcContent = new JsonRpcContent();
            rpcContent.setMsgId(msgId);
            rpcContent.setParam(msgContent);
            rpcContent.setSpan(span);
            rpcServer.onCall(req.getSrcService(), req.getMsgId(), rpcContent)
                    .consumerValue(resData -> {
                        JsonRes.Builder builder = JsonRes.newBuilder().setMsgId(req.getMsgId()).setMsgContent(resData);
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();
                    })
                    .catchError(throwable -> {
                        try {
                            JsonRes errorRes = JsonCallErrorProcessor.processError(req, throwable);
                            span.annotate(ZipkinUtil.SERVER_SEND_TAG).finish();
                            responseObserver.onNext(errorRes);
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            span.error(throwable);
                            responseObserver.onError(e);
                        }
                    }).start();
        } catch (Exception e) {
            span.error(e);
            log.error("jsonCall SrcService {} MsgId {} error e", req.getSrcService(), req.getMsgId(), e);
        }

    }

}
