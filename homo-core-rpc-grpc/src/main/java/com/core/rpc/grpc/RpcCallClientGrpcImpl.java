package com.core.rpc.grpc;

import com.homo.concurrent.schedule.HomoTimerMgr;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.concurrent.thread.ThreadPoolFactory;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.rpc.base.serial.BytesArrayRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.utils.fun.ConsumerEx;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
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
public class RpcCallClientGrpcImpl implements RpcClient {
    private final String serviceName;
    private final Integer servicePort;
    private static final Object lock = new Object();
    private final boolean isDirectExecutor;
    private ManagedChannel channel;
    private final Map<ManagedChannel, Integer> channelReferenceMap;
    private final Map<ManagedChannel, Boolean> channelReleaseMap;
    private final Map<String, HomoSink<Tuple2<String, BytesArrayRpcContent>>> requestContextMap;
    private final List<ClientInterceptor> clientInterceptorList;
    private final Set<String> addressSet;
    private final EventLoopGroup eventLoopGroup;
    private boolean servicePodChanged;
    private final boolean isStateful;
    private StreamObserver<StreamReq> reqStreamObserver;

    public RpcCallClientGrpcImpl(String hostname, int port, List<ClientInterceptor> clientInterceptorList, boolean isDirectExecutor, boolean isStateful) {
        this.serviceName = hostname;
        this.servicePort = port;
        this.isStateful = isStateful;
        this.addressSet = new HashSet<>();
        this.channelReferenceMap = new HashMap<>();
        this.channelReleaseMap = new HashMap<>();
        this.requestContextMap = new ConcurrentHashMap<>(1024);
        this.isDirectExecutor = isDirectExecutor;
        this.clientInterceptorList = clientInterceptorList != null ? clientInterceptorList : Collections.emptyList();
        this.servicePodChanged = false;
        this.eventLoopGroup = new NioEventLoopGroup(2, ThreadPoolFactory.newThreadPool("RPC_POOL", 2, 0));

        channel = buildChannel();
        updateStreamObserver(channel);
        lookupCheckAddress();
        Runtime.getRuntime().addShutdownHook(new Thread(RpcCallClientGrpcImpl.this::shutdown));
    }

