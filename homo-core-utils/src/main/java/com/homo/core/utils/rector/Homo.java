package com.homo.core.utils.rector;

import brave.Span;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.concurrent.event.SwitchThreadEvent;
import com.homo.core.utils.concurrent.lock.IdLocker;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.CallQueueProducer;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.fun.ConsumerWithException;
import com.homo.core.utils.fun.FuncWithException;
import com.homo.core.utils.fun.SupplierWithException;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.*;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.*;

@Slf4j
public class Homo<T> extends Mono<T> {
    private final Mono<T> mono;

    public Homo(Mono<T> mono) {
        this.mono = mono;
    }

    public static <T> Homo<T> queue(IdCallQueue idCallQueue, Callable<Homo<T>> callable, Runnable errCb) {
        return Homo.warp(new ConsumerWithException<HomoSink<T>>() {
            @Override
            public void accept(HomoSink<T> tHomoSink) throws Exception {
                idCallQueue.addIdTask(callable, errCb, tHomoSink);
            }
        });
    }

    public static Homo<Void> resultVoid() {
        return Homo.warp(Mono.empty());
    }

    public final Homo<T> switchToCurrentThread() {
        Span span = ZipkinUtil.currentSpan();
        return switchToCurrentThread(span);
    }

    public final Homo<T> switchToCurrentThread(Span span) {
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        return switchThread(callQueue, span);
    }

    public final Homo<T> switchThread(CallQueueProducer producer, Span span) {
        return producer == null ? this : this.switchThread(producer.getQueueId(), span);
    }

    public final Homo<T> switchThreadByString(String uniqueId, Span span) {
        return switchThreadByHashCode(uniqueId.hashCode(), span);
    }

    public final Homo<T> switchThreadByHashCode(int hashCode, Span span) {
        CallQueue callQueue = CallQueueMgr.getInstance().getQueueByHashCode(hashCode);
        return switchThread(callQueue, span);
    }


    public final Homo<T> switchThread(CallQueue callQueue, Span span) {
        return this.nextDo(ret -> {
            return CallQueueMgr.getInstance().isThreadChanged(callQueue) ?
                    warp(sink -> {
                        SwitchThreadEvent event;
//                        CallQueue localQueue = CallQueueMgr.getInstance().getLocalQueue();
//                        String fromInfo = localQueue == null ? Thread.currentThread().getName() : localQueue.name();
                        String fromInfo = Thread.currentThread().getName();
                        String targetInfo = callQueue.name();
                        if (span == null) {
                            event = new SwitchThreadEvent(fromInfo, targetInfo, sink, ret, ZipkinUtil.getTracing().tracer().currentSpan());
                        } else {
                            event = new SwitchThreadEvent(fromInfo, targetInfo, sink, ret, span);
                        }
                        callQueue.addEvent(event);
                    }) :
                    result(ret);
        });
    }

    public final Homo<T> switchThread(int callQueueId, Span span) {
        CallQueue callQueue = CallQueueMgr.getInstance().getQueue(callQueueId);
        return switchThread(callQueue, span);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        mono.subscribe(actual);
    }

    public static <T> Homo<T> warp(Mono<T> mono) {
        if (mono instanceof Homo) {
            return (Homo<T>) mono;
        }
        return new Homo<T>(mono);
    }

    public static Homo<Void> when(final Iterable<? extends Publisher<?>> sources) {
        return Homo.warp(Mono.when(sources));
    }

