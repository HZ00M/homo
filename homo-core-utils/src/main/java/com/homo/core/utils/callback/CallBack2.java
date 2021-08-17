package com.homo.core.utils.callback;

public interface CallBack2<T1, T2>{
    void onBack(T1 t1, T2 t2);

    void onError(Throwable t);
}