    private void lookupCheckAddress() {
        if (!isStateful) {
            //无状态服务器才需要动态负载，有状态服务器都是发送到指定的pod
            HomoTimerMgr.getInstance().schedule(new TaskFun0() {
                @Override
                public void run() {
                    try {
                        InetAddress[] newAddress = InetAddress.getAllByName(serviceName);
                        servicePodChanged = checkChannelChanged(newAddress);
                    } catch (Exception e) {
                        log.error("RpcCallClientGrpcImpl lookupCheckAddress parse service pod error,service {}", serviceName);
                    }
                }
            }, 0, 5, 0);
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
                if (refCount != null && refCount < 0) {
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
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forTarget("dns:///" + this.serviceName + ":" + servicePort)
                .eventLoopGroup(eventLoopGroup)
                .channelType(NioSocketChannel.class)
                .usePlaintext()
                .intercept(clientInterceptorList)
                .keepAliveTime(5000, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(5000, TimeUnit.MILLISECONDS)
                .maxInboundMessageSize(5 * 1024 * 1024)//最大消息大小 5M
                .defaultLoadBalancingPolicy("round_robin")//默认负载均衡策略
                .keepAliveWithoutCalls(true);
        if (isDirectExecutor) {
            nettyChannelBuilder.directExecutor();
        } else {
            nettyChannelBuilder.executor(ThreadPoolFactory.newThreadPool("DEFAULT_RPC_POOL", 1, 0));
        }
        try {
            parseAddressToSet(InetAddress.getAllByName(this.serviceName));
            servicePodChanged = false;
        } catch (Exception e) {
            log.error("RpcCallClientGrpcImpl buildChannel parse service address error, serviceName {} port {}", serviceName, servicePort, e);
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
    public Homo<Tuple2<String, BytesArrayRpcContent>> asyncBytesCall(Req req) {
        ManagedChannel callChannel = getChannel(true);
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(getChannel(true));//多路复用
        Homo<Tuple2<String, BytesArrayRpcContent>> result = Homo.warp(new ConsumerEx<HomoSink<Tuple2<String, BytesArrayRpcContent>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, BytesArrayRpcContent>> sink) throws Exception {
                StreamObserver<Res> observer = new StreamObserver<Res>() {
                    private byte[][] results;
                    private String msgId;

                    @Override
                    public void onNext(Res reply) {
                        log.trace("asyncBytesCall onNext, serviceName {} msgId {} content size {}", serviceName, reply.getMsgId(), reply.getMsgContentCount());
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
                        log.trace("asyncBytesCall onError, serviceName {} msgId {}", serviceName, msgId);
                        sink.error(throwable);
                        releaseChannel(callChannel);
                    }

                    @Override
                    public void onCompleted() {
                        log.trace("asyncBytesCall onCompleted, serviceName {} msgId {}", serviceName, msgId);
                        sink.success(Tuples.of(msgId, BytesArrayRpcContent.builder().data(results).build()));
                        releaseChannel(callChannel);
                    }
                };
                try {
                    stub.rpcCall(req, observer);
                } catch (Exception e) {
                    sink.error(e);
                    log.error("asyncBytesCall catch error targetServiceName {} funName {}", serviceName, req.getMsgId(), e);
                    releaseChannel(RpcCallClientGrpcImpl.this.channel);
                }
            }
        });
        return result;
    }

    public void updateStreamObserver(ManagedChannel channel) {
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(channel);
        reqStreamObserver = stub.streamCall(new StreamObserver<StreamRes>() {
            private byte[][] results;

            @Override
            public void onNext(StreamRes reply) {
                log.info("asyncBytesStreamCall reply msgId {} contentSize {} ReqId {}", reply.getMsgId(), reply.getMsgContentCount(), reply.getReqId());

                results = null;
                if (reply.getMsgContentCount() > 0) {
                    for (int i = 0; i < reply.getMsgContentCount(); i++) {
                        results[i] = reply.getMsgContent(i).toByteArray();
                    }
                }
                String msgId = reply.getMsgId();
                HomoSink<Tuple2<String, BytesArrayRpcContent>> sink = requestContextMap.get(reply.getReqId());
                if (sink != null) {
                    requestContextMap.remove(reply.getReqId());
                    sink.success(Tuples.of(msgId, BytesArrayRpcContent.builder().data(results).build()));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("asyncBytesStreamCall onError completed",throwable);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                log.error("asyncBytesStreamCall onCompleted completed");
                channel.shutdown();
            }
        });
    }

    @Override
    public Homo<Tuple2<String, BytesArrayRpcContent>> asyncBytesStreamCall(String reqId, StreamReq streamReq) {
        Homo<Tuple2<String, BytesArrayRpcContent>> result = Homo.warp(new ConsumerEx<HomoSink<Tuple2<String, BytesArrayRpcContent>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, BytesArrayRpcContent>> sink) throws Exception {
                requestContextMap.put(reqId, sink);//保存请求上下文
            }
        });
        //请求将在10秒后过期
        HomoTimerMgr.getInstance().once(new TaskFun0() {
            @Override
            public void run() {
                ZipkinUtil.getTracing().tracer().currentSpan().tag("tag", "asyncBytesStreamCall");
                if (requestContextMap.containsKey(reqId)) {
                    HomoSink<Tuple2<String, BytesArrayRpcContent>> sink = requestContextMap.remove(reqId);
                    sink.error(new RpcTimeOutException("asyncBytesStreamCall time out"));
                }
            }
        }, 10000);
        try {
            ZipkinUtil.currentSpan().annotate("grpc_send");
            synchronized (reqStreamObserver){
                reqStreamObserver.onNext(streamReq);
            }
        }catch (Exception e){
            log.info("asyncBytesStreamCall catch error, targetServiceName {} funName {}", streamReq.getSrcService(), streamReq.getMsgId(), e);
            requestContextMap.get(reqId).error(e);
        }
        return result;
    }

    @Override
    public Homo<Tuple2<String, JsonRpcContent>> asyncJsonCall(JsonReq jsonReq) {
        ManagedChannel channel = getChannel(true);
        RpcCallServiceGrpc.RpcCallServiceStub stub = RpcCallServiceGrpc.newStub(channel);
        Homo<Tuple2<String, JsonRpcContent>> result = Homo.warp(new ConsumerEx<HomoSink<Tuple2<String, JsonRpcContent>>>() {
            @Override
            public void accept(HomoSink<Tuple2<String, JsonRpcContent>> sink) throws Exception {
                StreamObserver<JsonRes> observer = new StreamObserver<JsonRes>() {
                    private String results;
                    private String msgId;

                    @Override
                    public void onNext(JsonRes reply) {
                        log.trace("asyncJsonCall onError, serviceName {} msgId {}", serviceName, msgId);
                        results = reply.getMsgContent();
                        msgId = reply.getMsgId();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.trace("asyncJsonCall onCompleted, serviceName {} msgId {}", serviceName, msgId);
                        sink.error(throwable);
                        releaseChannel(channel);
                    }

                    @Override
                    public void onCompleted() {
                        log.trace("asyncJsonCall onCompleted, serviceName {} msgId {}", serviceName, msgId);
                        sink.success(Tuples.of(msgId, JsonRpcContent.builder().data(results).build()));
                        releaseChannel(channel);
                    }
                };
                try {
                    stub.jsonCall(jsonReq, observer);
                } catch (Exception e) {
                    sink.error(e);
                    releaseChannel(channel);
                }
            }
        });
        return result;
    }
}
