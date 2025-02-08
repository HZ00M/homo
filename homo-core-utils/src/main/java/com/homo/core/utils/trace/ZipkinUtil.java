package com.homo.core.utils.trace;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.http.HttpTracing;
import brave.internal.Nullable;
import brave.propagation.TraceContext;
import brave.rpc.RpcTracing;
import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import brave.sampler.SamplerFunction;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.homo.core.utils.apollo.ConfigDriver;
import com.homo.core.utils.config.ZipKinProperties;
import com.homo.core.utils.fun.ConsumerWithException;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.SupportModule;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.function.Consumer;

/**
 * ZipKin全链路跟踪工具类
 * zipkin追踪流程  clientSend->serverReceiver->serverSend->clientReceive
 * CS (Client Send)：客户端发送请求开始的时间戳。在分布式系统中，表示客户端发起请求的时刻。
 *
 * SR (Server Receive)：服务端接收到请求的时间戳。表示服务端开始处理请求的时刻。
 *
 * SS (Server Send)：服务端完成处理并发送响应的时间戳。表示服务端处理完请求并发送响应给客户端的时刻。
 *
 * CR (Client Receive)：客户端接收到响应的时间戳。表示客户端收到来自服务端的响应的时刻。
 */
@Slf4j
public class ZipkinUtil implements SupportModule {
    public static final String CLIENT_SEND_TAG = "cs";
    public static final String SERVER_RECEIVE_TAG = "sr";
    public static final String SERVER_SEND_TAG = "ss";
    public static final String CLIENT_RECEIVE_TAG = "cr";
    public static final String BEGIN_TAG = "begin";
    public static final String FINISH_TAG = "finish";
    public static Tracing tracing = null;
    public static RpcTracing grpcTracing;
    public static HttpTracing httpTracing;
    @Autowired
    private ZipKinProperties zipKinProperties;
    @Autowired
    private RootModule rootModule;
    @Autowired
    private ConfigDriver configDriver;

    //要与brave.Span区别开，Brave是Java版的Zipkin客户端
    private AsyncReporter<zipkin2.Span> asyncReporter;

    @Override
    public void moduleInit() {
//        update();
        configDriver.listenerNamespace(zipKinProperties.getZipkinNamespace(), new Consumer<ConfigChangeEvent>() {
            @Override
            public void accept(ConfigChangeEvent configChangeEvent) {
                log.info("ZipkinUtil configChangeEvent {}" ,configChangeEvent.changedKeys());
                update();
            }
        });
    }

    @Override
    public void afterAllModuleInit() {
        update();
    }

    private void update() {
        if (tracing != null) {
            log.info("tracing close");
            tracing.close();
        }
        if (asyncReporter != null) {
            log.info("asyncReporter close");
            asyncReporter.close();
        }
        String localServiceName = rootModule.getServerInfo().serverName;
        log.info(
                "zipkin update success,开关 [{}] 上报地址:[{}] ，本地服务名:{} 每秒采样次数:{}",
                zipKinProperties.isOpen,
                zipKinProperties.reportAddr,
                localServiceName,
                zipKinProperties.tracesPerSecond);
        Tracing.Builder tracingBuilder = Tracing.newBuilder();
        if (zipKinProperties.isOpen()) {
            log.info("zipkin report open {}", zipKinProperties.reportAddr);
            Sender sender = OkHttpSender.create(zipKinProperties.reportAddr);
            AsyncReporter<zipkin2.Span> reporter = AsyncReporter.create(sender);
            asyncReporter = reporter;
            tracingBuilder.addSpanHandler(ZipkinSpanHandler.create(reporter))
                    .localServiceName(localServiceName)
                    .sampler(RateLimitingSampler.create(zipKinProperties.tracesPerSecond))
                    .supportsJoin(zipKinProperties.supportsJoin);
        } else {
            log.info("zipkin report close!!");
            tracingBuilder.sampler(Sampler.NEVER_SAMPLE);
        }
        tracing = tracingBuilder.build();
        grpcTracing = RpcTracing.create(tracing);
        httpTracing = HttpTracing.create(tracing);
    }

    public static ClientInterceptor clientInterceptor() {
        return GrpcTracing.create(grpcTracing).newClientInterceptor();
    }

