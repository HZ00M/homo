package com.homo.core.tread.tread.objTread;


import com.homo.core.facade.tread.tread.TreadContext;
import com.homo.core.facade.tread.tread.TreadMgr;
import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.facade.tread.tread.enums.SeqType;
import com.homo.core.facade.tread.tread.op.SeqPoint;
import com.homo.core.tread.tread.config.TreadProperties;
import com.homo.core.utils.fun.Func2Ex;
import com.homo.core.utils.fun.Func3Ex;
import com.homo.core.utils.fun.FuncEx;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Iterator;


/**
 * 组合类型流程控制管理类，代理其他类型管理类
 *
 * @author dubian
 */
@Component
@Slf4j
public class ObjTreadMgr  implements TreadMgr<Object>, Module {
    @Autowired
    private TreadProperties treadProperties;

    @Override
    public Integer getOrder() {
        return 1;
    }

    public Homo<Tuple2<ExecRet, String>> exec(TreadContext<Object> context) {
        return doAllSeq(context, SeqType.SUB)
                .nextDo(tuple -> {
                    if (!tuple.equals(execOk)) {
                        return Homo.result(tuple);
                    }
                    return doAllSeq(context, SeqType.ADD);
                })
                .nextDo(tuple -> {
                    if (treadProperties.traceEnable) {
                        log.info("ObjTread exec ownerId {} info {}", context.getOwnerId(), context.getRecords());
                    }
                    return Homo.result(tuple);
                });
    }

    private Homo<Tuple2<ExecRet, String>> doAllSeq(TreadContext<Object> context, SeqType seqType) {
        Iterator<SeqPoint<Object>> iterator = seqType == SeqType.ADD ? context.getAddIterator() : context.getSubIterator();
        if (iterator.hasNext()) {
            SeqPoint<Object> seqPoint = iterator.next();
            context.setProcessing(seqPoint);
            return doSeq(context).nextDo(tuple -> {
                if (tuple.equals(execOk)) {
                    return doAllSeq(context, seqType);
                }
                return Homo.result(tuple);
            }).onErrorContinue(throwable -> {
                ExecRet execRet = seqPoint.type.equals(SeqType.ADD) ? ExecRet.addCheckError : ExecRet.subCheckError;
                log.error("doAllSeq ownerId {} source {} opType {} ret {} error throwable", context.getOwnerId(), seqPoint.methodName, seqPoint.type, execRet, throwable);
                return Homo.result(Tuples.of(execRet, throwable.getMessage()));
            });
        } else {
            return Homo.result(execOk);
        }
    }

    public Homo<Tuple2<ExecRet, String>> doSeq(TreadContext context) {
        try {
            SeqPoint seqPoint = context.getProcessing();
            Class<?> typeClass = seqPoint.getParam().getClass();
            return treadMgrMap.get(typeClass).doSeq(context);
        } catch (Exception exception) {
            return Homo.error(exception);
        }
    }

    public boolean registerPromiseSetFun(String source, Func2Ex<Object, Object, Homo<Object>> setFun) {
        throw new RuntimeException("ObjTreadMgr unSupport registerPromiseSetFun!");
    }

    public boolean registerSetFun(String source, Func2Ex<Object, Object, Object> setFun) {
        throw new RuntimeException("ObjTreadMgr unSupport registerSetFun!");
    }

    public boolean registerPromiseGetFun(String source, FuncEx<Object, Homo<Object>> getFun) {
        throw new RuntimeException("ObjTreadMgr unSupport registerPromiseGetFun!");
    }

    public boolean registerGetFun(String source, FuncEx<Object, Object> getFun) {
        throw new RuntimeException("ObjTreadMgr unSupport registerGetFun!");
    }

    @Override
    public <P> boolean registerCreateObjFun(Class<P> mgrClassType,Func2Ex<P, Object, Object> getObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerGetFun!");
    }

    @Override
    public <P> boolean registerPromiseCreateObjFun(Class<P> mgrClassType,Func2Ex<P, Object, Homo<Object>> getObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerPromiseCreateObjFun!");
    }

    @Override
    public <P> boolean registerGetObjFun(Class<P> mgrClassType,Func2Ex<P, Object, Object> getObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerGetObjFun!");
    }

    @Override
    public <P> boolean registerPromiseGetObjFun(Class<P> mgrClassType,Func2Ex<P, Object, Homo<Object>> getObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerPromiseGetObjFun!");
    }

    @Override
    public <P> boolean registerSetObjFun(Class<P> mgrClassType, Func3Ex<P, Object, Object, Object> setObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerSetObjFun!");
    }

    @Override
    public <P> boolean registerPromiseSetObjFun(Class<P> mgrClassType, Func3Ex<P, Object, Object, Homo<Object>> setObjFun, String... sources) {
        throw new RuntimeException("ObjTreadMgr unSupport registerPromiseSetObjFun!");
    }

    public boolean containGet(String source) {
        return false;
    }

    public boolean containSet(String source) {
        return false;
    }

    @Override
    public void moduleInit() {
        registerMgr(Object.class, this);
    }
}

