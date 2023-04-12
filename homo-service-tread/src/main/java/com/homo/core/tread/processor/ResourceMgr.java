package com.homo.core.tread.processor;

import com.homo.core.common.module.Module;
import com.homo.core.facade.tread.processor.anotation.ResourceCheckMethod;
import com.homo.core.facade.tread.processor.anotation.ResourceGetMethod;
import com.homo.core.facade.tread.processor.anotation.ResourceSetMethod;
import com.homo.core.tread.processor.exception.CheckOpException;
import com.homo.core.tread.processor.exception.GetOpException;
import com.homo.core.tread.processor.exception.SetOpException;
import com.homo.core.tread.processor.op.*;
import com.homo.core.utils.rector.Homo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 资源管理
 * 注册资源的set和get方法
 */
@Component
@Log4j2
public class ResourceMgr implements Module {

    /**
     * 注解扫描路径，默认关闭
     */
    @Value("${tpf.resource.scanPath:com.syyx.tpf}")
    String scanPath;
    /**
     * 是否开启校验日志,默认开启
     */
    @Value("${tpf.resource.trace.enable:true}")
    boolean traceEnable;
    static Map<ResourceOpType, Map<Class<?>, Class<? extends ResourceOp<?>>>> opTypeMapHashMap = new HashMap<>();
    static Map<String, GetFun<?>> getFunMap = new HashMap<>();
    static Map<String, SetFun<?>> setFunMap = new HashMap<>();
    static Map<String, List<CheckInfo<?>>> checkFunMap = new HashMap<>();

    static Map<String, Class<?>> resourceTypeToClazz = new HashMap<>();
    static Map<Class, Class> baseTypeMap = new HashMap();
    static Map<String, Class<?>> resourceToOwnerClazzMap = new HashMap<>();

    /**
     * 注册op类
     *
     * @param resourceOpType 操作类型
     * @param opPointClazz   op类
     * @param resourceClazz  资源class信息
     * @throws Exception 异常
     */
    public static void registerOpPoint(ResourceOpType resourceOpType, Class<? extends ResourceOp<?>> opPointClazz, Class<?> resourceClazz) throws Exception {
        opTypeMapHashMap.computeIfAbsent(resourceOpType, t -> new HashMap<>()).put(resourceClazz, opPointClazz);
    }

    static ResourceOp<?> newOpPoint(ResourceOpType resourceOpType, Class<?> resourceClazz) {
        Map<Class<?>, Class<? extends ResourceOp<?>>> resourceMap = opTypeMapHashMap.get(resourceOpType);
        if (resourceMap == null) {
            throw new RuntimeException("resourceMap is null");
        }
        Class<? extends ResourceOp<?>> opClass = resourceMap.get(resourceClazz);
        if (opClass == null) {
            throw new RuntimeException("opClass is null!");
        }
        ResourceOp<?> resourceOp;
        try {
            resourceOp = opClass.newInstance();
            resourceOp.init(resourceOpType);
        } catch (Exception e) {
            throw new RuntimeException("newOpPoint newInstance error ", e.getCause());
        }
        return resourceOp;
    }

    /**
     * 获取资源的方法接口
     */
    public interface GetFun<T> {
        /**
         * 获取资源的方法
         *
         * @param owner         资源拥有者
         * @param resourceInfos 资源信息
         * @return 资源对象
         * @throws Exception 异常
         */
        Homo<T> get(Object owner, Object... resourceInfos) throws RuntimeException;
    }

    /**
     * 设置资源的方法接口
     */
    public interface SetFun<T> {
        /**
         * 设置资源的方法
         *
         * @param opValue       操作值
         * @param owner         资源拥有者
         * @param resourceInfos 资源信息
         * @return 操作结果
         * @throws Exception 异常
         */
        Homo<Boolean> set(T opValue, Object owner, Object... resourceInfos) throws RuntimeException;
    }

    public interface CheckFun<T> {
        /**
         * 设置资源的方法
         *
         * @param opValue       操作值
         * @param owner         资源拥有者
         * @param resourceInfos 资源信息
         * @return 操作结果
         * @throws Exception 异常
         */
        boolean check(T opValue, T newValue, Object owner, Object... resourceInfos) throws RuntimeException;
    }

    @AllArgsConstructor
    static class CheckInfo<T> {
        CheckFun<T> checkFun;
        String checkInfo;
    }


