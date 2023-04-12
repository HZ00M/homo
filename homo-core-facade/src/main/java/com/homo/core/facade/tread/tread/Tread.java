package com.homo.core.facade.tread.tread;

import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.facade.tread.tread.enums.SeqType;
import com.homo.core.facade.tread.tread.op.SeqPoint;
import com.homo.core.utils.fun.FuncEx;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 流程控制工具的基类
 * 基本概念
 *  容器对象mgrObj：管理资源对象的对象，在容器对象上声明CreateObjMethod、GetObjMethod、SetObjMethod以提供资源对象创建，获取，更新的能力
 *  资源对象sourceObj：拥有资源属性的对象，在资源对象上声明GetMethod、SetMethod以提供资源属性获取，校验，更新的能力
 *  资源source: 需要被管理和校验的值对象,可以是Integer,Long,String,Boolean
 *  资源操作顺序是先执行完所有的SUB调用（期间会进行合法性校验，默认检查扣除值是否大于等于0且扣除后的结果值是否大于0，检查成功后才会进行SUB并进行下一步操作，支持自定义校验方法及操作结果回调）
 *  SUB的所有调用都执行成功后，会依照声明顺序依次调用ADD方法（期间会进行合法性校验，默认检查增加值是否大于等于0，支持自定义校验方法及操作结果回调）
 *  框架支持基于支持基于资源对象(source)的调用（add、sub），需要在sourceObj对象的类上使用@SetMthod、@GetMethod声明设资源方法和获取资源方法
 *  框架也支持基于id的调用（addById、subById）,基于id的调用，需要在容器对象上声明CreateObjMethod、GetObjMethod、SetObjMethod以提供资源对象创建，获取，更新的能力
 *  基于id的调用通过GetObjMethod获取不到对象直接执行失败，add时获取不到对象会调用CreateObjMethod,操作完成后执行SetObjMethod方法更新资源对象
 * @author dubian
 */
@Log4j2
public class Tread<T> {
    public String id;
    protected HashMap<SeqType, List<SeqPoint<T>>> seqMap;
    protected Class<T> typeClass;
    protected Map<Object, FuncEx<Object, Object>> createSupplierMap = new HashMap<>();
    public Map<Object, Object> mgrObjMap = new HashMap<>();

    protected Tread(String id, Class<T> typeClass) {
        this.id = id;
        this.typeClass = typeClass;
        seqMap = new HashMap<SeqType, List<SeqPoint<T>>>(2) {
            {
                put(SeqType.ADD, new ArrayList<>());
                put(SeqType.SUB, new ArrayList<>());
            }
        };
    }

    protected Tread<T> registerMgrObjByType(Object mgrObj) {
        Class<?> mgrObjClass = mgrObj.getClass();
        if (mgrObjMap.containsKey(mgrObjClass)) {
            throw new RuntimeException("repeat register mgrObj! register by create() is base on annotation, mgrObj Type " + mgrObjClass + ". please verify legitimacy otherwise register specific mgrObj by registerMgrObj(id,mgrObj,sources)");
        }
        mgrObjMap.put(mgrObjClass, mgrObj);
        return this;
    }

    protected Tread<T> registerMgrObjById(Object identity, Object mgrObj) {
        if (mgrObjMap.containsKey(identity)) {
            throw new RuntimeException("register repeat mgrObj! register by identity [" + identity + "], identity base on id and source, please check id and source!");
        }
        mgrObjMap.put(identity, mgrObj);
        return this;
    }

    protected Tread<T> registerCreateFun(Object identity, FuncEx<Object, Object> createSupplier) {
        if (createSupplierMap.containsKey(identity)) {
            throw new RuntimeException("registerCreateFun repeat identity " + identity + ",please verify legitimacy");
        }
        createSupplierMap.put(identity, createSupplier);
        return this;
    }


    public Tread<T> sub(Object target, String source, T opValue, BiPredicate<T, T> checkPredicate, Consumer<T> resultConsumer) {
        Assert.notNull(target, "sub target is null!");
        seqMap.get(SeqType.SUB).add(new SeqPoint<T>(target, source, opValue, SeqType.SUB, checkPredicate, resultConsumer));
        return this;
    }

    public Tread<T> add(Object target, String source, T opValue, BiPredicate<T, T> checkPredicate, Consumer<T> resultConsumer) {
        return add(target, source, opValue, checkPredicate, resultConsumer, null, null);
    }

