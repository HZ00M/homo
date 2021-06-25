package com.homo.utils;

import com.homo.utils.fun.FuncEx;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Homo<T> extends Mono<T> {
    private final Mono<T> mono;

    public Homo(Mono<T> mono){
        this.mono = mono;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        mono.subscribe(actual);
    }

    private  static <T> Homo<T> warp(Mono<T> mono){
        return new Homo(mono);
    }
    public static <T> Homo<T> warp(Supplier<Mono<T>> supplier){
        return Homo.warp(supplier.get());
    }
    public final <R> Homo<R> nextDo(FuncEx<? super T, ? extends Mono<? extends R>> transformer){
        Function<? super T, ? extends Mono<? extends R>> warp = (Function<T, Mono<? extends R>>) origin -> {
            if (origin == Optional.empty()){
                origin = null;
            }
            try {
                return transformer.apply(origin);
            } catch (Exception e){
                return Mono.error(e);
            }
        };
        return Homo.warp(mono.flatMap(warp));
    }
    public final <R> Homo<R> nextValue(Function<? super T, ? extends R> mapper) {
        Function<? super T, ? extends R> warp = (Function<T, R>) t -> {
            if (t == Optional.empty()){
                t = null;
            }
            return mapper.apply(t);
        };
        return Homo.warp(mono.map(warp));
    }
    public final Homo<T> consumerValue(Consumer<? super T> onSuccess) {
        Consumer<? super T> warp = (Consumer<T>) t -> {
            if (t!= null){
                if (t == Optional.empty()){
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
    public static <T> Homo<T> result(T data){
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

    public static <T1, T2, T3, T4, T5, T6,T7> Homo<Tuple7<T1, T2, T3, T4, T5, T6,T7>> all(Mono<? extends T1> p1,
                                                                                                Mono<? extends T2> p2,
                                                                                                Mono<? extends T3> p3,
                                                                                                Mono<? extends T4> p4,
                                                                                                Mono<? extends T5> p5,
                                                                                                Mono<? extends T6> p6,
                                                                                                Mono<? extends T7> p7) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5, p6,p7));
    }

    public static <T1, T2, T3, T4, T5, T6,T7,T8> Homo<Tuple8<T1, T2, T3, T4, T5, T6,T7,T8>> all(Mono<? extends T1> p1,
                                                                                                      Mono<? extends T2> p2,
                                                                                                      Mono<? extends T3> p3,
                                                                                                      Mono<? extends T4> p4,
                                                                                                      Mono<? extends T5> p5,
                                                                                                      Mono<? extends T6> p6,
                                                                                                      Mono<? extends T7> p7,
                                                                                                      Mono<? extends T8> p8) {
        return Homo.warp(Mono.zip(p1, p2, p3, p4, p5, p6,p7,p8));
    }

    public static <R> Homo<R> all(final Iterable<? extends Homo<?>> monos, Function<? super Object[], ? extends R> combinator) {
        return Homo.warp(Mono.zip(monos, combinator));
    }
}
