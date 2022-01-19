package com.homo.core.utils.callback;

public interface CallBack<T> {

    void onBack(T value);

    void onError(Throwable throwable);
}
