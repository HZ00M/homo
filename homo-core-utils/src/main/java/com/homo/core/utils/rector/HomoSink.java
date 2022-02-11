package com.homo.core.utils.rector;

import reactor.core.publisher.MonoSink;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class HomoSink<T> {
    MonoSink<T> monoSink;
    public HomoSink(MonoSink<T> monoSink){
        this.monoSink = monoSink;
    }

    public void success(@Nullable T v){
        if (v == null){
            monoSink.success((T) Optional.<T>empty());
        } else{
            monoSink.success(v);
        }
    }

    public void success(){
        monoSink.success();
    }

    public void error(Throwable e){
        monoSink.error(e);
    }
}
