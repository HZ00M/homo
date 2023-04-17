package com.homo.core.tread.tread;

import com.homo.core.common.module.Module;
import com.homo.core.facade.tread.tread.TreadContext;
import com.homo.core.facade.tread.tread.TreadMgr;
import com.homo.core.facade.tread.tread.annotation.*;
import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.facade.tread.tread.enums.SeqType;
import com.homo.core.facade.tread.tread.op.SeqPoint;
import com.homo.core.tread.tread.exception.*;
import com.homo.core.utils.fun.Func2Ex;
import com.homo.core.utils.fun.Func3Ex;
import com.homo.core.utils.fun.FuncEx;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 流程管理抽象类 负责流程控制主逻辑
 *
 * @author dubian
 */
@Log4j2
public abstract class AbstractTreadMgr<T>  implements TreadMgr<T>,Module {

    protected Map<String, Func2Ex<Object, T, Homo<T>>> setFuncMap = new HashMap<>();
    protected Map<String, FuncEx<Object, Homo<T>>> getFunMap = new HashMap<>();
    protected Map<String, Func2Ex<Object, Object, Homo<Object>>> createObjFunMap = new HashMap<>();
    protected Map<String, Func3Ex<Object, Object, Object, Homo<Object>>> setObjFunMap = new HashMap<>();
    protected Map<String, Func2Ex<Object, Object, Homo<Object>>> getObjFunMap = new HashMap<>();
    protected Map<String, Class<?>> sourceNameToCreateClassTypeMap = new HashMap<>();
    protected Map<String, Class<?>> sourceNameToGetClassTypeMap = new HashMap<>();
    protected Map<String, Class<?>> sourceNameToSetClassTypeMap = new HashMap<>();

    public abstract boolean traceEnable();

    public abstract BiPredicate<T, T> defaultAddCheckPredicate();

    public abstract BiPredicate<T, T> defaultSubCheckPredicate();

    protected abstract FuncEx<T, T> beforeCheckApply();

    protected abstract Func2Ex<T, T, T> subStrategy();

    protected abstract Func2Ex<T, T, T> addStrategy();

    protected abstract Func2Ex<Object, T, Homo<T>> setMethodWrapStrategy(Method method, String source);

    protected abstract FuncEx<Object, Homo<T>> getMethodWrapStrategy(Method method, String source);

    public void init(Class<T> scanType, String scanPath) {
        scanCreateObjMethod(scanPath);
        scanGetObjMethod(scanPath);
        scanSetObjMethod(scanPath);
        scanGetMethod(scanType, scanPath);
        scanSetMethod(scanType, scanPath);
    }

    @Override
    public Integer getOrder() {
        return 1;
    }

    @Override
    public Homo<Tuple2<ExecRet, String>> exec(TreadContext<T> context) {
        if (log.isTraceEnabled()) {
            log.trace("exec ownerId {} subSize {} addSize {}", context.getOwnerId(), context.getSubs().size(), context.getAdds().size());
        }
        return doAllSeq(context, SeqType.SUB)
                .nextDo(tuple -> {
                    if (!tuple.equals(execOk)) {
                        return Homo.result(tuple);
                    }
                    return doAllSeq(context, SeqType.ADD);
                })
                .nextDo(tuple -> {
                    if (traceEnable()) {
                        log.info("Tread exec ownerId {} info {}", context.getOwnerId(), context.getRecords());
                    }
                    return Homo.result(tuple);
                })
                .catchError(throwable -> Homo.result(Tuples.of(ExecRet.sysError, throwable.getMessage())));
    }

    private Homo<Tuple2<ExecRet, String>> doAllSeq(TreadContext<T> context, SeqType seqType) {
        Iterator<SeqPoint<T>> iterator = seqType == SeqType.ADD ? context.getAddIterator() : context.getSubIterator();
        if (iterator.hasNext()) {
            SeqPoint<T> seqPoint = iterator.next();
            context.setProcessing(seqPoint);
            return doSeq(context).nextDo(tuple -> {
                if (tuple.equals(execOk)) {
                    return doAllSeq(context, seqType);
                }
                return Homo.result(tuple);
            }).onErrorContinue(throwable -> {
                ExecRet execRet = seqType.equals(SeqType.ADD) ? ExecRet.addError : ExecRet.subError;
                if (traceEnable()) {
                    context.record(seqPoint.getMethodName(), seqType, "[]", seqPoint.getParam(), "[]", execRet);
                }
                log.error("doAllSeq ownerId {} source {} opType {} ret {} error throwable", context.getOwnerId(), seqPoint.methodName, seqPoint.type, execRet, throwable);
                return Homo.result(Tuples.of(execRet, throwable.getMessage()));
            });
        } else {
            return Homo.result(execOk);
        }
    }

