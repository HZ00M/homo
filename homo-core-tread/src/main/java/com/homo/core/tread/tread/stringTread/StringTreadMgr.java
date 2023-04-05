package com.homo.core.tread.tread.stringTread;

import com.homo.core.tread.tread.AbstractTreadMgr;
import com.homo.core.tread.tread.config.TreadProperties;
import com.homo.core.tread.tread.exception.TreadGetException;
import com.homo.core.tread.tread.exception.TreadSetException;
import com.homo.core.utils.fun.Func2Ex;
import com.homo.core.utils.fun.FuncEx;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.function.BiPredicate;

/**
 * String类型流程控制管理类
 *
 * @author dubian
 */
@Component
@Slf4j
public class StringTreadMgr extends AbstractTreadMgr<String> {
    @Autowired
    private TreadProperties treadProperties;

    @Override
    public boolean traceEnable() {
        return treadProperties.traceEnable;
    }

    @Override
    public BiPredicate<String, String> defaultAddCheckPredicate() {
        return (opValue, getValue) -> true;
    }

    @Override
    public BiPredicate<String, String> defaultSubCheckPredicate() {
        return (opValue, getValue) -> true;
    }

    @Override
    protected FuncEx<String, String> beforeCheckApply() {
        return getValue -> getValue;
    }

    @Override
    protected Func2Ex<String, String, String> subStrategy() {
        return (opValue, getValue) -> opValue;
    }

    @Override
    protected Func2Ex<String, String, String> addStrategy() {
        return (opValue, getValue) -> opValue;
    }

    @Override
    protected Func2Ex<Object, String, Homo<String>> setMethodWrapStrategy(Method method, String source) {
        return (object, opValue) -> {
            try {
                Object invoke = method.invoke(object, opValue);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return ((Homo) invoke).nextValue(ret -> ret instanceof String ? ret : null);
                } else if (method.getReturnType().isAssignableFrom(String.class)) {
                    return Homo.result((String) invoke);
                } else {
                    return Homo.result(null);
                }
            } catch (Exception e) {
                throw new TreadSetException(method.getName(),source, opValue, e);
            }
        };
    }

    @Override
    protected FuncEx<Object, Homo<String>> getMethodWrapStrategy(Method method,String source) {
        return object -> {
            try {
                Object invoke = method.invoke(object);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<String>) invoke;
                } else {
                    return Homo.result((String) invoke);
                }
            } catch (Exception e) {
                throw new TreadGetException(method.getName(),source, e);
            }
        };
    }

    @Override
    public void init() {
        baseTypeMap.put(String.class, String.class);
        init(String.class, treadProperties.scanPath);
        registerMgr(String.class, this);
    }
}
