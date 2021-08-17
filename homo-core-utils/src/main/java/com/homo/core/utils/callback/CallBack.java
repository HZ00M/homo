package com.homo.core.utils.callback;

public interface CallBack<R> {

    void onBack(R r);

    void onError(Throwable t);
}
