package com.homo.core.utils.callback;

public interface CallBack<T> {

    void onBack(T t);

    void onError(Throwable t);
}
