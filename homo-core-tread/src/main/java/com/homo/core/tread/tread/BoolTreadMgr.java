package com.homo.core.tread.tread;

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
 * Boolean类型流程控制管理类
 *
 * @author dubian
 */
@Component
@Slf4j
public class BoolTreadMgr extends AbstractTreadMgr<Boolean> {
    @Autowired
    private TreadProperties treadProperties;

    @Override
    public boolean traceEnable() {
        return treadProperties.traceEnable;
    }

    @Override
    public BiPredicate<Boolean, Boolean> defaultAddCheckPredicate() {
        return (opValue, getValue) -> true;
    }

    @Override
    public BiPredicate<Boolean, Boolean> defaultSubCheckPredicate() {
        return (opValue, getValue) -> true;
    }

    @Override
    protected FuncEx<Boolean, Boolean> beforeCheckApply() {
        return getValue -> getValue;
    }

    @Override
    protected Func2Ex<Boolean, Boolean, Boolean> subStrategy() {
        return (opValue, getValue) -> opValue;
    }

    @Override
    protected Func2Ex<Boolean, Boolean, Boolean> addStrategy() {
        return (opValue, getValue) -> opValue;
    }

    @Override
    protected Func2Ex<Object, Boolean, Homo<Boolean>> setMethodWrapStrategy(Method method,String source) {
        return (object, opValue) -> {
            try {
                Object invoke = method.invoke(object, opValue);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return ((Homo) invoke).nextValue(ret -> ret instanceof Boolean ? ret : null);
                } else if (method.getReturnType().isAssignableFrom(Boolean.class)) {
                    return Homo.result((Boolean) invoke);
                } else {
                    return Homo.result(null);
                }
            } catch (Exception e) {
                throw new TreadSetException(method.getName(),source, opValue.toString(), e);
            }
        };
    }

    @Override
    protected FuncEx<Object, Homo<Boolean>> getMethodWrapStrategy(Method method,String source) {
        return object -> {
            try {
                Object invoke = method.invoke(object);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<Boolean>) invoke;
                } else {
                    return Homo.result((Boolean) invoke);
                }
            } catch (Exception e) {
                throw new TreadGetException(method.getName(),source, e);
            }
        };
    }

    @Override
    public void init() {
        baseTypeMap.put(Boolean.class, Boolean.class);
        baseTypeMap.put(boolean.class, Boolean.class);
        init(Boolean.class, treadProperties.scanPath);
        registerMgr(Boolean.class, this);
        registerMgr(boolean.class, this);
    }
}