    public static ServerInterceptor serverInterceptor() {
        return GrpcTracing.create(grpcTracing).newServerInterceptor();
    }

    public static Tracing getTracing() {
        return tracing;
    }

    /**
     * 获取当前线程的span
     *
     * @return 当前span
     */
    public static Span currentSpan() {
        return getTracing().tracer().currentSpan();
    }

    /**
     * 创建一个新的客户端发起请求span
     *
     * @return 返回新创建的span
     */
    public static Span newCSSpan() {
        return getTracing().tracer().newTrace().kind(Span.Kind.CLIENT).annotate(CLIENT_SEND_TAG).start();
    }

    /**
     * 创建一个新的服务端接收请求span
     *
     * @return 返回新创建的span
     */
    public static Span newSRSpan() {
        return getTracing().tracer().newTrace().kind(Span.Kind.SERVER).annotate(SERVER_RECEIVE_TAG).start();
    }

    /**
     * 创建一个新的服务端处理完成span
     *
     * @return 返回新创建的span
     */
    public static Span newSSSpan() {
        return getTracing().tracer().newTrace().kind(Span.Kind.SERVER).annotate(SERVER_SEND_TAG).start();
    }

    /**
     * 创建一个客户端接收处理结果span
     *
     * @return 新创建的span, 如果currentSpan不为空，那么就会创建一个currentSpan的子span
     */
    public static Span newCRSpan() {
        return getTracing().tracer().newTrace().kind(Span.Kind.CLIENT).annotate(CLIENT_RECEIVE_TAG).start();
    }


    static <T> Span nextSpanWithParent(Span.Kind kind, String startAnnotate, SamplerFunction<T> samplerFunction, T arg, @Nullable TraceContext parent) {
        return getTracing()
                .tracer()
                .nextSpanWithParent(samplerFunction, arg, parent)
                .kind(kind)
                .annotate(startAnnotate)
                .start();
    }

    /**
     * 创建一个客户端发起请求span, 通常是为了异步过程创建一个span。
     *
     * @param samplerFunction 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg             采样函数的参数
     * @param parent          父级上下文
     * @param <T>             采样函数参数类型
     * @return 新span
     */
    public static <T> Span nextCSSpanWithParent(SamplerFunction<T> samplerFunction, T arg, @Nullable TraceContext parent) {
        return nextSpanWithParent(Span.Kind.CLIENT, CLIENT_SEND_TAG, samplerFunction, arg, parent);
    }


    /**
     * 创建一个服务端接收请求 span
     *
     * @return 新创建的span, 如果currentSpan不为空，那么就会创建一个currentSpan的子span
     */
    public static Span nextOrCreateSRSpan() {
        return getTracing().tracer().nextSpan().kind(Span.Kind.SERVER).annotate(SERVER_RECEIVE_TAG).start();
    }

    /**
     * 创建一个客户端发送请求span, 通常是为了异步过程创建一个span。
     *
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg      采样函数的参数
     * @param <T>      采样函数参数类型
     * @return 新span
     */
    public static <T> Span nextOrCreateCSSpan(SamplerFunction<T> function, T arg) {
        return nextSpan(Span.Kind.CLIENT, CLIENT_SEND_TAG, function, arg);
    }

    /**
     * 创建一个服务端接收请求span, 通常是为了异步过程创建一个span。
     *
     * @param samplerFunction 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg             采样函数的参数
     * @param parent          父级上下文
     * @param <T>             采样函数参数类型
     * @return 新span
     */
    public static <T> Span nextSRSpanWithParent(SamplerFunction<T> samplerFunction, T arg, @Nullable TraceContext parent) {
        return nextSpanWithParent(Span.Kind.SERVER, SERVER_RECEIVE_TAG, samplerFunction, arg, parent);
    }

    /**
     * 创建一个服务端span, 通常是为了异步过程创建一个span。
     *
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg      采样函数的参数
     * @param <T>      采样函数参数类型
     * @return 新span
     */
    public static <T> Span nextOrCreateSRSpan(SamplerFunction<T> function, T arg) {
        return nextSpan(Span.Kind.SERVER, SERVER_RECEIVE_TAG, function, arg);
    }