    public static <T> Homo<T> warp(Supplier<Mono<T>> supplier) {
        Supplier<Mono<T>> warp = () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                return Homo.error(e);
            }
        };
        return Homo.warp(Mono.defer(warp));
    }

    public static <T> Homo<T> warp(ConsumerWithException<HomoSink<T>> callback) {
        Consumer<MonoSink<T>> consumer =
                monoSink -> {
                    try {
                        HomoSink<T> homoSink = new HomoSink<>(monoSink);
                        callback.accept(homoSink);
                    } catch (Exception e) {
                        e.printStackTrace();
                        monoSink.error(e);
                    }
                };
        return Homo.warp(Mono.create(consumer));
    }

    public <V> Homo<V> justThen(Homo<V> other) {
        return Homo.warp(mono.then(other));
    }

    public <V> Homo<V> justThen(Supplier<Mono<V>> supplier) {
        return justThen(Homo.warp(supplier));
    }

    public final Homo<T> ifEmptyThen(Homo<? extends T> alternate) {
        return Homo.warp(mono.switchIfEmpty(alternate));
    }

    public final <R> Homo<R> nextDo(FuncWithException<? super T, ? extends Mono<? extends R>> transformer) {
        Function<? super T, ? extends Mono<? extends R>> warp = (Function<T, Mono<? extends R>>) origin -> {
            if (origin == Optional.empty()) {
                origin = null;
            }
            try {
                return transformer.apply(origin);
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
        return Homo.warp(mono.flatMap(warp));
    }

    public final <R> Homo<R> nextValue(Function<? super T, ? extends R> mapper) {
        Function<? super T, ? extends Homo<R>> warp = (Function<T, Homo<R>>) t -> {
            try {
                if (t == Optional.empty()) {
                    t = null;
                }
                R ret = null;
                ret = mapper.apply(t);
                if (ret == null) {
                    return Homo.result((R) Optional.empty());
                }
                return Homo.result(ret);
            } catch (Exception e) {
                return Homo.error(e);
            }
        };
        return Homo.warp(mono.flatMap(warp));
    }

    public final Homo<T> consumerValue(Consumer<? super T> onSuccess) {
        Consumer<? super T> warp = (Consumer<T>) t -> {
            if (t != null) {
                if (t == Optional.empty()) {
                    t = null;
                }
                onSuccess.accept(t);
            } else {
                onSuccess.accept(t);
            }
        };
        return Homo.warp(mono.doOnSuccess(warp));
    }

    public final Homo<T> consumerEmpty(Runnable onEmpty) {
        return Homo.warp(mono.doOnSuccess(new Consumer<T>() {
            @Override
            public void accept(T t) {
                if (t == null) {
                    onEmpty.run();
                }
            }
        }));
    }

    public final Homo<T> catchError(Consumer<? super Throwable> onError) {
        return Homo.warp(mono.doOnError(onError));
    }

    public final Homo<T> onErrorContinue(Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
        Function<? super Throwable, ? extends Mono<? extends T>> warp = new Function<Throwable, Mono<? extends T>>() {
            @Override
            public Mono<? extends T> apply(Throwable throwable) {
                return fallback.apply(throwable);
            }
        };
        return Homo.warp(mono.onErrorResume(warp));
    }

    public static <T> Homo<T> error(Throwable error) {
        return Homo.warp(Mono.error(error));
    }

    public final Homo<T> finallySignal(Consumer<SignalType> onFinally) {
        return Homo.warp(mono.doFinally(onFinally));
    }

    public final Disposable start() {
        return mono.subscribe();
    }

    public static <T> Homo<T> result() {
        return Homo.warp(Mono.empty());
    }

    public static <T> Homo<T> result(T data) {
        if (data == null) {
            data = (T) Optional.empty();
        }
        return Homo.warp(Mono.just(data));
    }

    public static <T1, T2> Homo<Tuple2<T1, T2>> all(Mono<? extends T1> p1, Mono<? extends T2> p2) {
        return Homo.warp(Mono.zip(p1, p2));
    }

    public static <T1, T2, T3> Homo<Tuple3<T1, T2, T3>> all(Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3) {
        return Homo.warp(Mono.zip(p1, p2, p3));
    }

    public static <T1, T2, T3, T4> Homo<Tuple4<T1, T2, T3, T4>> all(Mono<? extends T1> p1,
                                                                    Mono<? extends T2> p2,
                                                                    Mono<? extends T3> p3,
                                                                    Mono<? extends T4> p4) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4));
    }


    public static <T1, T2, T3, T4, T5> Homo<Tuple5<T1, T2, T3, T4, T5>> all(Mono<? extends T1> p1,
                                                                            Mono<? extends T2> p2,
                                                                            Mono<? extends T3> p3,
                                                                            Mono<? extends T4> p4,
                                                                            Mono<? extends T5> p5) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5));
    }

    public static <T1, T2, T3, T4, T5, T6> Homo<Tuple6<T1, T2, T3, T4, T5, T6>> all(Mono<? extends T1> p1,
                                                                                    Mono<? extends T2> p2,
                                                                                    Mono<? extends T3> p3,
                                                                                    Mono<? extends T4> p4,
                                                                                    Mono<? extends T5> p5,
                                                                                    Mono<? extends T6> p6) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5, p6));
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Homo<Tuple7<T1, T2, T3, T4, T5, T6, T7>> all(Mono<? extends T1> p1,
                                                                                            Mono<? extends T2> p2,
                                                                                            Mono<? extends T3> p3,
                                                                                            Mono<? extends T4> p4,
                                                                                            Mono<? extends T5> p5,
                                                                                            Mono<? extends T6> p6,
                                                                                            Mono<? extends T7> p7) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5, p6, p7));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Homo<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> all(Mono<? extends T1> p1,
                                                                                                    Mono<? extends T2> p2,
                                                                                                    Mono<? extends T3> p3,
                                                                                                    Mono<? extends T4> p4,
                                                                                                    Mono<? extends T5> p5,
                                                                                                    Mono<? extends T6> p6,
                                                                                                    Mono<? extends T7> p7,
                                                                                                    Mono<? extends T8> p8) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5, p6, p7, p8));
    }

    public static <R> Homo<R> all(final Iterable<? extends Homo<?>> monos, Function<? super Object[], ? extends R> combinator) {
        return Homo.warp(Mono.zip(monos, combinator));
    }

    public final Homo<T> errorContinue(
            FuncWithException<? super Throwable, ? extends Mono<? extends T>> fallback) {
        Function<? super Throwable, ? extends Mono<? extends T>> warp =
                (Function<? super Throwable, Mono<? extends T>>)
                        t -> {
                            try {
                                return fallback.apply(t);
                            } catch (Throwable e) {
                                return Mono.error(e);
                            }
                        };

        return Homo.warp(mono.onErrorResume(warp));
    }

    public static final BiFunction<Integer, Class<? extends Throwable>, Predicate<Throwable>> matchRetryFun = ((retries, exception) -> (throwable) -> {
        if (retries <= 0) {
            return false;
        }
        return exception.isInstance(throwable);
    });

    /**
     * Set module Scheduler on which to execute the delays computed by the exponential backoff strategy.
     * for fixed delays (min backoff equals max backoff, no jitter), given module maximum number of retry attempts and the fixed Duration for the backoff.
     * Note that calling RetryBackoffSpec.minBackoff(Duration) or RetryBackoffSpec.maxBackoff(Duration) would switch back to an exponential backoff strategy.
     *
     * @param retryCount       – the maximum number of retry attempts to allow
     * @param retryDelaySecond – retryDelaySecond of the fixed delays
     * @param exception        – the exception can be retried
     * @return the fixed delays spec for further configuration
     */
    public final Homo<T> retry(int retryCount, int retryDelaySecond, Class<? extends Throwable> exception) {
        Predicate<Throwable> retryPredicate = matchRetryFun.apply(retryCount, exception);
        return Homo.warp(
                mono.retryWhen(
                        Retry.fixedDelay(retryCount, Duration.ofSeconds(retryDelaySecond))
                                .scheduler(Schedulers.single())
                                .filter(retryPredicate)
                                .doBeforeRetry(retrySignal -> log.warn("retry on the exception {} starting , times {}", retrySignal.failure(), retrySignal.totalRetries() + 1))
                )
        );
    }

    public static final BiFunction<Integer, Collection<Class<? extends Throwable>>, Predicate<Throwable>> anyMatchRetryFun = ((retries, exceptions) -> (throwable) -> {
        if (retries <= 0 || exceptions.isEmpty()) {
            return false;
        }
        return exceptions.stream().anyMatch(exception -> exception.isInstance(throwable));
    });

    /**
     * Set module Scheduler on which to execute the delays computed by the exponential backoff strategy.
     * for fixed delays (min backoff equals max backoff, no jitter), given module maximum number of retry attempts and the fixed Duration for the backoff.
     * Note that calling RetryBackoffSpec.minBackoff(Duration) or RetryBackoffSpec.maxBackoff(Duration) would switch back to an exponential backoff strategy.
     *
     * @param retryCount – the maximum number of retry attempts to allow
     * @param duration   – the Duration of the fixed delays
     * @param exceptions – the predicate to filter which exceptions can be retried
     * @return the fixed delays spec for further configuration
     */
    public final Homo<T> retry(int retryCount, Duration duration, Class<? extends Throwable>... exceptions) {
        Predicate<Throwable> retryPredicate = anyMatchRetryFun.apply(retryCount, Arrays.asList(exceptions));
        return Homo.warp(
                mono.retryWhen(
                        Retry.fixedDelay(retryCount, duration)
                                .scheduler(Schedulers.single())
                                .filter(retryPredicate)
                                .doBeforeRetry(retrySignal -> log.warn("retry on the exception {} starting , times {}", retrySignal.failure(), retrySignal.totalRetries() + 1))
                )
        );
    }

    public Homo<T> zipCalling(String tag) {
        return zipToBeforeCalling(tag, ()->this);
    }

    private static final IdLocker lockForZipCall= new IdLocker();
    private static final ConcurrentHashMap<String, ConcurrentLinkedDeque<CallBack<Object>>> tagToZipCallContext = new ConcurrentHashMap<>();

    /**
     * 将呼叫压缩到之前的tag
     * 如果之前有未完成的调用，就不用发起新的调用，等待之前的调用返回时统一返回
     * @param tag tag
     * @param supplier 提供一个异步请求
     * @param <T> 返回值类型参数
     * @return 异步调用句柄
     */
    public static <T> Homo<T> zipToBeforeCalling(String tag, SupplierWithException<Homo<T>> supplier) {
        ConsumerWithException<HomoSink<T>> consumer =
                sink->{
                    CallBack<Object> retFunc = new CallBack<Object>() {
                        @Override
                        public void onBack(Object value) {
                            sink.success((T)value);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.error("zipToBeforeCalling ObjCallBack onError, {} :", tag, throwable);
                            sink.error(throwable);
                        }
                    };
                    lockForZipCall.lock(tag,()->{
                        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
                        // 到这里为止都是同步的， 为每个对象的读取请求创建一个专属的回调队列
                        ConcurrentLinkedDeque<CallBack<Object>> concurrentLinkedDeque =
                                tagToZipCallContext.computeIfAbsent(tag, newTag -> new ConcurrentLinkedDeque<>());
                        CallBack<Object> previousFun = concurrentLinkedDeque.peek();
                        concurrentLinkedDeque.add(retFunc);
                        //第一次加入的时候，才需要发起请求,直接调用size会有性能问题
                        if (previousFun == null){
                            fromSupplier(supplier)
                                    .consumerValue(
                                            ret->{
                                                Callable<ConcurrentLinkedDeque<CallBack<Object>>> tagRemoveCallable = () -> tagToZipCallContext.remove(tag);
                                                ConcurrentLinkedDeque<CallBack<Object>> tagCallBacks = lockForZipCall.lockCallable(tag,tagRemoveCallable);
                                                tagCallBacks.forEach(callback -> {
                                                    if (log.isDebugEnabled()) {
                                                        log.debug("zipToBeforeCalling tag_{} call ret {}", tag, ret);
                                                    }
                                                    callback.onBack(ret);
                                                });
                                            }
                                    )
                                    .catchError(throwable -> {
                                        ConcurrentLinkedDeque<CallBack<Object>> tagCallBacks;
                                        // 如果不在同一个线程就需要再加一次锁，否则就不用枷锁
                                        if (callQueue != CallQueueMgr.getInstance().getLocalQueue()){
                                            tagCallBacks = lockForZipCall.lockCallable(tag,()->tagToZipCallContext.remove(tag));
                                        }else {
                                            tagCallBacks = tagToZipCallContext.remove(tag);
                                        }
                                        tagCallBacks.forEach(callback -> {
                                            callback.onError(throwable);
                                        });
                                    }).start();
                        }else {
                            if (log.isDebugEnabled()) {
                                log.debug("zipToBeforeCalling waiting to call tag_{} queue.size {}", tag,concurrentLinkedDeque.size());
                            }
                        }
                    });
                };
        return Homo.warp(consumer);
    }

    /**
     * 包装一个supplier
     * @param supplier 提供一个Homo
     * @return 返回一个Homo
     * @param <T> 返回值类型
     */
    public static <T> Homo<T> fromSupplier(SupplierWithException<Homo<T>> supplier) {
        Supplier<Homo<T>> wrapper = ()->{
            try {
                return supplier.get();
            } catch (Exception e) {
                return Homo.error(e);
            }
        };
        return Homo.warp(Mono.defer(wrapper));
    }

    public static void main(String[] args) {
        // 一个简单的发布者
        Publisher<Integer> publisher = subscriber -> {
            Subscription subscription = new Subscription() {
                private boolean canceled = false;
                private int count = 0;

                @Override
                public void request(long n) {
                    if (n <= 0) {
                        subscriber.onError(new IllegalArgumentException("Request must be positive!"));
                        return;
                    }
                    for (int i = 0; i < n && !canceled; i++) {
                        subscriber.onNext(count++);
                    }
                    if (count >= 5 && !canceled) {
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() {
                    canceled = true;
                }
            };

            subscriber.onSubscribe(subscription);
        };

        // 一个简单的订阅者
        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                System.out.println("Subscribed!");
                subscription.request(3); // 一开始请求 3 个数据项
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("Received: " + item);
                if (item == 2) {
                    subscription.request(5); // 再请求 5 个数据项
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Completed!");
            }
        };

        // 发布者订阅订阅者
        publisher.subscribe(subscriber);
    }
}
