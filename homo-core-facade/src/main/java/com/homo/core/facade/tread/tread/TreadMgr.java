package com.homo.core.facade.tread.tread;

import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.utils.fun.Func2PWithException;
import com.homo.core.utils.fun.Func3PWithException;
import com.homo.core.utils.fun.FuncWithException;
import com.homo.core.utils.rector.Homo;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程控制工具
 *
 * @author dubian
 */
public interface TreadMgr<T> {
    Tuple2<Boolean, String> checkOk = Tuples.of(true, "success");
    Tuple2<ExecRet, String> execOk = Tuples.of(ExecRet.ok, "success");
    Map<Class, TreadMgr> treadMgrMap = new HashMap<>();
    Map<Class, Class> baseTypeMap = new HashMap<>();

    default void registerMgr(Class<?> typeClass, TreadMgr treadMgr) {
        treadMgrMap.put(typeClass, treadMgr);
    }

    static <T> TreadMgr<T> getActualTreadMgr(Class<T> typeClass) {
        return treadMgrMap.getOrDefault(typeClass, null);
    }

    Homo<Tuple2<ExecRet, String>> doSeq(TreadContext<T> context);

    Homo<Tuple2<ExecRet, String>> exec(TreadContext<T> context);

    boolean registerPromiseSetFun(String source, Func2PWithException<Object, T, Homo<T>> setFun);

    boolean registerSetFun(String source, Func2PWithException<Object, T, T> setFun);

    boolean registerPromiseGetFun(String source, FuncWithException<Object, Homo<T>> getFun);

    boolean registerGetFun(String source, FuncWithException<Object, T> getFun);

    <P> boolean  registerCreateObjFun(Class<P> mgrClassType, Func2PWithException<P,Object, Object> getObjFun, String... sources);

    <P> boolean registerPromiseCreateObjFun(Class<P> mgrClassType, Func2PWithException<P,Object, Homo<Object>> getObjFun, String... sources);

    <P> boolean registerGetObjFun(Class<P> mgrClassType, Func2PWithException<P,Object, Object> getObjFun, String... sources);

    <P> boolean registerPromiseGetObjFun(Class<P> mgrClassType, Func2PWithException<P, Object,Homo<Object>> getObjFun, String... sources);

    <P> boolean registerSetObjFun(Class<P> mgrClassType, Func3PWithException<P,Object, Object, Object> setObjFun, String... sources);

    <P> boolean registerPromiseSetObjFun(Class<P> mgrClassType, Func3PWithException<P, Object,Object, Homo<Object>> setObjFun, String... sources);

    boolean containGet(String source);

    boolean containSet(String source);

    static String buildIdentity(Object target, String source) {
        String identity;
        if (targetIsId(target)) {
            identity = String.format("%s:%s",target,source);
        } else {
            identity = String.format("%s:%s",target.hashCode(),source);
        }
        return identity;
    }

    static boolean targetIsId(Object target) {
        if (target.getClass().isAssignableFrom(String.class) || target.getClass().isAssignableFrom(Integer.class) || target.getClass().isAssignableFrom(Long.class)) {
            return true;
        }
        return false;
    }
}
