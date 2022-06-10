package com.homo.core.facade.storege;

import com.homo.core.utils.callback.CallBack;

import java.util.Map;

public interface Dirty<T> {
    String key();

    Map<String,String> dirtyMap();

    CallBack<T> callBack();
}
