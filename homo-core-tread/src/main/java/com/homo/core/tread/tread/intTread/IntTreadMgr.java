package com.homo.core.tread.tread.intTread;

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
 * Integer类型流程控制管理类
 *
 * @author dubian
 */
@Component
@Slf4j
public class IntTreadMgr extends AbstractTreadMgr<Integer> {
    @Autowired
    private TreadProperties treadProperties;

    @Override
    public boolean traceEnable() {
        return treadProperties.traceEnable;
    }

    @Override
    public BiPredicate<Integer, Integer> defaultAddCheckPredicate() {
        return IntCheckStrategy.Greater_Equal_ZERO;
    }

    @Override
    public BiPredicate<Integer, Integer> defaultSubCheckPredicate() {
        return IntCheckStrategy.Less_Equal.and(IntCheckStrategy.Greater_Equal_ZERO);
    }

    @Override
    protected FuncEx<Integer, Integer> beforeCheckApply() {
        return getValue -> getValue == null ? 0 : getValue;
    }

    @Override
    protected Func2Ex<Integer, Integer, Integer> subStrategy() {
        return (opValue, getValue) -> getValue - opValue;
    }

    @Override
    protected Func2Ex<Integer, Integer, Integer> addStrategy() {
        return Integer::sum;
    }

    @Override
    protected Func2Ex<Object, Integer, Homo<Integer>> setMethodWrapStrategy(Method method,String source) {
        return (object, opValue) -> {
            try {
                Object invoke = method.invoke(object, opValue);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return ((Homo) invoke).nextValue(ret -> ret instanceof Integer ? ret : null);
                } else if (method.getReturnType().isAssignableFrom(Integer.class)) {
                    return Homo.result((Integer) invoke);
                } else {
                    return Homo.result(null);
                }
            } catch (Exception e) {
                throw new TreadSetException(method.getName(),source, opValue.toString(), e);
            }
        };
    }


    @Override
    protected FuncEx<Object, Homo<Integer>> getMethodWrapStrategy(Method method,String source) {
        return object -> {
            try {
                Object invoke = method.invoke(object);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<Integer>) invoke;
                } else {
                    return Homo.result((Integer) invoke);
                }
            } catch (Exception e) {
                throw new TreadGetException(method.getName(),source, e);
            }
        };
    }

    @Override
    public void init() {
        baseTypeMap.put(Integer.class, Integer.class);
        baseTypeMap.put(int.class, Integer.class);
        init(Integer.class, treadProperties.scanPath);
        registerMgr(Integer.class, this);
        registerMgr(int.class, this);


    }
}
