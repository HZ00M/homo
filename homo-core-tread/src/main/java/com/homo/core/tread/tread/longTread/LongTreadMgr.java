package com.homo.core.tread.tread.longTread;

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
 * Long类型流程控制管理类
 *
 * @author dubian
 */
@Component
@Slf4j
public class LongTreadMgr extends AbstractTreadMgr<Long> {
    @Autowired
    private TreadProperties treadProperties;

    @Override
    public boolean traceEnable() {
        return treadProperties.traceEnable;
    }

    @Override
    public BiPredicate<Long, Long> defaultAddCheckPredicate() {
        return LongCheckStrategy.Greater_Equal_ZERO;
    }

    @Override
    public BiPredicate<Long, Long> defaultSubCheckPredicate() {
        return LongCheckStrategy.Greater_Equal_ZERO.and(LongCheckStrategy.Less_Equal);
    }

    @Override
    protected FuncEx<Long, Long> beforeCheckApply() {
        return getValue -> getValue == null ? 0 : getValue;
    }

    @Override
    protected Func2Ex<Long, Long, Long> subStrategy() {
        return (opValue, getValue) -> getValue - opValue;
    }

    @Override
    protected Func2Ex<Long, Long, Long> addStrategy() {
        return Long::sum;
    }

    @Override
    protected Func2Ex<Object, Long, Homo<Long>> setMethodWrapStrategy(Method method,String source) {
        return (object, opValue) -> {
            try {
                Object invoke = method.invoke(object, opValue);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return ((Homo) invoke).nextValue(ret -> ret instanceof Long ? ret : null);
                } else if (method.getReturnType().isAssignableFrom(Long.class)) {
                    return Homo.result((Long) invoke);
                } else {
                    return Homo.result(null);
                }
            } catch (Exception e) {
                throw new TreadSetException(method.getName(),source, opValue.toString(), e);
            }
        };
    }

    @Override
    protected FuncEx<Object, Homo<Long>> getMethodWrapStrategy(Method method,String source) {
        return object -> {
            try {
                Object invoke = method.invoke(object);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<Long>) invoke;
                } else {
                    return Homo.result((Long) invoke);
                }
            } catch (Exception e) {
                throw new TreadGetException(method.getName(),source, e);
            }
        };
    }

    @Override
    public void init() {
        baseTypeMap.put(Long.class, Long.class);
        baseTypeMap.put(long.class, Long.class);
        init(Long.class, treadProperties.scanPath);
        registerMgr(Long.class, this);
        registerMgr(long.class, this);
    }
}
