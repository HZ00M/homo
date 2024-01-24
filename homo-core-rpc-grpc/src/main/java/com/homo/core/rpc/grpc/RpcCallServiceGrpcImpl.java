package com.homo.core.rpc.grpc;

import brave.Span;
import com.google.protobuf.ByteString;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.rpc.grpc.proccessor.CallErrorProcessor;
import com.homo.core.rpc.grpc.proccessor.JsonCallErrorProcessor;
import com.homo.core.rpc.grpc.proccessor.StreamCallErrorProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.stub.StreamObserver;
import io.homo.proto.rpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcCallServiceGrpcImpl extends RpcCallServiceGrpc.RpcCallServiceImplBase {
    private final RpcServer rpcServer;

    public RpcCallServiceGrpcImpl(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    private void processMsgResult(StreamObserver<Res> responseObserver, Req req, byte[][] resData) {
        Res.Builder builder = Res.newBuilder().setMsgId(req.getMsgId());
        if (resData != null) {
            for (byte[] resDatum : resData) {
                if (resDatum == null) {
                    //不支持包含间断的空返回
                    break;
                }
                builder.addMsgContent(ByteString.copyFrom(resDatum));
            }
        }
        Res res = builder.build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    private void processMsgError(StreamObserver<Res> responseObserver, Req req, Throwable throwable) {
        try {
            Res res = CallErrorProcessor.processError(req, throwable);
            responseObserver.onNext(res);
            responseObserver.onCompleted();
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processError done")
                    .error(throwable);
        } catch (Exception e) {
            responseObserver.onError(e);
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processError catch")
                    .error(e);
        }
    }

    private void processStreamResult(StreamObserver<StreamRes> responseObserver, StreamReq req, byte[][] resData) {
        StreamRes.Builder builder = StreamRes.newBuilder().setMsgId(req.getMsgId());
        if (resData != null) {
            for (byte[] resDatum : resData) {
                if (resDatum == null) {
                    break;
                }
                builder.addMsgContent(ByteString.copyFrom(resDatum));
            }
        }
        builder.setReqId(req.getReqId());
        StreamRes res = builder.build();
        // 这里就是要对单个的responseObserver加锁
        synchronized (responseObserver) {
            responseObserver.onNext(res);
        }
    }

    private void processStreamError(StreamObserver<StreamRes> responseObserver, StreamReq req, Throwable throwable) {
        try {
            StreamRes res = StreamCallErrorProcessor.processError(req, throwable);
            synchronized (responseObserver) {
                responseObserver.onNext(res);
            }
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processStreamError done")
                    .error(throwable);
        } catch (Exception e) {
            synchronized (responseObserver) {
                responseObserver.onError(e);
            }
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processStreamError catch")
                    .error(e);
        }
    }

    private void processJsonResult(StreamObserver<JsonRes> responseObserver, JsonReq req, String resData) {
        JsonRes.Builder builder = JsonRes.newBuilder().setMsgId(req.getMsgId()).setMsgContent(resData);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private void processJsonError(StreamObserver<JsonRes> responseObserver, JsonReq req, Throwable throwable) {
        try {
            JsonRes errorRes = JsonCallErrorProcessor.processError(req, throwable);
            responseObserver.onNext(errorRes);
            responseObserver.onCompleted();
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processJsonError done")
                    .error(throwable);
        } catch (Exception e) {
            responseObserver.onError(e);
            ZipkinUtil.currentSpan()
                    .tag(ZipkinUtil.FINISH_TAG, "processJsonError catch")
                    .error(e);
        }
    }


    @Override
    public void rpcCall(Req req, StreamObserver<Res> responseObserver) {
        Span span = ZipkinUtil.currentSpan();
        byte[][] params = new byte[req.getMsgContentCount()][];
        for (int i = 0; i < req.getMsgContentCount(); i++) {
            params[i] = req.getMsgContent(i).toByteArray();
        }
        try {
            rpcServer.onCall(req.getSrcService(), req.getMsgId(), new ByteRpcContent(params, span))
                    .consumerValue(ret -> {
                        processMsgResult(responseObserver, req, ret);
                    })
                    .catchError(e -> {
                        processMsgError(responseObserver, req, e);
                    }).start();
        } catch (Exception e) {
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
                    byte[][] params = new byte[req.getMsgContentCount()][];
                    for (int i = 0; i < req.getMsgContentCount(); i++) {
                        params[i] = req.getMsgContent(i).toByteArray();
                    }
                    rpcServer.onCall(req.getSrcService(), req.getMsgId(), new ByteRpcContent(params, span))
                            .consumerValue(ret -> {
                                span.finish();
                                processStreamResult(responseObserver, req, ret);
                            })
                            .catchError(err -> {
                                span.error((Throwable) err);
                                processStreamError(responseObserver, req, err);
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
                log.error("RpcCallServiceImpl streamCall onCompleted responseObserver {}", responseObserver);
            }
        };
    }


    @Override
    public void jsonCall(JsonReq req, StreamObserver<JsonRes> responseObserver) {
        String msgContent = req.getMsgContent();
        Span span = ZipkinUtil.currentSpan().annotate("jsonCall start");
        try {
            rpcServer.onCall(req.getSrcService(), req.getMsgId(), JsonRpcContent.builder().data(msgContent).build())
                    .consumerValue(ret -> {
                        processJsonResult(responseObserver, req, ret);
                        span.annotate("jsonCall done");
                    })
                    .catchError(e -> {
                        processJsonError(responseObserver, req, e);
                        span.annotate("jsonCall error").error((e).getCause());
                    }).start();
        } catch (Exception e) {
            log.error("jsonCall SrcService {} MsgId {} error e", req.getSrcService(), req.getMsgId(), e);
        }

    }

}
