package com.homo.core.utils.origin.single;


import com.homo.core.utils.origin.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例原型类<br>
 * 提供单例对象的统一管理，当调用get方法时，如果对象池中存在此对象，返回此对象，否则创建新对象返回<br>
 * 注意：单例针对的是类和对象，因此get方法第一次调用时创建的对象始终唯一，也就是说就算参数变更，返回的依旧是第一次创建的对象
 *
 *
 *
 */
public final class SingletonPrototype {
	private static Map<String, Object> pool = new ConcurrentHashMap<>();

	private SingletonPrototype() {
	}

	/**
	 * 获得指定类的单例对象<br>
	 * 对象存在于池中返回，否则创建，每次调用此方法获得的对象为同一个对象<br>
	 * 注意：单例针对的是类和对象，因此get方法第一次调用时创建的对象始终唯一，也就是说就算参数变更，返回的依旧是第一次创建的对象
	 *
	 * @param <T> 单例对象类型
	 * @param clazz 类
	 * @param params 构造方法参数
	 * @return 单例对象
	 */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Object... params) {
        Assert.notNull(clazz, "Class must be not null !");
        final String key = buildKey(clazz.getName(), params);
        T obj = (T) pool.get(key);

        if (null == obj) {
            synchronized (SingletonPrototype.class) {
                obj = (T) pool.get(key);
                if (null == obj) {
                    obj = (T) ReflectUtil.newInstance(clazz, params);
                    pool.put(key, obj);
                }
            }
        }

        return obj;
    }

	/**
	 * 获得指定类的单例对象<br>
	 * 对象存在于池中返回，否则创建，每次调用此方法获得的对象为同一个对象<br>
	 * 注意：单例针对的是类和对象，因此get方法第一次调用时创建的对象始终唯一，也就是说就算参数变更，返回的依旧是第一次创建的对象
	 *
	 * @param <T> 单例对象类型
	 * @param className 类名
	 * @param params 构造参数
	 * @return 单例对象
	 */
	public static <T> T get(String className, Object... params) {
		Assert.notBlank(className, "Class name must be not blank !");
		final Class<T> clazz = ClassUtil.loadClass(className);
		return get(clazz, params);
	}

	/**
	 * 将已有对象放入单例中，其Class做为键
	 *
	 * @param obj 对象
	 *
	 */
	public static void put(Object obj) {
		Assert.notNull(obj, "Bean object must be not null !");
		pool.put(obj.getClass().getName(), obj);
	}

	/**
	 * 移除指定Singleton对象
	 *
	 * @param clazz 类
	 */
	public static void remove(Class<?> clazz) {
		if (null != clazz) {
			pool.remove(clazz.getName());
		}
	}

	/**
	 * 清除所有Singleton对象
	 */
	public static void destroy() {
		pool.clear();
	}

	// ------------------------------------------------------------------------------------------- Private method start
	/**
	 * 构建key
	 *
	 * @param className 类名
	 * @param params 参数列表
	 * @return key
	 */
	private static String buildKey(String className, Object... params) {
		if (ArrayUtil.isEmpty(params)) {
			return className;
		}
		return StringUtil.format("{}#{}", className, ArrayUtil.join(params, "_"));
	}
	// ------------------------------------------------------------------------------------------- Private method end
}
