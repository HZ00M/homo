package com.homo.core.rpc.grpc;

import brave.Span;
import com.homo.core.configurable.rpc.RpcGrpcClientProperties;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.thread.ThreadPoolFactory;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.utils.fun.ConsumerWithException;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import io.grpc.stub.StreamObserver;
import io.homo.proto.rpc.*;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcRpcClient implements RpcClient {
    private final String host;
    private final Integer servicePort;
    private static final Object lock = new Object();
    private final boolean isDirectExecutor;
    private ManagedChannel channel;
    private final Map<ManagedChannel, Integer> channelReferenceMap;
    private final Map<ManagedChannel, Boolean> channelReleaseMap;
    private final Map<String, HomoSink<Tuple2<String, byte[][]>>> requestContextMap;
//    private final Map<String, Span> requestSpanMap;
    private final List<ClientInterceptor> clientInterceptorList;
    private final Set<String> addressSet;
    private final EventLoopGroup eventLoopGroup;
    private boolean servicePodChanged;
    private final boolean isStateful;
    private final int checkDelay;
    private final int checkPeriod;
    private final int messageMaxSize;
    private final int channelKeepLiveMills;
    private final int channelKeepLiveTimeoutMills;
    private StreamObserver<StreamReq> reqStreamObserver;

    public GrpcRpcClient(String host, int port, List<ClientInterceptor> clientInterceptorList, boolean isStateful, RpcGrpcClientProperties clientProperties) {
        this.host = host;
        this.servicePort = port;
        this.isStateful = isStateful;
        this.addressSet = new HashSet<>();
        this.channelReferenceMap = new HashMap<>();
        this.channelReleaseMap = new HashMap<>();
        this.requestContextMap = new ConcurrentHashMap<>(1024);
//        this.requestSpanMap = new ConcurrentHashMap<>(1024);
        this.isDirectExecutor = clientProperties.isDirector();
        this.clientInterceptorList = clientInterceptorList != null ? clientInterceptorList : Collections.emptyList();
        this.servicePodChanged = false;
        this.eventLoopGroup = new NioEventLoopGroup(clientProperties.getWorkerThread(), ThreadPoolFactory.newThreadPool("rpcClient", 2, 0));
        this.checkDelay = clientProperties.getCheckDelaySecond();
        this.checkPeriod = clientProperties.getCheckPeriodSecond();
        this.messageMaxSize = clientProperties.getMessageMaxSize();
        this.channelKeepLiveMills = clientProperties.getChannelKeepLiveMillsSecond();
        this.channelKeepLiveTimeoutMills = clientProperties.getChannelKeepLiveTimeoutMillsSecond();
        channel = buildChannel();
        updateStreamObserver(channel);
        lookupCheckAddress();
        Runtime.getRuntime().addShutdownHook(new Thread(GrpcRpcClient.this::shutdown));
    }

    private void lookupCheckAddress() {
        if (!isStateful) {
            //无状态服务器才需要动态负载，有状态服务器都是发送到指定的pod
            HomoTimerMgr.getInstance().schedule("lookupCheckAddress", new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress[] newAddress = InetAddress.getAllByName(host);
                        servicePodChanged = checkChannelChanged(newAddress);
                        if (servicePodChanged){
                            log.info("RpcCallClientGrpcImpl lookupCheckAddress service pod changed {} newAddress {}", servicePodChanged, newAddress);
                        }else {
                            log.trace("RpcCallClientGrpcImpl lookupCheckAddress service pod changed {} newAddress {}", servicePodChanged, newAddress);
                        }
                    } catch (Exception e) {
                        log.error("RpcCallClientGrpcImpl lookupCheckAddress parse service pod error,service {}", host);
                    }
                }
            }, checkDelay * 1000L, checkPeriod * 1000L, 0);
        }
    }

    private boolean checkChannelChanged(InetAddress[] newAddress) {
        for (InetAddress address : newAddress) {
            if (!addressSet.contains(address.toString())) {
                return true;
            }
        }
        return false;
    }

    public ManagedChannel getChannel(boolean playload) {
        synchronized (lock) {
            //如果pod改变，则重新建立channel，以更新变化
            //如果当前channel被使用，就先放入releaseMap打上标记,如果没有被使用，则关闭当前channel
            if (servicePodChanged && playload && channel != null) {
                Integer refCount = channelReferenceMap.get(channel);
                if (refCount != null && refCount <= 0) {
                    channel.shutdown();
                } else {
                    channelReleaseMap.put(channel, true);
                }
            }
            //判断channel是不是已经断开，如果是，则清理引用信息
            boolean isShutdown = false;
            if (channel != null && (channel.isTerminated() || channel.isShutdown())) {
                channelReferenceMap.remove(channel);
                channelReleaseMap.remove(channel);
                isShutdown = true;

            }
            //如果channel不存在，或者channel已经被标记清除，或者channel已经断开，重新建立channel
            if (channel == null || channelReleaseMap.get(channel) != null || isShutdown) {
                channel = buildChannel();
                updateStreamObserver(channel);
            }
            //如果新请求，则增加引用计数
            if (playload) {
                Integer old = channelReferenceMap.computeIfAbsent(channel, managedChannel -> 0);
                channelReferenceMap.put(channel, ++old);
            }
            return channel;
        }
    }

    public void releaseChannel(ManagedChannel channel) {
        synchronized (lock) {
            Integer newRef = channelReferenceMap.computeIfPresent(channel, (managedChannel, oldRef) -> oldRef - 1);
            //如果没有引用，就清除channel的引用，然后断开channel
            if (newRef != null && newRef <= 0 && channelReleaseMap.get(channel) != null) {
                log.error("RpcCallClientGrpcImpl releaseChannel channel shutdown  channel {}", channel);
                channelReleaseMap.remove(channel);
                channelReferenceMap.remove(channel);
                channel.shutdown();
            }
        }
    }


    private ManagedChannel buildChannel() {
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forTarget("dns:///" + this.host + ":" + servicePort)
                .eventLoopGroup(eventLoopGroup)
                .channelType(NioSocketChannel.class)
                .usePlaintext()
                .intercept(clientInterceptorList)
                .keepAliveTime(channelKeepLiveMills, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(channelKeepLiveTimeoutMills, TimeUnit.MILLISECONDS)
                .maxInboundMessageSize(messageMaxSize)//最大消息大小 5M
                .defaultLoadBalancingPolicy("round_robin")//默认负载均衡策略
                .keepAliveWithoutCalls(true);
        if (isDirectExecutor) {
            nettyChannelBuilder.directExecutor();
        } else {
            nettyChannelBuilder.executor(ThreadPoolFactory.newThreadPool("DEFAULT_RPC_POOL", 1, 0));
        }
        try {
            parseAddressToSet(InetAddress.getAllByName(this.host));
            servicePodChanged = false;
        } catch (Exception e) {
            log.error("RpcCallClientGrpcImpl buildChannel parse service address error, serviceName {} port {}", host, servicePort, e);
            addressSet.clear();
            return null;
        }
        return nettyChannelBuilder.build();
    }

    private void parseAddressToSet(InetAddress[] addresses) {
        addressSet.clear();
        for (InetAddress inetAddress : addresses) {
            addressSet.add(inetAddress.toString());
        }
    }

    private void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("RpcCallClientGrpcImpl shutdown error ", e);
        }
    }

    @Override
    public Homo asyncBytesCall(Req req) {
        log.info("asyncBytesCall req {}",req);
        ManagedChannel callChannel = getChannel(true);
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(callChannel);//多路复用
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.currentSpan().annotate(ZipkinUtil.CLIENT_SEND_TAG);;
        TraceLogUtil.setTraceIdBySpan(span,"rpcClient asyncBytesCall");
        Homo<Tuple2<String, byte[]>> warp = Homo.warp(new ConsumerWithException<HomoSink<Tuple2<String, byte[]>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, byte[]>> sink) throws Exception {
                StreamObserver<Res> observer = new StreamObserver<Res>() {
                    private byte[][] results = null;
                    private String msgId;

                    @Override
                    public void onNext(Res reply) {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncBytesCall onNext");
                        log.trace("asyncBytesCall onNext, serviceName {} msgId {} content size {}", host, reply.getMsgId(), reply.getMsgContentCount());
                        if (reply.getMsgContentCount() > 0) {
                            results = new byte[reply.getMsgContentCount()][];
                            for (int i = 0; i < reply.getMsgContentCount(); i++) {
                                results[i] = reply.getMsgContent(i).toByteArray();
                            }
                        }
                        msgId = reply.getMsgId();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncBytesCall onError");
                        log.trace("asyncBytesCall onError, serviceName {} msgId {}", host, msgId);
                        span.error(throwable);
                        sink.error(throwable);
                        releaseChannel(callChannel);
                    }

                    @Override
                    public void onCompleted() {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncBytesCall onCompleted");
                        log.trace("asyncBytesCall onCompleted, serviceName {} msgId {} resultsSize {}", host, msgId,results.length);
                        span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                        //目前看来返回值不大可能会有多个，所以这里暂时只返回result[0]
                        sink.success(Tuples.of(msgId, results[0]));
                        releaseChannel(callChannel);
                    }
                };
                try {
                    stub.rpcCall(req, observer);
                } catch (Exception e) {
                    span.error(e);
                    sink.error(e);
                    log.error("asyncBytesCall catch error targetServiceName {} funName {}", host, req.getMsgId(), e);
                    releaseChannel(GrpcRpcClient.this.channel);
                }
            }
        });
        return warp.switchThread(callQueue,span);
    }

    public void updateStreamObserver(ManagedChannel channel) {
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(channel);
        reqStreamObserver = stub.streamCall(new StreamObserver<StreamRes>() {
            private byte[][] results;

            @Override
            public void onNext(StreamRes reply) {
                log.info("asyncBytesStreamCall onNext msgId {} contentSize {} ReqId {} ", reply.getMsgId(), reply.getMsgContentCount(), reply.getReqId());
                results = null;
                if (reply.getMsgContentCount() > 0) {
                    results = new byte[reply.getMsgContentCount()][];
                    for (int i = 0; i < reply.getMsgContentCount(); i++) {
                        results[i] = reply.getMsgContent(i).toByteArray();
                    }
                }
                String msgId = reply.getMsgId();
                HomoSink<Tuple2<String, byte[][]>> sink = requestContextMap.get(reply.getReqId());
                if (sink != null) {
                    log.info("asyncBytesStreamCall reply msgId {} contentSize {} ReqId {}", msgId, reply.getMsgContentCount(), reply.getReqId());
                    log.info("requestContextMap remove sink timeout reqId {}", reply.getReqId());
                    requestContextMap.remove(reply.getReqId());
                    sink.success(Tuples.of(msgId, results));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("asyncBytesStreamCall onError ", throwable);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                log.error("asyncBytesStreamCall onCompleted ");
                channel.shutdown();
            }
        });
    }

    @Override
    public Homo<Tuple2<String, byte[][]>> asyncBytesStreamCall(String reqId, StreamReq streamReq) {
        log.info("asyncBytesStreamCall reqId {} call ", reqId);
        ManagedChannel callChannel = getChannel(true);//检测更新channel
        Homo<Tuple2<String, byte[][]>> warp = Homo.warp(new ConsumerWithException<HomoSink<Tuple2<String, byte[][]>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, byte[][]>> sink) throws Exception {
                log.info("requestContextMap put sink reqId {}", reqId);
                requestContextMap.put(reqId, sink);//保存请求上下文
            }
        });
        //请求将在10秒后过期
        HomoTimerMgr.getInstance().once("rpcTimeOut", new Runnable() {
            @Override
            public void run() {
                log.info("asyncBytesStreamCall reqId {} timer run ", reqId);
                if (requestContextMap.containsKey(reqId)) {
                    log.info("requestContextMap remove sink timeout reqId {}", reqId);
                    HomoSink<Tuple2<String, byte[][]>> sink = requestContextMap.remove(reqId);
                    HomoException timeoutException = HomoError.throwError(HomoError.rpcTimeOutException);
                    sink.error(timeoutException);
                }
            }
        }, 10000);
        try {
            synchronized (reqStreamObserver) {
                reqStreamObserver.onNext(streamReq);
            }
        } catch (Exception e) {
            log.info("asyncBytesStreamCall catch error, targetServiceName {} funName {}", streamReq.getSrcService(), streamReq.getMsgId(), e);
            requestContextMap.get(reqId).error(e);
        }
        return warp;
    }

    @Override
    public Homo<Tuple2<String, String>> asyncJsonCall(JsonReq jsonReq) {
        log.info("asyncJsonCall jsonReq {}",jsonReq);
        ManagedChannel channel = getChannel(true);
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(channel);
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.currentSpan().annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Tuple2<String, String>> warp = Homo.warp(new ConsumerWithException<HomoSink<Tuple2<String, String>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, String>> sink) throws Exception {
                StreamObserver<JsonRes> observer = new StreamObserver<JsonRes>() {
                    private String results;
                    private String msgId;
                    @Override
                    public void onNext(JsonRes reply) {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncJsonCall onNext");
                        log.trace("asyncJsonCall onError, serviceName {} msgId {}", host, msgId);
                        span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                        results = reply.getMsgContent();
                        msgId = reply.getMsgId();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncJsonCall throwable");
                        log.trace("asyncJsonCall onCompleted, serviceName {} msgId {}", host, msgId);
                        span.error(throwable);
                        sink.error(throwable);
                        releaseChannel(channel);
                    }

                    @Override
                    public void onCompleted() {
                        TraceLogUtil.setTraceIdBySpan(span,"asyncJsonCall throwable");
                        log.trace("asyncJsonCall onCompleted, serviceName {} msgId {}", host, msgId);
                        span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG);
                        sink.success(Tuples.of(msgId, results));
                        releaseChannel(channel);
                    }
                };
                try {
                    stub.jsonCall(jsonReq, observer);
                } catch (Exception e) {
                    span.error(e);
                    sink.error(e);
                    releaseChannel(channel);
                }
            }
        });
        return warp.switchThread(callQueue,span);
    }
}
