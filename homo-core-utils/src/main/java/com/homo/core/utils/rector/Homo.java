package com.homo.core.utils.rector;

import com.homo.core.utils.fun.ConsumerEx;
import com.homo.core.utils.fun.FuncEx;
import lombok.extern.log4j.Log4j2;
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
import java.util.function.*;

@Log4j2
public class Homo<T> extends Mono<T> {
    private final Mono<T> mono;

    public Homo(Mono<T> mono) {
        this.mono = mono;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        mono.subscribe(actual);
    }

    private static <T> Homo<T> warp(Mono<T> mono) {
        return new Homo(mono);
    }

    public static <T> Homo<T> warp(Supplier<Mono<T>> supplier) {
        return Homo.warp(supplier.get());
    }

    public static <T> Homo<T> warp(ConsumerEx<MonoSink<T>> callback) {
        Consumer<MonoSink<T>> consumer =
                sink -> {
                    try {
                        callback.accept(sink);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sink.error(e);
                    }
                };
        return Homo.warp(Mono.create(consumer));
    }

    public final <R> Homo<R> nextDo(FuncEx<? super T, ? extends Mono<? extends R>> transformer) {
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
        Function<? super T, ? extends R> warp = (Function<T, R>) t -> {
            if (t == Optional.empty()) {
                t = null;
            }
            return mapper.apply(t);
        };
        return Homo.warp(mono.map(warp));
    }

    public final Homo<T> consumerValue(Consumer<? super T> onSuccess) {
        Consumer<? super T> warp = (Consumer<T>) t -> {
            if (t != null) {
                if (t == Optional.empty()) {
                    t = null;
                }
                onSuccess.accept(t);
            }
        };
        return Homo.warp(mono.doOnSuccess(warp));
    }

    public final Homo<T> catchError(Consumer<? super Throwable> onError) {
        return Homo.warp(mono.doOnError(onError));
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

    public final Homo<T> onErrorContinue(FuncEx<? super Throwable, ? extends Mono<? extends T>> fallback) {
        Function<? super Throwable, ? extends Mono<? extends T>> warp = (t) -> {
            try {
                return fallback.apply(t);
            } catch (Throwable throwable) {
                return Mono.error(throwable);
            }
        };
        return warp(mono.onErrorResume(warp));
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
                                .doBeforeRetry(retrySignal -> log.warn("retry on the exception_{} starting , times_{}", retrySignal.failure(), retrySignal.totalRetries() + 1))
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
                                .doBeforeRetry(retrySignal -> log.warn("retry on the exception_{} starting , times_{}", retrySignal.failure(), retrySignal.totalRetries() + 1))
                )
        );
    }

}
