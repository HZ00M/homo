package com.homo.core.utils.delegate;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 委托
 *
 * @param <Target> 接口类型
 *                 举例：
 *                 1: 定义一个接口
 *                 interface MyInterface{
 *                 Integer call(String param);
 *                 }
 *                 2:  定义一个代理对象
 *                 DelegateInterface<DelegateInterFace> delegate = new DelegateInterface<>(MyInterface.class);
 *                 3: 添加一个监听函數，参数和返回值和接口一致即可
 *                 （可以是lambda表达式，也可以是一个对象的方法，可以添加多个监听）
 *                 <p>
 *                 delegate.add(param->{ // 一个字符串参数
 *                 Integer rel = 1;
 *                 System.out.println("监听到delegate被调用，参数：" + param + ":" + rel);
 *                 return rel; // 一个整形返回值
 *                 });
 *                 4：发起一次调用（调用接口中定义的方法）
 *                 delegate.asInterface().call("发起一次调用")；
 *                 输出如下：
 *                 监听到delegate被调用，参数：发起一次调用: 1
 */
@Log4j2
public class DelegateInterface<Target> extends BroadCasterCall<Target> {

    private final Class<Target> interfaceClass;
    private final Method method;
    Target proxy;

    public DelegateInterface(Class<Target> targetInterfaceClass) {
        Assert.isTrue(targetInterfaceClass.isInterface() && targetInterfaceClass.getMethods().length == 1,
                "DelegateInterface delegate targetInterfaceClass must be FunctionalInterface");
        this.interfaceClass = targetInterfaceClass;
        this.method = interfaceClass.getMethods()[0];
        this.method.setAccessible(true);
    }

    public Target asInterface() {
        if (proxy == null) {
            proxy = interfaceClass.cast(createProxy());
        }
        return proxy;
    }

    private Object createProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{interfaceClass}, new DelegateInvoker<>(this));
    }

    @Override
    protected Object execute(Target target, Object... objects) throws Exception {
        return method.invoke(target, objects);
    }

    private class DelegateInvoker<Target> implements InvocationHandler {

        private final DelegateInterface<Target> delegateInterface;

        public DelegateInvoker(DelegateInterface<Target> delegateInterface) {
            this.delegateInterface = delegateInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return delegateInterface.call(args);
        }
    }
}
