/**
 * Created by 尼恩 at 疯狂创客圈
 */

package com.homo.concurrent.callback;


public interface CallbackTask<T> {

    T execute() throws Exception;

    void onBack(T t);

    void onError(Throwable t);
}