    public Tread<T> add(Object target, String source, T opValue, BiPredicate<T, T> checkPredicate, Consumer<T> resultConsumer, Supplier<Object> createObjFun, Object mgrObj) {
        Assert.notNull(target, "add target is null!");
        if (mgrObj != null) {
            registerMgrObjById(TreadMgr.buildIdentity(target, source), mgrObj);
        }
        seqMap.get(SeqType.ADD).add(new SeqPoint<T>(target, source, opValue, SeqType.ADD, checkPredicate, resultConsumer, createObjFun));
        return this;
    }

    public TreadContext<T> buildTread() throws Exception {
        if (TreadMgr.getActualTreadMgr(typeClass) == null) {
            throw new Exception("TreadMgr not implement for type" + typeClass.getSimpleName());
        }
        if (seqMap.get(SeqType.ADD).isEmpty() || seqMap.get(SeqType.SUB).isEmpty()) {
            throw new Exception("sub must be call and add must be call!");
        }
        TreadContext<T> treadContext = new TreadContext<T>(id == null ? "id" : id, seqMap.get(SeqType.SUB), seqMap.get(SeqType.ADD), createSupplierMap, mgrObjMap);
        return treadContext;
    }

    public TreadContext<T> buildOnlyAddTread() throws Exception {
        if (TreadMgr.getActualTreadMgr(typeClass) == null) {
            throw new Exception("TreadMgr not implement for type" + typeClass.getSimpleName());
        }
        if (seqMap.get(SeqType.ADD).isEmpty() || !seqMap.get(SeqType.SUB).isEmpty()) {
            throw new Exception("add must be call and sub forbid call!");
        }
        TreadContext<T> treadContext = new TreadContext<T>(id == null ? "id" : id, seqMap.get(SeqType.SUB), seqMap.get(SeqType.ADD), createSupplierMap, mgrObjMap);
        return treadContext;
    }

    public TreadContext<T> buildOnlySubTread() throws Exception {
        if (TreadMgr.getActualTreadMgr(typeClass) == null) {
            throw new Exception("TreadMgr not implement for type" + typeClass.getSimpleName());
        }
        if (!seqMap.get(SeqType.ADD).isEmpty() || seqMap.get(SeqType.SUB).isEmpty()) {
            throw new Exception("sub must be call and add forbid call!");
        }
        TreadContext<T> treadContext = new TreadContext<T>(id == null ? "id" : id, seqMap.get(SeqType.SUB), seqMap.get(SeqType.ADD), createSupplierMap, mgrObjMap);
        return treadContext;
    }

    public TreadContext<T> buildUnSafeTread() throws Exception {
        if (TreadMgr.getActualTreadMgr(typeClass) == null) {
            throw new Exception("TreadMgr not implement for type" + typeClass.getSimpleName());
        }
        TreadContext<T> treadContext = new TreadContext<T>(id == null ? "id" : id, seqMap.get(SeqType.SUB), seqMap.get(SeqType.ADD), createSupplierMap, mgrObjMap);
        return treadContext;
    }

    /**
     * 必须同时有Add操作和Sub操作
     * @return
     */
    public Homo<Tuple2<ExecRet, String>> exec() {
        try {
            return TreadMgr.getActualTreadMgr(typeClass).exec(buildTread());
        } catch (Exception e) {
            log.error("exec error!", e);
            return Homo.result(Tuples.of(ExecRet.sysError, e.getMessage()));
        }

    }

    /**
     * 只能有Add操作
     * @return
     */
    public Homo<Tuple2<ExecRet, String>> execAddOnly() {
        try {
            return TreadMgr.getActualTreadMgr(typeClass).exec(buildOnlyAddTread());
        } catch (Exception e) {
            log.error("execAddOnly error!", e);
            return Homo.result(Tuples.of(ExecRet.sysError, e.getMessage()));
        }
    }

    /**
     * 只能有Sub操作
     * @return
     */
    public Homo<Tuple2<ExecRet, String>> execSubOnly() {
        try {
            return TreadMgr.getActualTreadMgr(typeClass).exec(buildOnlySubTread());
        } catch (Exception e) {
            log.error("execSubOnly error!", e);
            return Homo.result(Tuples.of(ExecRet.sysError, e.getMessage()));
        }
    }

    /**
     * 不检查操作类型和数量
     * @return
     */
    public Homo<Tuple2<ExecRet, String>> execUnSafe() {
        try {
            return TreadMgr.getActualTreadMgr(typeClass).exec(buildUnSafeTread());
        } catch (Exception e) {
            log.error("execSubOnly error!", e);
            return Homo.result(Tuples.of(ExecRet.sysError, e.getMessage()));
        }
    }
}