    static <T> Span nextSpan(Span.Kind kind, String startAnnotate, SamplerFunction<T> samplerFunction, T arg) {
        return getTracing()
                .tracer()
                .nextSpan(samplerFunction, arg)
                .kind(kind)
                .annotate(startAnnotate)
                .start();
    }

    /**
     * 如果当前没有被采样，创建一个服务端span, 根据采样规则决定是否采样, 如果当前已经span已经被采样，就返回当前span。
     *
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg 采样函数的参数
     * @param <T> 采样函数参数类型
     * @return 新span或当前span
     */
    public static <T> Span newSpanIfNotSampled(SamplerFunction<T> function, T arg) {
        Span span = getTracing().tracer().currentSpan();
        if (span.context().sampled()){
            return span;
        }
        return nextOrCreateSRSpan(function,arg);
    }

    /**
     * 将span包装成一个scope, 在scope关闭以前，可以通过currentSpan 获取到这个span
     *
     * @param span 需要包装的span
     * @return 包装的scope
     */
    public static Tracer.SpanInScope startScope(Span span) {
        return getTracing().tracer().withSpanInScope(span);
    }

    /**
     * 用span包装一个执行过程，并且在过程结束的时候触发一个onScopeClose的回调函数,在这个执行过程中可以通过currentSpan 获取到这个span
     *
     * @param span         绑定的span
     * @param consumer     执行函数
     * @param onScopeClose 结束时的回调函数
     * @return 返回绑定的span
     */
    public static Span startScope(Span span, ConsumerWithException<Span> consumer, @Nullable Consumer<Span> onScopeClose) {
        try (Tracer.SpanInScope ss = startScope(span)) {
            consumer.accept(span);
            return span;
        } catch (Exception throwable) {
            log.error("startScope error", throwable);
            span.error(throwable);
            return span;
        } finally {
            if (onScopeClose != null) {
                onScopeClose.accept(span);
            }
        }
    }
    /**
     * 用span包装一个执行过程，可抛出异常，并且在过程结束的时候出发一个onScopeClose的回调函数,在这个执行过程中可以通过currentSpan 获取到这个span
     *
     * @param span         绑定的span
     * @param consumer     执行函数
     * @param onScopeClose 结束时的回调函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static Span startScopeThrowable(Span span, ConsumerWithException<Span> consumer, Consumer<Span> onScopeClose) throws Exception {
        try (Tracer.SpanInScope ws = startScope(span)) {
            consumer.accept(span);
            return span;
        } catch (Exception throwable) {
            span.error(throwable);
            throw throwable;
        } finally {
            if (onScopeClose != null) {
                onScopeClose.accept(span);
            }
        }
    }

    /**
     * 用span包装一个执行过程，但是不上报，在这个执行过程中可以通过currentSpan 获取到这个span
     * 需要通过调用span.finish()才会真正上报，通常用于跟踪异步过程
     *
     * @param span     绑定的span
     * @param consumer 执行函数
     * @return 返回传入的span
     * @throws Exception 抛出异常
     */
    public static Span startScopeThrowable(Span span, ConsumerWithException<Span> consumer)
            throws Exception {
        return startScopeThrowable(span, consumer, null);
    }

