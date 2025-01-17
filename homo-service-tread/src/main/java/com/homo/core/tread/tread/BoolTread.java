package com.homo.core.tread.tread;

import com.homo.core.facade.tread.tread.Tread;
import com.homo.core.facade.tread.tread.TreadMgr;
import com.homo.core.utils.fun.FuncWithException;
import org.springframework.util.Assert;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoolTread extends Tread<Boolean> {
    /**
     *
     * @param ownerId 用户唯一标识，只做日志输出使用
     * @param mgrObjs 管理对象列表，用于创建、获取、设置资源对象的容器对象
     */
    protected BoolTread(String ownerId, Object... mgrObjs) {
        super(ownerId, Boolean.class);
        for (Object mgrObj : mgrObjs) {
            registerMgrObjByType(mgrObj);
        }
    }

    public static BoolTread create() {
        return new BoolTread("");
    }

    /**
     *
     * @param ownerId ownerId 用户唯一标识，只做日志输出使用
     * @return
     */
    public static BoolTread create(String ownerId) {
        return new BoolTread(ownerId);
    }

    /**
     *
     * @param ownerId 用户唯一标识，只做日志输出使用
     * @param mgrObjs 管理对象列表，用于创建、获取、设置资源对象的容器对象
     */
    public static BoolTread create(String ownerId, Object... mgrObjs) {
        return new BoolTread(ownerId, mgrObjs);
    }

    /**
     *
     * @param id 资源id 资源对象唯一id 
     * @param mgrObj 容器对象，用于管理这个资源对象的对象 资源对象所在的容器对象
     * @param sources 资源属性列表
     * @return
     */
    public BoolTread registerMgrObj(Object id, Object mgrObj, String ... sources) {
        for (String source : sources) {
            registerMgrObjById(TreadMgr.buildIdentity(id, source), mgrObj);
        }
        return this;
    }

    /**
     *
     * @param id 资源id 资源对象唯一id 
     * @param newObjSupplier 创建对象方法，创建对象优先级: addById中的createObjFun > Tread中的registerCreateFun >注解上的@CreateObjMethod
     * @param sources 资源属性列表
     * @return
     */
    public BoolTread registerCreateFun(Object id, FuncWithException<Object, Object> newObjSupplier, String ... sources) {
        for (String source : sources) {
            registerCreateFun(TreadMgr.buildIdentity(id, source), newObjSupplier);
        }
        return this;
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @return
     */
    public BoolTread sub(Object sourceObj, String source, Boolean opValue) {
        return sub(sourceObj, source, opValue, null, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值 操作值
     * @param checkPredicate 操作资源前的检查函数
     * @return
     */
    public BoolTread sub(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate) {
        return sub(sourceObj, source, opValue, checkPredicate, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread sub(Object sourceObj, String source, Boolean opValue, Consumer<Boolean> resultConsumer) {
        return sub(sourceObj, source, opValue, null, resultConsumer);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread sub(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer) {
        Assert.notNull(sourceObj, "subTarget is null");
        super.sub(sourceObj, source, opValue, checkPredicate, resultConsumer);
        return this;
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue) {
        return add(sourceObj, source, opValue, null, null, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate) {
        return add(sourceObj, source, opValue, checkPredicate, null, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, Consumer<Boolean> resultConsumer) {
        return add(sourceObj, source, opValue, null, resultConsumer, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param createObjFun 创建资源对象函数 创建资源对象函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, Supplier<Object> createObjFun) {
        return add(sourceObj, source, opValue, null, null, createObjFun);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer) {
        return add(sourceObj, source, opValue, checkPredicate, resultConsumer, null);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, Consumer<Boolean> resultConsumer, Supplier<Object> createObjFun) {
        return add(sourceObj, source, opValue, null, resultConsumer, createObjFun);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Supplier<Object> createObjFun) {
        return add(sourceObj, source, opValue, checkPredicate, null, createObjFun);
    }

    /**
     *
     * @param sourceObj 资源对象
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread add(Object sourceObj, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer, Supplier<Object> createObjFun) {
        Assert.notNull(sourceObj, "addTarget is null");
        super.add(sourceObj, source, opValue, checkPredicate, resultConsumer, createObjFun,null);
        return this;
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @return
     */
    public BoolTread subById(Object id, String source, Boolean opValue) {
        return subById(id, source, opValue, null, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @return
     */
    public BoolTread subById(Object id, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate) {
        return subById(id, source, opValue, checkPredicate, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread subById(Object id, String source, Boolean opValue, Consumer<Boolean> resultConsumer) {
        return subById(id, source, opValue, null, resultConsumer);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread subById(Object id, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer) {
        Assert.notNull(id, "subTarget is null");
        Assert.isTrue(TreadMgr.targetIsId(id),"id only support String,Boolean,Long");
        super.sub(id, source, opValue, checkPredicate, resultConsumer);
        return this;
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue) {
        return addById(id, source, opValue, null, null, null, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate) {
        return addById(id, source, opValue, null, checkPredicate, null, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Consumer<Boolean> resultConsumer) {
        return addById(id, source, opValue, null, null, resultConsumer, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, null, null, null, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer) {
        return addById(id, source, opValue, null, checkPredicate, resultConsumer, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param resultConsumer 操作资源后的结果函数
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Consumer<Boolean> resultConsumer, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, null, null, resultConsumer, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, null, checkPredicate, null, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj) {
        return addById(id, source, opValue, mgrObj, null, null, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param opValue 操作值
     * @param checkPredicate 操作资源前的检查函数 
     * @return
     */
    public BoolTread addById(Object id, String source, Object mgrObj, Boolean opValue, BiPredicate<Boolean, Boolean> checkPredicate) {
        return addById(id, source, opValue, mgrObj, checkPredicate, null, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, Consumer<Boolean> resultConsumer) {
        return addById(id, source, opValue, mgrObj, null, resultConsumer, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, mgrObj, null, null, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer) {
        return addById(id, source, opValue, mgrObj, checkPredicate, resultConsumer, null);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param resultConsumer 操作资源后的结果函数
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, Consumer<Boolean> resultConsumer, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, mgrObj, null, resultConsumer, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param checkPredicate 操作资源前的检查函数 
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, BiPredicate<Boolean, Boolean> checkPredicate, Supplier<Object> createObjFun) {
        return addById(id, source, opValue, mgrObj, checkPredicate, null, createObjFun);
    }

    /**
     *
     * @param id 资源id
     * @param source 资源属性
     * @param opValue 操作值
     * @param mgrObj 容器对象，用于管理这个资源对象的对象
     * @param checkPredicate 操作资源前的检查函数 
     * @param resultConsumer 操作资源后的结果函数
     * @param createObjFun 创建资源对象函数
     * @return
     */
    public BoolTread addById(Object id, String source, Boolean opValue, Object mgrObj, BiPredicate<Boolean, Boolean> checkPredicate, Consumer<Boolean> resultConsumer, Supplier<Object> createObjFun) {
        Assert.notNull(id, "addTarget is null");
        Assert.isTrue(TreadMgr.targetIsId(id),"id only support String,Boolean,Long");
        super.add(id, source, opValue, checkPredicate, resultConsumer, createObjFun, mgrObj);
        return this;
    }
}
