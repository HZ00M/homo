package com.homo.core.tread.tread.stringTread;

import com.homo.core.facade.tread.tread.Tread;
import com.homo.core.facade.tread.tread.TreadMgr;
import com.homo.core.utils.fun.FuncEx;
import org.springframework.util.Assert;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringTread extends Tread<String> {
    /**
     *
     * @param ownerId 用户唯一标识，只做日志输出使用
     * @param mgrObjs 管理对象列表，用于创建、获取、设置资源对象的容器对象
     */
    protected StringTread(String ownerId, Object... mgrObjs) {
        super(ownerId, String.class);
        for (Object mgrObj : mgrObjs) {
            registerMgrObjByType(mgrObj);
        }
    }

    public static StringTread create() {
        return new StringTread("");
    }

    /**
     *
     * @param ownerId ownerId 用户唯一标识，只做日志输出使用
     * @return
     */
    public static StringTread create(String ownerId) {
        return new StringTread(ownerId);
    }

    /**
     *
     * @param ownerId 用户唯一标识，只做日志输出使用
     * @param mgrObjs 管理对象列表，用于创建、获取、设置资源对象的容器对象
     */
    public static StringTread create(String ownerId, Object... mgrObjs) {
        return new StringTread(ownerId, mgrObjs);
    }

    /**
     *
     * @param id 资源id 资源对象唯一id 
     * @param mgrObj 容器对象，用于管理这个资源对象的对象 资源对象所在的容器对象
     * @param sources 资源属性列表
     * @return
     */
    public StringTread registerMgrObj(Object id, Object mgrObj, String ... sources) {
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
    public StringTread registerCreateFun(Object id, FuncEx<Object, Object> newObjSupplier, String ... sources) {
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
    public StringTread sub(Object sourceObj, String source, String opValue) {
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
    public StringTread sub(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate) {
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
    public StringTread sub(Object sourceObj, String source, String opValue, Consumer<String> resultConsumer) {
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
    public StringTread sub(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer) {
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
    public StringTread add(Object sourceObj, String source, String opValue) {
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
    public StringTread add(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate) {
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
    public StringTread add(Object sourceObj, String source, String opValue, Consumer<String> resultConsumer) {
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
    public StringTread add(Object sourceObj, String source, String opValue, Supplier<Object> createObjFun) {
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
    public StringTread add(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer) {
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
    public StringTread add(Object sourceObj, String source, String opValue, Consumer<String> resultConsumer, Supplier<Object> createObjFun) {
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
    public StringTread add(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate, Supplier<Object> createObjFun) {
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
    public StringTread add(Object sourceObj, String source, String opValue, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer, Supplier<Object> createObjFun) {
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
    public StringTread subById(Object id, String source, String opValue) {
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
    public StringTread subById(Object id, String source, String opValue, BiPredicate<String, String> checkPredicate) {
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
    public StringTread subById(Object id, String source, String opValue, Consumer<String> resultConsumer) {
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
    public StringTread subById(Object id, String source, String opValue, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer) {
        Assert.notNull(id, "subTarget is null");
        Assert.isTrue(TreadMgr.targetIsId(id),"id only support String,String,Long");
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
    public StringTread addById(Object id, String source, String opValue) {
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
    public StringTread addById(Object id, String source, String opValue, BiPredicate<String, String> checkPredicate) {
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
    public StringTread addById(Object id, String source, String opValue, Consumer<String> resultConsumer) {
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
    public StringTread addById(Object id, String source, String opValue, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer) {
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
    public StringTread addById(Object id, String source, String opValue, Consumer<String> resultConsumer, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, BiPredicate<String, String> checkPredicate, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj) {
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
    public StringTread addById(Object id, String source, Object mgrObj, String opValue, BiPredicate<String, String> checkPredicate) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, Consumer<String> resultConsumer) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, Consumer<String> resultConsumer, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, BiPredicate<String, String> checkPredicate, Supplier<Object> createObjFun) {
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
    public StringTread addById(Object id, String source, String opValue, Object mgrObj, BiPredicate<String, String> checkPredicate, Consumer<String> resultConsumer, Supplier<Object> createObjFun) {
        Assert.notNull(id, "addTarget is null");
        Assert.isTrue(TreadMgr.targetIsId(id),"id only support String,String,Long");
        super.add(id, source, opValue, checkPredicate, resultConsumer, createObjFun, mgrObj);
        return this;
    }
}