    /**
     * 创建一个名为name参数的span包装一个执行过程，可抛出异常，并且在过程结束的时候出发一个onScopeClose的回调函数,在这个执行过程中可以通过currentSpan 获取到这个span
     * @param name 绑定的span
     * @param consumer 执行函数
     * @param onScopeClose 结束时的回调函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static Span startScopeThrowable(String name, ConsumerWithException<Span> consumer, Consumer<Span> onScopeClose)
            throws Exception {
        return startScopeThrowable(nextOrCreateSRSpan().name(name), consumer, onScopeClose);
    }

    /**
     * 如果当前没有被采样，创建一个服务端span, 根据采样规则决定是否采样, 如果当前已经span已经被采样，就返回当前span。
     * 用此span包装一个执行过程，可抛出异常，并且在过程结束的时候出发一个onScopeClose的回调函数,在这个执行过程中可以通过currentSpan 获取到这个span
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg 采样函数的参数   * @param consumer 执行函数
     * @param onScopeClose 结束时的回调函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static <T> Span startScopeThrowable(
            SamplerFunction<T> function,
            T arg,
            ConsumerWithException<Span> consumer,
            Consumer<Span> onScopeClose)
            throws Exception {
        return startScopeThrowable(null, function, arg, consumer, onScopeClose);
    }

    /**
     * 如果当前没有被采样，创建一个服务端span, 根据采样规则决定是否采样, 如果当前已经span已经被采样，就返回当前span。
     * 将这个span名字设置为name参数
     * 用此span包装一个执行过程，可抛出异常，并且在过程结束的时候出发一个onScopeClose的回调函数,在这个执行过程中可以通过currentSpan 获取到这个span
     * @param name 绑定的span
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg 采样函数的参数
     * @param consumer 执行函数
     * @param onScopeClose 结束时的回调函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static <T> Span startScopeThrowable(
            String name,
            SamplerFunction<T> function,
            T arg,
            ConsumerWithException<Span> consumer,
            Consumer<Span> onScopeClose)
            throws Exception {
        Span span = newSpanIfNotSampled(function, arg).name(name);
        return startScopeThrowable(span, consumer, onScopeClose);
    }

    /**
     * 如果当前没有被采样，创建一个服务端span, 根据采样规则决定是否采样, 如果当前已经span已经被采样，就返回当前span。
     * 用此span包装一个执行过程，并上报，可抛出异常, 在这个执行过程中可以通过currentSpan 获取到这个span
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg 采样函数的参数
     * @param consumer 执行函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static <T> Span startScopeAndFinish(String finishAnnotate, SamplerFunction<T> function, T arg, ConsumerWithException<Span> consumer) {
        return startScope(
                getTracing().tracer().nextSpan(function,arg).annotate(finishAnnotate),
                consumer,
                span -> span.tag(FINISH_TAG, "startScopeAndFinishThrowable").annotate(finishAnnotate).finish());
    }

    /**
     * 如果当前没有被采样，创建一个服务端span, 根据采样规则决定是否采样, 如果当前已经span已经被采样，就返回当前span。
     * 用此span包装一个执行过程，并上报，可抛出异常, 在这个执行过程中可以通过currentSpan 获取到这个span
     * @param function 采样函数，返回true表示会被采样，如果被采样，在开启zipkin的情况下回上报到收集程序
     * @param arg 采样函数的参数
     * @param consumer 执行函数
     * @return 返回绑定的span
     * @throws Exception 抛出异常
     */
    public static <T> Span startScopeAndFinishThrowable(String finishAnnotate, SamplerFunction<T> function, T arg, ConsumerWithException<Span> consumer) throws Exception {
        return startScopeThrowable(
                function,
                arg,
                consumer,
                span -> span.tag(FINISH_TAG, "startScopeAndFinishThrowable").annotate(finishAnnotate).finish());
    }

    /**
     * 用span包装一个执行过程，并上报，在这个执行过程中可以通过currentSpan 获取到这个span
     *
     * @param finishAnnotate 注释
     * @param span           绑定的span
     * @param consumer       执行函数
     * @return 返回传入的span
     */
    public static Span startScopeWithSamplerFunAndFinish(String finishAnnotate, Span span, ConsumerWithException<Span> consumer) {
        return startScope(span, consumer, identitySpan -> {
            identitySpan.tag(FINISH_TAG, "startSamplerFunAndFinish").annotate(finishAnnotate).finish();
        });
    }

    /**
     * 用span包装一个执行过程，并上报，在这个执行过程中可以通过currentSpan 获取到这个span
     *
     * @param finishAnnotate 注释
     * @param span           绑定的span
     * @param consumer       执行函数
     * @return 返回传入的span
     */
    public static Span startScopeWithSamplerFunAndFinishThrowable(String finishAnnotate, Span span, ConsumerWithException<Span> consumer) throws Exception {
        return startScopeThrowable(span, consumer, identitySpan -> identitySpan.tag(FINISH_TAG, "startScopeWithSamplerFunAndFinishThrowable").annotate(finishAnnotate).finish());
    }





}