    protected void onInitModule() throws Exception {
        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Int_Resource_Sub.class, int.class);
        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Int_Resource_Sub.class, Integer.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Int_Resource_Add.class, int.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Int_Resource_Add.class, Integer.class);
        baseTypeMap.put(Integer.TYPE, Integer.class);
        baseTypeMap.put(Integer.class, Integer.class);

        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Long_Resource_Sub.class, long.class);
        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Long_Resource_Sub.class, Long.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Long_Resource_Add.class, long.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Long_Resource_Add.class, Long.class);
        baseTypeMap.put(Long.TYPE, Long.class);
        baseTypeMap.put(Long.class, Long.class);

        ResourceMgr.registerOpPoint(ResourceOpType.SUB, String_Resource_Sub.class, String.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, String_Resource_Add.class, String.class);
        baseTypeMap.put(String.class, String.class);

        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Bool_Resource_Sub.class, boolean.class);
        ResourceMgr.registerOpPoint(ResourceOpType.SUB, Bool_Resource_Sub.class, Boolean.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Bool_Resource_Add.class, boolean.class);
        ResourceMgr.registerOpPoint(ResourceOpType.ADD, Bool_Resource_Add.class, Boolean.class);
        baseTypeMap.put(Boolean.TYPE, Boolean.class);
        baseTypeMap.put(Boolean.class, Boolean.class);

        scanGetMethod();
        scanSetMethod();
        scanCheckMethod();
    }

    /**
     * 注册设置方法
     *
     * @param resourceType 资源类型
     * @param setFun       设置函数
     */
    public static <T> void registerResourceSetFun(String resourceType, Class<T> resourceClazz, Class<?> ownerClazz, SetFun<T> setFun) throws Exception {
        setFunMap.put(resourceType, setFun);
        registerResourceClazz(resourceType, resourceClazz);
        registerResourceOwnerClazz(resourceType, ownerClazz);
    }

    /**
     * 注册获得方法
     *
     * @param resourceType 资源类型
     * @param getFun       获取资源的函数
     */
    public static <T> void registerResourceGetFun(String resourceType, Class<T> resourceClazz, Class<?> ownerClazz, GetFun<T> getFun) throws Exception {
        getFunMap.put(resourceType, getFun);
        registerResourceClazz(resourceType, resourceClazz);
        registerResourceOwnerClazz(resourceType, ownerClazz);
    }


    public static <T> void registerResourceCheckFun(String resourceType, Class<?> ownerClazz, CheckFun<T> checkFun, String checkInfo) throws Exception {
        checkFunMap.computeIfAbsent(resourceType, rt -> new LinkedList<>()).add(new CheckInfo<>(checkFun, checkInfo));
        registerResourceOwnerClazz(resourceType, ownerClazz);
    }


    static <T> boolean check(String resourceType, T opValue, T newValue, Object owner, Object[] resourceInfo) throws RuntimeException {
        List<CheckInfo<?>> checkFunList = checkFunMap.get(resourceType);
        if (checkFunList != null) {
            for (CheckInfo<?> checkFun : checkFunList) {
                if (!((CheckInfo<T>) checkFun).checkFun.check(opValue, newValue, owner, resourceInfo)) {
                    System.out.println(checkFun.checkInfo);
                    return false;
                }
            }
        }
        return true;
    }


    static void registerResourceOwnerClazz(String resourceType, Class<?> ownerClazz) throws Exception {
        Class<?> oldzz = resourceToOwnerClazzMap.get(resourceType);
        if (oldzz != null) {
            if (!oldzz.equals(ownerClazz)) {
                throw new Exception("resource class error!");
            }
            return;
        }
        resourceToOwnerClazzMap.put(resourceType, ownerClazz);
    }

    static void registerResourceClazz(String resourceType, Class<?> resourceClazz) throws Exception {
        Class<?> oldzz = resourceTypeToClazz.get(resourceType);
        if (oldzz != null) {
            if (!oldzz.equals(resourceClazz)) {
                throw new Exception("resource class error!");
            }
            return;
        }
        resourceTypeToClazz.put(resourceType, resourceClazz);
    }

    public static Class<?> getResourceClazz(String resourceType) {
        return resourceTypeToClazz.get(resourceType);
    }

    public static Class<?> getResourceOwnerClazz(String resourceType) {
        Class<?> ownerClazz = resourceToOwnerClazzMap.get(resourceType);
        return ownerClazz;
    }

    static Homo<?> getResource(Object owner, String resourceType, Object... resourceInfo) throws RuntimeException {
        GetFun<?> getFun = getFunMap.get(resourceType);
        if (getFun != null) {
            return getFun.get(owner, resourceInfo);
        } else {
            throw new RuntimeException("get fun not found!");
        }
    }

    static <T> Homo<Boolean> checkResource(T opValue, T newValue, Object owner, String resourceType, Object... resourceInfo) throws RuntimeException {
        if (!check(resourceType, opValue, newValue, owner, resourceInfo)) {
            return Homo.result(false);
        }
        return Homo.result(true);
    }

    static <T> Homo<Boolean> setResource(T value, Object owner, String resourceType, Object... resourceInfo) throws RuntimeException {
        SetFun<T> setFun = (SetFun<T>) setFunMap.get(resourceType);
        if (setFun != null) {
            return setFun.set(value, owner, resourceInfo);
        } else {
            throw new RuntimeException("set fun not found!");
        }
    }

    public void scanGetMethod() throws Exception {
        log.info("ResourceMgr scanGetMethod start packagePath {}", scanPath);
        Reflections reflections = new Reflections((new ConfigurationBuilder()).forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> getMethods = reflections.getMethodsAnnotatedWith(ResourceGetMethod.class);
        if (getMethods != null) {
            for (Method method : getMethods) {
                ResourceGetMethod getMethod = method.getAnnotation(ResourceGetMethod.class);
                String resourceType = StringUtils.isEmpty(getMethod.value()) ? method.getName() : getMethod.value();
                if (this.getFunMap.containsKey(resourceType)) {
                    log.error("ResourceMgr scanGetMethod find conflict method {} resourceType {}, please check!", method.getName(), resourceType);
                    System.exit(-1);
                }

                Class<?> returnType = method.getReturnType();
                if ((!baseTypeMap.containsKey(returnType) && !returnType.equals(Homo.class))) {
                    log.warn("scanGetMethod Wrong parameter type resourceType {} returnType {} ", resourceType, returnType);
                    return;
                }
                if (returnType.isAssignableFrom(Homo.class)) {
                    try {
                        Class parameterizedType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                        if (!baseTypeMap.containsKey(parameterizedType)) {
                            log.warn("scanGetMethod Wrong parameter type resourceType {} returnType {} parameterizedType {}", resourceType, returnType,parameterizedType);
                            return;
                        }
                    } catch (Exception e) {
                        log.error("ResourceMgr scanGetMethod error method {} resourceType {}, please check return type!", method.getName(), resourceType);
                        System.exit(-1);
                    }

                }
                method.setAccessible(true);
                int parameterCount = method.getParameterCount();
                GetFun getFun = new GetFun() {
                    @Override
                    public Homo get(Object owner, Object... resourceInfos) throws RuntimeException {
                        try {
                            Object invoke;
                            if (parameterCount > 0) {
                                invoke = method.invoke(owner, resourceInfos);
                            } else {
                                invoke = method.invoke(owner);
                            }
                            if (method.getReturnType().isAssignableFrom(Homo.class)) {
                                return (Homo<Integer>) invoke;
                            } else {
                                return Homo.result(invoke);
                            }
                        } catch (InvocationTargetException e) {
                            throw new GetOpException(method.getName(), resourceType, e.getTargetException());
                        } catch (Exception e) {
                            throw new GetOpException(method.getName(), resourceType, e);
                        }
                    }
                };
                Class<?> ownerClazz = method.getDeclaringClass();
                registerResourceGetFun(resourceType, returnType, ownerClazz, getFun);
            }
        }
        log.info("scanGetMethod finish getFunMap {}", getFunMap);
    }

    public void scanSetMethod() throws Exception {
        log.info("scanSetMethod start packagePath {} ", scanPath);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> setMethods = reflections.getMethodsAnnotatedWith(ResourceSetMethod.class);
        if (setMethods != null) {
            for (Method method : setMethods) {
                ResourceSetMethod setMethod = method.getAnnotation(ResourceSetMethod.class);
                String resourceType = StringUtils.isEmpty(setMethod.value()) ? method.getName() : setMethod.value();
                if (setFunMap.containsKey(resourceType)) {
                    log.error(" scanSetMethod find conflict method {} resourceType {}, please rename the method name!", method.getName(), resourceType);
                    System.exit(-1);
                }
                int parameterCount = method.getParameterCount();
                if (parameterCount < 1) {
                    log.error(" scanSetMethod parameterCount error method {} resourceType {}, please rename the method name!", method.getName(), resourceType);
                    System.exit(-1);
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                method.setAccessible(true);
                SetFun setFun = new SetFun() {
                    @Override
                    public Homo<Boolean> set(Object opValue, Object owner, Object... resourceInfos) throws RuntimeException {
                        try {
                            Object invoke;
                            if (resourceInfos != null && resourceInfos.length > 0) {
                                Object[] params = new Object[resourceInfos.length + 1];
                                params[0] = opValue;
                                for (int i = 0; i < resourceInfos.length; i++) {
                                    params[i + 1] = resourceInfos[i];
                                }
                                invoke = method.invoke(owner, params);
                            } else {
                                invoke = method.invoke(owner, opValue);
                            }
                            if (method.getReturnType().isAssignableFrom(Homo.class)) {
                                return ((Homo<Boolean>) invoke).nextValue(ret -> true);
                            } else {
                                return Homo.result(true);
                            }
                        } catch (InvocationTargetException e) {
                            throw new SetOpException(method.getName(), opValue.toString(), e.getTargetException());
                        } catch (Exception e) {
                            throw new SetOpException(method.getName(), opValue.toString(), e);
                        }
                    }
                };
                Class<?> ownerClazz = method.getDeclaringClass();
                registerResourceSetFun(resourceType, parameterTypes[0], ownerClazz, setFun);
            }
        }
        log.info("scanSetMethod finish setFunMap {}", setFunMap);
    }

    public void scanCheckMethod() throws Exception {
        log.info("scanSetMethod start packagePath {} ", scanPath);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPath).addScanners(Scanners.MethodsAnnotated));
        Set<Method> checkMethods = reflections.getMethodsAnnotatedWith(ResourceCheckMethod.class);
        if (checkMethods != null) {
            for (Method method : checkMethods) {
                ResourceCheckMethod checkMethod = method.getAnnotation(ResourceCheckMethod.class);
                String resourceType = StringUtils.isEmpty(checkMethod.value()) ? method.getName() : checkMethod.value();
                if (checkFunMap.containsKey(resourceType)) {
                    log.error("scanSetMethod find conflict method {} resourceType {}, please rename the method name!", method.getName(), resourceType);
                    System.exit(-1);
                }
                Class<?> returnType = method.getReturnType();
                if ((!returnType.isAssignableFrom(boolean.class) && !returnType.isAssignableFrom(Boolean.class) && !returnType.equals(Homo.class))) {
                    log.warn("scanCheckMethod Wrong parameter type resourceType {} returnType {} ", resourceType, returnType);
                    return;
                }
                if (returnType.isAssignableFrom(Homo.class)) {
                    try {
                        Class parameterizedType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                        if (!baseTypeMap.containsKey(parameterizedType)) {
                            log.warn("scanCheckMethod Wrong parameter type method {} resourceType {} returnType {} parameterizedType {}",method.getName(), resourceType, returnType,parameterizedType);
                            return;
                        }
                    } catch (Exception e) {
                        log.error(" scanGetMethod error method {} resourceType {}, please check return type!", method.getName(), resourceType);
                        System.exit(-1);
                    }

                }
                method.setAccessible(true);

                CheckFun checkFun = new CheckFun() {
                    @Override
                    public boolean check(Object opValue, Object newValue, Object owner, Object... resourceInfos) throws RuntimeException {
                        try {
                            Object invoke;
                            if (resourceInfos != null && resourceInfos.length > 0) {
                                Object[] params = new Object[resourceInfos.length + 2];
                                params[0] = opValue;
                                params[1] = newValue;
                                for (int i = 0; i < resourceInfos.length; i++) {
                                    params[i + 2] = resourceInfos[i];
                                }
                                invoke = method.invoke(owner, params);
                            } else {
                                invoke = method.invoke(owner, opValue, newValue);
                            }
                            if (method.getReturnType().isAssignableFrom(Homo.class)) {
                                return ((Homo<Boolean>) invoke).block();
                            } else {
                                return (Boolean) invoke;
                            }
                        } catch (InvocationTargetException e) {
                            throw new CheckOpException(method.getName(), opValue.toString(), e.getTargetException());
                        } catch (Exception e) {
                            throw new CheckOpException(method.getName(), opValue.toString(), e);
                        }
                    }
                };
                Class<?> ownerClazz = method.getDeclaringClass();
                registerResourceCheckFun(resourceType, ownerClazz, checkFun, checkMethod.checkInfo());
            }
        }
        log.info("scanCheckMethod finish checkFunMap_{}", checkFunMap);
    }


}