    @Override
    public Homo<Tuple2<ExecRet, String>> doSeq(TreadContext<T> context) {
        SeqPoint<T> seqPoint = context.getProcessing();
        Map<Object, Object> mgrObjMap = context.mgrObjMap;
        Object sourceObj = seqPoint.getObject();
        String source = seqPoint.getMethodName();
        T opValue = seqPoint.getParam();
        SeqType type = seqPoint.getType();
        BiPredicate<T, T> customerCheckPredicate = seqPoint.getCheckPredicate();
        Consumer<T> resultConsumer = seqPoint.getResultConsumer();
        Supplier<Object> targetSupplier = seqPoint.targetSupplier;
        boolean opByIdSeq = false;
        String opObjId = "sourceObj";
        Object identity = null;
        if (TreadMgr.targetIsId(sourceObj)) {
            opByIdSeq = true;
            opObjId = sourceObj.toString();
            identity = TreadMgr.buildIdentity(sourceObj, source);
        }
        Object getMgrObj = null;
        Object createMgrObj = null;
        Object setMgrObj = null;
        if (opByIdSeq) {
            Class<?> getClass = sourceNameToGetClassTypeMap.get(source);
            Class<?> createClass = sourceNameToCreateClassTypeMap.get(source);
            Class<?> setClass = sourceNameToSetClassTypeMap.get(source);
            if (mgrObjMap.isEmpty()) {
                return Homo.result(Tuples.of(ExecRet.sysError, source + type + " exec by id must declare mgrObj!"));
            }
            if (!getObjFunMap.containsKey(source)) {
                return Homo.result(Tuples.of(ExecRet.sysError, source + " annotation declare miss getObjFun define!"));
            }
            if (!createObjFunMap.containsKey(source)) {
                return Homo.result(Tuples.of(ExecRet.sysError, source + " annotation declare miss createObjFun define!"));
            }
            if ( !setObjFunMap.containsKey(source)) {
                return Homo.result(Tuples.of(ExecRet.sysError, source + " annotation declare miss setObjFun define!"));
            }
            if (mgrObjMap.containsKey(identity)) {
                Class<?> mgrClass = mgrObjMap.get(identity).getClass();
                if (!getClass.isAssignableFrom(mgrClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " exec by id mgrObj must declare getObjMethod!"));
                }
                if (!createClass.isAssignableFrom(mgrClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " exec by id mgrObj must declare createObjMethod!"));
                }
                if (!setClass.isAssignableFrom(mgrClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " exec by id mgrObj must declare setObjMethod!"));
                }
                getMgrObj = mgrObjMap.get(identity);
                createMgrObj = mgrObjMap.get(identity);
                setMgrObj = mgrObjMap.get(identity);
            } else {
                if (!mgrObjMap.containsKey(getClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " declare mgrObj miss createObjMethod define!"));
                }
                if (!mgrObjMap.containsKey(createClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " declare mgrObj getObjMethod define!"));
                }
                if (!mgrObjMap.containsKey(setClass)) {
                    return Homo.result(Tuples.of(ExecRet.sysError, source + " declare mgrObj setObjMethod define!"));
                }
                getMgrObj = mgrObjMap.get(getClass);
                createMgrObj = mgrObjMap.get(createClass);
                setMgrObj = mgrObjMap.get(setClass);
            }
            if (getMgrObj == null || createMgrObj == null || setMgrObj == null) {
                return Homo.result(Tuples.of(ExecRet.sysError, source + "getMgrObj or createMgrObj or setMgrObj is null!"));
            }
        }

        if (!getFunMap.containsKey(source)) {
            return Homo.result(Tuples.of(ExecRet.sysError, source + " annotation declare miss getMethod define!"));
        }
        if (!setFuncMap.containsKey(source)) {
            return Homo.result(Tuples.of(ExecRet.sysError, source + " annotation declare miss setMethod define!"));
        }
        try {
            BiPredicate<T, T> defaultCheckPredicate = type.equals(SeqType.ADD) ? defaultAddCheckPredicate() : defaultSubCheckPredicate();
            BiPredicate<T, T> checkPredicate = customerCheckPredicate != null ? customerCheckPredicate.and(defaultCheckPredicate) : defaultCheckPredicate;
            boolean opById = opByIdSeq;
            String printId = opObjId;
            Object finalGetMgrObj = getMgrObj;
            Object finalCreateMgrObj = createMgrObj;
            Object finalSetMgrObj = setMgrObj;
            Object finalIdentity = identity;
            return Homo.result(1)
                    .nextDo(ret -> {
                        if (opById) {
                            return getObjFunMap.get(source).apply(finalGetMgrObj, sourceObj)
                                    .nextDo(getObj -> {
                                        if (getObj == null) {
                                            if (type.equals(SeqType.SUB)) {
                                                //如果是扣除直接失败
                                                return Homo.error(new TreadGetObjException(sourceObj, source, "sub obj is null"));
                                            } else {
                                                //如果是添加则尝试去创建对象
                                                return Homo.result(targetSupplier != null)
                                                        .nextDo(haveSeqSupplier -> {
                                                            if (haveSeqSupplier) {
                                                                //当前seq的supplier最高优先级
                                                                return Homo.result(targetSupplier.get());
                                                            } else if (context.containCreateObjFun(finalIdentity)) {
                                                                //当前context的supplier次优先级
                                                                return Homo.result(context.getCreateObjFun(finalIdentity).apply(sourceObj));
                                                            } else if (createObjFunMap.containsKey(source)) {
                                                                //基于注解的supplier最低优先级
                                                                return createObjFunMap.get(source).apply(finalCreateMgrObj, sourceObj);
                                                            } else {
                                                                return Homo.result(null);
                                                            }
                                                        })
                                                        .nextDo(createObj -> {
                                                            if (createObj == null) {
                                                                return Homo.error(new TreadCreateObjException(sourceObj, source, "create obj is null"));
                                                            }
                                                            return Homo.result(createObj);
                                                        });
                                            }
                                        } else {
                                            return Homo.result(getObj);
                                        }
                                    });
                        } else {
                            return Homo.result(sourceObj);
                        }
                    })
                    .nextDo(opObj -> {
                        return getFunMap.get(source).apply(opObj)
                                .nextDo(getValue -> {
                                    T newGetValue = beforeCheckApply().apply(getValue);
                                    if (checkPredicate.test(opValue, newGetValue)) {
                                        T newValue = type.equals(SeqType.ADD) ? addStrategy().apply(opValue, newGetValue) : subStrategy().apply(opValue, getValue);
                                        return setFuncMap.get(source).apply(opObj, newValue)
                                                .nextDo(opResult -> {
                                                    if (opById) {
                                                        return setObjFunMap.get(source).apply(finalSetMgrObj, sourceObj, opObj)
                                                                .nextValue(setRet -> TreadMgr.execOk)
                                                                ;
                                                    }
                                                    return Homo.result(TreadMgr.execOk);
                                                })
                                                .consumerValue(ret -> {
                                                    if (log.isTraceEnabled()) {
                                                        log.trace("doSeq ownerId {} opObjId {} type {} source {} opValue {} newValue {}", context.getOwnerId(), printId, type, source, newGetValue, newValue);
                                                    }
                                                    if (traceEnable()) {
                                                        context.record(source, printId, type, newGetValue, opValue, newValue, ret);
                                                    }
                                                    if (resultConsumer != null) {
                                                        try {
                                                            resultConsumer.accept(newValue);
                                                        } catch (Exception e) {
                                                            log.error("opObjId {} source {} SeqType {} opValue {} resultConsumer error!", printId, source, type, opValue, e);
                                                        }
                                                    }
                                                });
                                    } else {
                                        Tuple2<ExecRet, String> checkFail = type.equals(SeqType.ADD) ?
                                                Tuples.of(ExecRet.addCheckFail, String.format("add check fail ownerId %s opObjId %s source %s opType %s opValue %s getValue %s", context.getOwnerId(), printId, source, type, opValue, getValue)) :
                                                Tuples.of(ExecRet.subCheckFail, String.format("sub check fail ownerId %s opObjId %s source %s opType %s opValue %s getValue %s", context.getOwnerId(), printId, source, type, opValue, getValue));
                                        if (traceEnable()) {
                                            context.record(source, printId, type, newGetValue, opValue, newGetValue, checkFail);
                                        }
                                        return Homo.result(checkFail);
                                    }
                                });
                    })
                    .onErrorContinue(throwable -> {
                        ExecRet execRet;
                        if (throwable instanceof TreadGetObjException) {
                            execRet = ExecRet.getObjError;
                        } else if (throwable instanceof TreadCreateObjException) {
                            execRet = ExecRet.createObjError;
                        } else if (throwable instanceof TreadSetObjException) {
                            execRet = ExecRet.setObjError;
                        } else if (throwable instanceof TreadGetException) {
                            execRet = type.equals(SeqType.ADD) ? ExecRet.addGetError : ExecRet.subGetError;
                        } else if (throwable instanceof TreadSetException) {
                            execRet = type.equals(SeqType.ADD) ? ExecRet.addSetError : ExecRet.subSetError;
                        } else {
                            execRet = type.equals(SeqType.ADD) ? ExecRet.addError : ExecRet.subError;
                        }
                        if (traceEnable()) {
                            context.record(source, printId, type, "[]", opValue, "[]", execRet, throwable.getMessage());
                        }
                        log.error("doSeq error ownerId {} opObjId {} source {} opType {} opValue {} ret {} throwable", context.getOwnerId(), printId, source, type, opValue, execRet, throwable);
                        return Homo.result(Tuples.of(execRet, throwable.getMessage()));
                    });
        } catch (Exception exception) {
            log.error("doSeq error ownerId {} opObjId {} source {} opValue {} type {} throwable ", context.getOwnerId(), opObjId, source, opValue, type, exception);
            return Homo.error(exception);
        }
    }

    public <T> void scanSetMethod(Class<T> scanType, String scanPath) {
        log.info("Tread scanSetMethod start packagePath {} scanType {}", scanPath, scanType);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> setMethods = reflections.getMethodsAnnotatedWith(SetMethod.class);
        if (setMethods != null) {
            setMethods.forEach(method -> {
                SetMethod setMethod = method.getAnnotation(SetMethod.class);
                String[] sources = setMethod.value();
                for (String source : sources) {
                    if (setFuncMap.containsKey(source)) {
                        log.error("Tread scanSetMethod find conflict method {}, please rename the method name!", source);
                        System.exit(-1);
                    }
                    Parameter[] parameters = method.getParameters();
                    if (parameters.length != 1 || !baseTypeMap.containsKey(parameters[0].getType()) || !baseTypeMap.get(parameters[0].getType()).equals(scanType)) {
                        return;
                    }
                    method.setAccessible(true);
                    setFuncMap.put(source, setMethodWrapStrategy(method, source));
                }
            });
        }
        log.info("Tread scanSetMethod finish setFuncMap {}", setFuncMap);
    }

    public <T> void scanGetMethod(Class<T> scanType, String scanPath) {
        log.info("Tread scanGetMethod start packagePath {} scanType {}", scanPath, scanType);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> getMethods = reflections.getMethodsAnnotatedWith(GetMethod.class);
        if (getMethods != null) {
            getMethods.forEach(method -> {
                GetMethod getMethod = method.getAnnotation(GetMethod.class);
                String[] sources = getMethod.value();
                for (String source : sources) {
                    if (getFunMap.containsKey(source)) {
                        log.error("Tread scanGetMethod find conflict method {} scanType {}, please rename the method name!", source, scanType);
                        System.exit(-1);
                    }
                    Class<?> returnType = method.getReturnType();
                    if ((!baseTypeMap.containsKey(returnType) || !baseTypeMap.get(returnType).equals(scanType)) && !returnType.equals(Homo.class)) {
                        return;
                    }
                    if (returnType.isAssignableFrom(Homo.class)) {
                        try {
                            Class parameterizedType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                            if (!parameterizedType.isAssignableFrom(scanType)) {
                                return;
                            }
                        } catch (Exception e) {
                            log.error("Tread scanGetMethod error method {} scanType {}, please check return type!", source, scanType);
                        }

                    }
                    method.setAccessible(true);
                    getFunMap.put(source, getMethodWrapStrategy(method, source));
                }
            });
        }
        log.info("Tread scanType {}scanGetMethod finish getFunMap {}", scanType, getFunMap);
    }

    public void scanCreateObjMethod(String scanPath) {
        log.info("Tread scanCreateObjMethod start packagePath {}", scanPath);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> createObjMethods = reflections.getMethodsAnnotatedWith(CreateObjMethod.class);
        if (createObjMethods != null) {
            createObjMethods.forEach(method -> {
                CreateObjMethod createMethod = method.getAnnotation(CreateObjMethod.class);
                for (String source : createMethod.value()) {
                    if (createObjFunMap.containsKey(source)) {
                        log.error("Tread scanCreateObjMethod find conflict method {} , please rename the method name!", source);
                        System.exit(-1);
                    }
                    Class<?> returnType = method.getReturnType();
                    Class<?> requireType = createMethod.type();
                    if (!requireType.isAssignableFrom(returnType)) {
                        log.error("source {} CreateObjMethod miss cause require type is {} provide type is {}", source, requireType.getSimpleName(), returnType.getSimpleName());
                        System.exit(-1);
                        return;
                    }
                    method.setAccessible(true);
                    createObjFunMap.put(source, wrapCreateObjMethod(method, source));
                    sourceNameToCreateClassTypeMap.put(source, method.getDeclaringClass());
                }
            });
        }
        log.info("Tread scanCreateObjMethod finish createObjFunMap {}", createObjFunMap);
    }

    public void scanSetObjMethod(String scanPath) {
        log.info("Tread scanSetObjMethod start packagePath {} ", scanPath);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> setObjMethods = reflections.getMethodsAnnotatedWith(SetObjMethod.class);
        if (setObjMethods != null) {
            setObjMethods.forEach(method -> {
                SetObjMethod setMethod = method.getAnnotation(SetObjMethod.class);
                for (String source : setMethod.value()) {
                    if (setObjFunMap.containsKey(source)) {
                        log.error("Tread scanSetObjMethod find conflict method {}, please rename the method name!", source);
                        System.exit(-1);
                    }
                    int parameterCount = method.getParameterCount();
                    if (parameterCount <= 0 || parameterCount > 2) {
                        log.error("source {} SetObjMethod miss cause function require setObjFun(id,setObj) or setObjFun(setObj), please check wrong method {}!", source, method.getName());
                        System.exit(-1);
                        return;
                    }
                    Class<?> provideType;
                    if (parameterCount == 1) {
                        provideType = method.getParameterTypes()[0];
                    } else {
                        provideType = method.getParameterTypes()[1];
                    }
                    Class<?> requireType = setMethod.type();
                    if (!requireType.isAssignableFrom(provideType)) {
                        log.error("source {} CreateObjMethod miss cause require type is {} but provide type is {}", source, requireType.getSimpleName(), provideType.getSimpleName());
                        System.exit(-1);
                        return;
                    }
                    method.setAccessible(true);
                    setObjFunMap.put(source, wrapSetObjMethod(method, source));
                    sourceNameToSetClassTypeMap.put(source, method.getDeclaringClass());
                }
            });
        }
        log.info("Tread scanSetObjMethod finish setObjFuncMap {}", setObjFunMap);
    }

    public void scanGetObjMethod(String scanPath) {
        log.info("Tread scanGetObjMethod start packagePath {}", scanPath);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> getObjMethods = reflections.getMethodsAnnotatedWith(GetObjMethod.class);
        if (getObjMethods != null) {
            getObjMethods.forEach(method -> {
                GetObjMethod getObjMethod = method.getAnnotation(GetObjMethod.class);
                Class<?> requireType = getObjMethod.type();
                for (String source : getObjMethod.value()) {
                    if (getObjMethods.contains(source)) {
                        log.error("Tread scanGetObjMethod find conflict method {} , please rename the method name!", source);
                        System.exit(-1);
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(Homo.class)) {
                        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                        Class<?> realClassType = (Class) parameterizedType.getRawType();
                        if (!realClassType.isAssignableFrom(requireType)) {
                            log.error("source {} GetObjMethod miss cause require type is {} provide type is {}", source, requireType.getSimpleName(), realClassType.getSimpleName());
                            System.exit(-1);
                            return;
                        }
                    } else {
                        if (!requireType.isAssignableFrom(returnType)) {
                            log.error("source {} GetObjMethod miss cause require type is {} provide type is {}", source, requireType.getSimpleName(), returnType.getSimpleName());
                            System.exit(-1);
                            return;
                        }
                    }
                    method.setAccessible(true);
                    getObjFunMap.put(source, wrapGetObjMethod(method, source));
                    sourceNameToGetClassTypeMap.put(source, method.getDeclaringClass());
                }
            });
        }
        log.info("Tread scanGetObjMethod finish getObjFunMap {}", getObjFunMap);
    }

    protected static Func3Ex<Object, Object, Object, Homo<Object>> wrapSetObjMethod(Method method, String source) {
        return (mgr, identity, setObj) -> {
            try {
                Object invoke;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (method.getParameterCount() == 1) {
                    Class<?> parameterType = parameterTypes[0];
                    if (!setObj.getClass().isAssignableFrom(parameterType)) {
                        throw new RuntimeException(String.format("parameterCount type not match require %s provide %s", parameterType, setObj.getClass()));
                    }
                    invoke = method.invoke(mgr, setObj);
                } else if (method.getParameterCount() == 2) {
                    Class<?> idParameterType = parameterTypes[0];
                    Class<?> objParameterType = parameterTypes[1];
                    if (!identity.getClass().isAssignableFrom(idParameterType) || !setObj.getClass().isAssignableFrom(objParameterType)) {
                        throw new RuntimeException(String.format("parameter type not match require (%s,%s) provide (%s,%s)",
                                idParameterType.getSimpleName(), objParameterType.getSimpleName(), identity.getClass().getSimpleName(), setObj.getClass().getSimpleName()));
                    }
                    invoke = method.invoke(mgr, identity, setObj);
                } else {
                    throw new RuntimeException("parameter count not match, only support (setObj) or (id,setObj) ");
                }
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return ((Homo) invoke);
                } else if (method.getReturnType().isAssignableFrom(Object.class)) {
                    return Homo.result(invoke);
                } else {
                    return Homo.result(null);
                }
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    throw new TreadSetObjException(method.getName(), source, ((InvocationTargetException) e).getTargetException());
                } else {
                    throw new TreadSetObjException(method.getName(), source, e);
                }
            }
        };
    }

    protected static Func2Ex<Object, Object, Homo<Object>> wrapCreateObjMethod(Method method, String source) {
        return (mgrObj, identity) -> {
            try {
                Object invoke = method.invoke(mgrObj, identity);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<Object>) invoke;
                } else {
                    return Homo.result(invoke);
                }
            } catch (InvocationTargetException e) {
                throw new TreadCreateObjException(method.getName(), source, e.getTargetException());
            }
        };
    }

    protected static Func2Ex<Object, Object, Homo<Object>> wrapGetObjMethod(Method method, String source) {
        return (mgrObj, identity) -> {
            try {
                Object invoke = method.invoke(mgrObj, identity);
                if (method.getReturnType().isAssignableFrom(Homo.class)) {
                    return (Homo<Object>) invoke;
                } else {
                    return Homo.result(invoke);
                }
            } catch (InvocationTargetException e) {
                throw new TreadGetObjException(method.getName(), source, e.getTargetException());
            }
        };
    }

    @Override
    public boolean registerSetFun(String source, Func2Ex<Object, T, T> setFun) {
        Func2Ex<Object, T, Homo<T>> wrapSetFun = (o, addCount) -> Homo.result(setFun.apply(o, addCount));
        return registerPromiseSetFun(source, wrapSetFun);
    }

    @Override
    public boolean registerPromiseSetFun(String source, Func2Ex<Object, T, Homo<T>> setFun) {
        if (setFuncMap.containsKey(source)) {
            log.error("registerPromiseSetFun find conflict source {}, please redefine source!", source);
            return false;
        }
        setFuncMap.put(source, setFun);
        return true;
    }

    @Override
    public boolean registerGetFun(String source, FuncEx<Object, T> getFun) {
        FuncEx<Object, Homo<T>> wrapGetFun = (obj) -> Homo.result(getFun.apply(obj));
        return registerPromiseGetFun(source, wrapGetFun);
    }

    @Override
    public boolean registerPromiseGetFun(String source, FuncEx<Object, Homo<T>> getFun) {
        if (getFunMap.containsKey(source)) {
            log.error("registerPromiseGetFun find conflict source {}, please redefine source!", source);
            return false;
        }
        getFunMap.put(source, getFun);
        return true;
    }

    @Override
    public <P> boolean registerCreateObjFun(Class<P> mgrClassType, Func2Ex<P, Object, Object> createObjFun, String... sources) {
        Func2Ex<P, Object, Homo<Object>> warpCreateFun = (mgrObj, id) -> Homo.result(createObjFun.apply(mgrObj, id));
        return registerPromiseCreateObjFun(mgrClassType, warpCreateFun, sources);
    }

    @Override
    public <P> boolean registerPromiseCreateObjFun(Class<P> mgrClassType, Func2Ex<P, Object, Homo<Object>> getObjFun, String... sources) {
        for (String source : sources) {
            if (createObjFunMap.containsKey(source)) {
                log.error("registerPromiseCreateObjFun find conflict source {}, please redefine source!", source);
                return false;
            }
            sourceNameToCreateClassTypeMap.put(source, mgrClassType);
            createObjFunMap.put(source, (Func2Ex<Object, Object, Homo<Object>>) getObjFun);
        }
        return true;
    }

    @Override
    public <P> boolean registerGetObjFun(Class<P> mgrClassType, Func2Ex<P, Object, Object> getObjFun, String... sources) {
        Func2Ex<P, Object, Homo<Object>> wrapGetObjFun = (mgrObj, id) -> Homo.result(getObjFun.apply(mgrObj, id));
        return registerPromiseGetObjFun(mgrClassType, wrapGetObjFun, sources);
    }

    @Override
    public <P> boolean registerPromiseGetObjFun(Class<P> mgrClassType, Func2Ex<P, Object, Homo<Object>> getObjFun, String... sources) {
        for (String source : sources) {
            if (getObjFunMap.containsKey(source)) {
                log.error("registerPromiseGetObjFun find conflict source {}, please redefine source!", source);
                return false;
            }
            sourceNameToGetClassTypeMap.put(source, mgrClassType);
            getObjFunMap.put(source, (Func2Ex<Object, Object, Homo<Object>>) getObjFun);
        }
        return true;
    }

    @Override
    public <P> boolean registerSetObjFun(Class<P> mgrClassType, Func3Ex<P, Object, Object, Object> setObjFun, String... sources) {
        Func3Ex<P, Object, Object, Homo<Object>> wrapSetObjFun = (mgrObj, id, setObj) -> Homo.result(setObjFun.apply(mgrObj, id, setObj));
        return registerPromiseSetObjFun(mgrClassType, wrapSetObjFun, sources);
    }

    @Override
    public <P> boolean registerPromiseSetObjFun(Class<P> mgrClassType, Func3Ex<P, Object, Object, Homo<Object>> setObjFun, String... sources) {
        for (String source : sources) {
            if (setObjFunMap.containsKey(source)) {
                log.error("registerPromiseSetObjFun find conflict source {}, please redefine source!", source);
                return false;
            }
            sourceNameToSetClassTypeMap.put(source, mgrClassType);
            setObjFunMap.put(source, (Func3Ex<Object, Object, Object, Homo<Object>>) setObjFun);
        }
        return true;
    }

    @Override
    public boolean containGet(String source) {
        return getFunMap.containsKey(source);
    }

    @Override
    public boolean containSet(String source) {
        return setFuncMap.containsKey(source);
    }

}
