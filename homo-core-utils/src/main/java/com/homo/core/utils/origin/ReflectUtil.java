package com.homo.core.utils.origin;


import com.homo.core.utils.origin.exceptions.UtilException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 反射工具类
 * 
 *
 *
 */
public class ReflectUtil {
	private ReflectUtil() {
	}
	/** 构造对象缓存 */
	private static final SimpleCache<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new SimpleCache<>();
	/** 字段缓存 */
	private static final SimpleCache<Class<?>, Field[]> FIELDS_CACHE = new SimpleCache<>();
	/** 方法缓存 */
	private static final SimpleCache<Class<?>, Method[]> METHODS_CACHE = new SimpleCache<>();

	// --------------------------------------------------------------------------------------------------------- Constructor
	/**
	 * 查找类中的指定参数的构造方法，如果找到构造方法，会自动设置可访问为true
	 *
	 * @param <T> 对象类型
	 * @param clazz 类
	 * @param parameterTypes 参数类型，只要任何一个参数是指定参数的父类或接口或相等即可，此参数可以不传
	 * @return 构造方法，如果未找到返回null
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		if (null == clazz) {
			return null;
		}

		final Constructor<?>[] constructors = getConstructors(clazz);
		Class<?>[] pts;
		for (Constructor<?> constructor : constructors) {
			pts = constructor.getParameterTypes();
			if (ClassUtil.isAllAssignableFrom(pts, parameterTypes)) {
				// 构造可访问
				setAccessible(constructor);
				return (Constructor<T>) constructor;
			}
		}
		return null;
	}

	/**
	 * 获得一个类中所有构造列表
	 * 
	 * @param <T> 构造的对象类型
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T>[] getConstructors(Class<T> beanClass) throws SecurityException {
		Assert.notNull(beanClass);
		Constructor<?>[] constructors = CONSTRUCTORS_CACHE.get(beanClass);
		if (null != constructors) {
			return (Constructor<T>[]) constructors;
		}

		constructors = getConstructorsDirectly(beanClass);
		return (Constructor<T>[]) CONSTRUCTORS_CACHE.put(beanClass, constructors);
	}

	/**
	 * 获得一个类中所有字段列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Constructor<?>[] getConstructorsDirectly(Class<?> beanClass) throws SecurityException {
		Assert.notNull(beanClass);
		return beanClass.getDeclaredConstructors();
	}

	// --------------------------------------------------------------------------------------------------------- Field
	/**
	 * 查找指定类中是否包含指定名称对应的字段，包括所有字段（包括非public字段），也包括父类和Object类的字段
	 *
	 * @param beanClass 被查找字段的类,不能为null
	 * @param name 字段名
	 * @return 是否包含字段
	 * @throws SecurityException 安全异常
	 *
	 */
	public static boolean hasField(Class<?> beanClass, String name) throws SecurityException {
		return null != getField(beanClass, name);
	}


	/**
	 * 查找指定类中的所有字段（包括非public字段），也包括父类和Object类的字段， 字段不存在则返回<code>null</code>
	 * 
	 * @param beanClass 被查找字段的类,不能为null
	 * @param name 字段名
	 * @return 字段
	 * @throws SecurityException 安全异常
	 */
	public static Field getField(Class<?> beanClass, String name) throws SecurityException {
		final Field[] fields = getFields(beanClass);
		if (ArrayUtil.isNotEmpty(fields)) {
			for (Field field : fields) {
				if ((name.equals(field.getName()))) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * 获取指定类中字段名和字段对应的Map，包括其父类中的字段
	 *
	 * @param beanClass 类
	 * @return 字段名和字段对应的Map
	 */
	public static Map<String, Field> getFieldMap(Class<?> beanClass) {
		final Field[] fields = getFields(beanClass);
		final HashMap<String, Field> map = MapUtil.newHashMap(fields.length);
		for (Field field : fields) {
			map.put(field.getName(), field);
		}
		return map;
	}

	/**
	 * 获得一个类中所有字段列表，包括其父类中的字段
	 * 
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Field[] getFields(Class<?> beanClass) throws SecurityException {
		Field[] allFields = FIELDS_CACHE.get(beanClass);
		if (null != allFields) {
			return allFields;
		}

		allFields = getFieldsDirectly(beanClass, true);
		return FIELDS_CACHE.put(beanClass, allFields);
	}

	/**
	 * 获得一个类中所有字段列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @param withSuperClassFieds 是否包括父类的字段列表
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Field[] getFieldsDirectly(Class<?> beanClass, boolean withSuperClassFieds) throws SecurityException {
		Assert.notNull(beanClass);

		Field[] allFields = null;
		Class<?> searchType = beanClass;
		Field[] declaredFields;
		while (searchType != null) {
			declaredFields = searchType.getDeclaredFields();
			if (null == allFields) {
				allFields = declaredFields;
			} else {
				allFields = ArrayUtil.append(allFields, declaredFields);
			}
			searchType = withSuperClassFieds ? searchType.getSuperclass() : null;
		}

		return allFields;
	}

	/**
	 * 获取字段值
	 * 
	 * @param obj 对象
	 * @param fieldName 字段名
	 * @return 字段值
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static Object getFieldValue(Object obj, String fieldName) throws UtilException {
		if (null == obj || StringUtil.isBlank(fieldName)) {
			return null;
		}
		return getFieldValue(obj, getField(obj instanceof Class ? (Class<?>) obj : obj.getClass(), fieldName));
	}

	/**
	 * 获取静态字段值
	 *
	 * @param field 字段
	 * @return 字段值
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static Object getStaticFieldValue(Field field) throws UtilException {
		return getFieldValue(null, field);
	}

	/**
	 * 获取字段值
	 * 
	 * @param obj 对象
	 * @param field 字段
	 * @return 字段值
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static Object getFieldValue(Object obj, Field field) throws UtilException {
		if (null == field) {
			return null;
		}
		if (obj instanceof Class) {
			// 静态字段获取时对象为null
			obj = null;
		}

		setAccessible(field);
		Object result;
		try {
			result = field.get(obj);
		} catch (IllegalAccessException e) {
			throw new UtilException( "IllegalAccess for "+field.getDeclaringClass()+"."+field.getName(),e);
		}
		return result;
	}
	/**
	 * 获取所有字段的值
	 * @param obj bean对象
	 * @return 字段值数组
	 *
	 */
	public static Object[] getFieldsValue(Object obj) {
		if (null != obj) {
			final Field[] fields = getFields(obj instanceof Class ? (Class<?>) obj : obj.getClass());
			if (null != fields) {
				final Object[] values = new Object[fields.length];
				for (int i = 0; i < fields.length; i++) {
					values[i] = getFieldValue(obj, fields[i]);
				}
				return values;
			}
		}
		return null;
	}
	/**
	 * 设置字段值
	 * 
	 * @param obj 对象
	 * @param fieldName 字段名
	 * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static void setFieldValue(Object obj, String fieldName, Object value) throws UtilException {
		Assert.notNull(obj);
		Assert.notBlank(fieldName);

		final Field field = getField(obj.getClass(), fieldName);
		Assert.notNull(field, "Field ["+fieldName+"] is not exist in ["+obj.getClass().getName()+"]");
		setFieldValue(obj, field, value);
	}

	/**
	 * 设置字段值
	 * 
	 * @param obj 对象
	 * @param field 字段
	 * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
	 * @throws UtilException UtilException 包装IllegalAccessException异常
	 */
	public static void setFieldValue(Object obj, Field field, Object value) throws UtilException {
		Assert.notNull(field, "Field in ["+obj+"] not exist !");

		setAccessible(field);
		try {
			field.set(obj instanceof Class ? null : obj, value);
		} catch (IllegalAccessException e) {
			throw new UtilException("IllegalAccess for "+obj+"."+field.getName(),e);
		}
	}

	// --------------------------------------------------------------------------------------------------------- method
	/**
	 * 获得指定类本类及其父类中的Public方法名<br>
	 * 去重重载的方法
	 *
	 * @param clazz 类
	 * @return 方法名Set
	 */
	public static Set<String> getPublicMethodNames(Class<?> clazz) {
		final HashSet<String> methodSet = new HashSet<>();
		final Method[] methodArray = getPublicMethods(clazz);
		if(ArrayUtil.isNotEmpty(methodArray)) {
			for (Method method : methodArray) {
				methodSet.add(method.getName());
			}
		}
		return methodSet;
	}

	/**
	 * 获得本类及其父类所有Public方法
	 *
	 * @param clazz 查找方法的类
	 * @return 过滤后的方法列表
	 */
	public static Method[] getPublicMethods(Class<?> clazz) {
		return null == clazz ? null : clazz.getMethods();
	}




	/**
	 * 查找指定Public方法 如果找不到对应的方法或方法不为public的则返回<code>null</code>
	 *
	 * @param clazz 类
	 * @param methodName 方法名
	 * @param paramTypes 参数类型
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 */
	public static Method getPublicMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}
	/**
	 * 查找指定对象中的所有方法（包括非public方法），也包括父对象和Object类的方法
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * @param obj 被查找的对象，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param args 参数
	 * @return 方法
	 * @throws SecurityException 无访问权限抛出异常
	 */
	public static Method getMethodOfObj(Object obj, String methodName, Object... args) throws SecurityException {
		if (null == obj || StringUtil.isBlank(methodName)) {
			return null;
		}
		return getMethod(obj.getClass(), methodName, ClassUtil.getClasses(args));
	}

	/**
	 * 忽略大小写查找指定方法，如果找不到对应的方法则返回<code>null</code>
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 *
	 */
	public static Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		return getMethod(clazz, true, methodName, paramTypes);
	}

	/**
	 * 查找指定方法 如果找不到对应的方法则返回<code>null</code>
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		return getMethod(clazz, false, methodName, paramTypes);
	}

	/**
	 * 查找指定方法 如果找不到对应的方法则返回<code>null</code>
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param ignoreCase 是否忽略大小写
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 *
	 */
	public static Method getMethod(Class<?> clazz, boolean ignoreCase, String methodName, Class<?>... paramTypes) throws SecurityException {
		if (null == clazz || StringUtil.isBlank(methodName)) {
			return null;
		}

		final Method[] methods = getMethods(clazz);
		if (ArrayUtil.isNotEmpty(methods)) {
			for (Method method : methods) {
				if (StringUtil.equals(methodName, method.getName(), ignoreCase)) {
					if (ClassUtil.isAllAssignableFrom(method.getParameterTypes(), paramTypes)) {
						return method;
					}
				}
			}
		}
		return null;
	}
	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 *
	 * <p>
	 * 此方法只检查方法名是否一致，并不检查参数的一致性。
	 * </p>
	 *
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 *
	 */
	public static Method getMethodByName(Class<?> clazz, String methodName) throws SecurityException {
		return getMethodByName(clazz, false, methodName);
	}

	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 *
	 * <p>
	 * 此方法只检查方法名是否一致（忽略大小写），并不检查参数的一致性。
	 * </p>
	 *
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 *
	 */
	public static Method getMethodByNameIgnoreCase(Class<?> clazz, String methodName) throws SecurityException {
		return getMethodByName(clazz, true, methodName);
	}

	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 *
	 * <p>
	 * 此方法只检查方法名是否一致，并不检查参数的一致性。
	 * </p>
	 *
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param ignoreCase 是否忽略大小写
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 *
	 */
	public static Method getMethodByName(Class<?> clazz, boolean ignoreCase, String methodName) throws SecurityException {
		if (null == clazz || StringUtil.isBlank(methodName)) {
			return null;
		}

		final Method[] methods = getMethods(clazz);
		if (ArrayUtil.isNotEmpty(methods)) {
			for (Method method : methods) {
				if (StringUtil.equals(methodName, method.getName(), ignoreCase)) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * 获得指定类中的Public方法名<br>
	 * 去重重载的方法
	 * 
	 * @param clazz 类
	 * @return 方法名Set
	 * @throws SecurityException 安全异常
	 */
	public static Set<String> getMethodNames(Class<?> clazz) throws SecurityException {
		final HashSet<String> methodSet = new HashSet<>();
		final Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			methodSet.add(method.getName());
		}
		return methodSet;
	}


	/**
	 * 获得一个类中所有方法列表，包括其父类中的方法
	 * 
	 * @param beanClass 类
	 * @return 方法列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Method[] getMethods(Class<?> beanClass) throws SecurityException {
		Method[] allMethods = METHODS_CACHE.get(beanClass);
		if (null != allMethods) {
			return allMethods;
		}

		allMethods = getMethodsDirectly(beanClass, true);
		return METHODS_CACHE.put(beanClass, allMethods);
	}

	/**
	 * 获得一个类中所有方法列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @param withSuperClassMethods 是否包括父类的方法列表
	 * @return 方法列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSuperClassMethods) throws SecurityException {
		Assert.notNull(beanClass);

		Method[] allMethods = null;
		Class<?> searchType = beanClass;
		Method[] declaredMethods;
		while (searchType != null) {
			declaredMethods = searchType.getDeclaredMethods();
			if (null == allMethods) {
				allMethods = declaredMethods;
			} else {
				allMethods = ArrayUtil.append(allMethods, declaredMethods);
			}
			searchType = withSuperClassMethods ? searchType.getSuperclass() : null;
		}

		return allMethods;
	}

	/**
	 * 是否为equals方法
	 * 
	 * @param method 方法
	 * @return 是否为equals方法
	 */
	public static boolean isEqualsMethod(Method method) {
		if (method == null || false == "equals".equals(method.getName())) {
			return false;
		}
		final Class<?>[] paramTypes = method.getParameterTypes();
		return (1 == paramTypes.length && paramTypes[0] == Object.class);
	}

	/**
	 * 是否为hashCode方法
	 * 
	 * @param method 方法
	 * @return 是否为hashCode方法
	 */
	public static boolean isHashCodeMethod(Method method) {
		return method != null//
				&& "hashCode".equals(method.getName())//
				&& isEmptyParam(method);
	}

	/**
	 * 是否为toString方法
	 * 
	 * @param method 方法
	 * @return 是否为toString方法
	 */
	public static boolean isToStringMethod(Method method) {
		return method != null//
				&& "toString".equals(method.getName())//
				&& isEmptyParam(method);
	}

	/**
	 * 是否为无参数方法
	 *
	 * @param method 方法
	 * @return 是否为无参数方法
	 */
	public static boolean isEmptyParam(Method method) {
		return method.getParameterTypes().length == 0;
	}

	// --------------------------------------------------------------------------------------------------------- newInstance
	/**
	 * 实例化对象
	 * 
	 * @param <T> 对象类型
	 * @param clazz 类名
	 * @return 对象
	 * @throws UtilException 包装各类异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String clazz) throws UtilException {
		try {
			return (T) Class.forName(clazz).newInstance();
		} catch (Exception e) {
			throw new UtilException( "Instance class ["+clazz+"] error!", e);
		}
	}

	/**
	 * 实例化对象
	 * 
	 * @param <T> 对象类型
	 * @param clazz 类
	 * @param params 构造函数参数
	 * @return 对象
	 * @throws UtilException 包装各类异常
	 */
	public static <T> T newInstance(Class<T> clazz, Object... params) throws UtilException {
		if (ArrayUtil.isEmpty(params)) {
			final Constructor<T> constructor = getConstructor(clazz);
			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw new UtilException( "Instance class ["+clazz+"] error!", e);
			}
		}

		final Class<?>[] paramTypes = ClassUtil.getClasses(params);
		final Constructor<T> constructor = getConstructor(clazz, paramTypes);
		if (null == constructor) {
			throw new UtilException("No Constructor matched for parameter types: [{"+ Arrays.toString(paramTypes)+"}]" );
		}
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new UtilException("Instance class ["+clazz+"] error!", e );
		}
	}

	/**
	 * 尝试遍历并调用此类的所有构造方法，直到构造成功并返回
	 * 
	 * @param <T> 对象类型
	 * @param beanClass 被构造的类
	 * @return 构造后的对象
	 */
	public static <T> T newInstanceIfPossible(Class<T> beanClass) {
		Assert.notNull(beanClass);
		try {
			return newInstance(beanClass);
		} catch (Exception e) {
			// ignore
			// 默认构造不存在的情况下查找其它构造
		}

		final Constructor<T>[] constructors = getConstructors(beanClass);
		Class<?>[] parameterTypes;
		for (Constructor<T> constructor : constructors) {
			parameterTypes = constructor.getParameterTypes();
			if (0 == parameterTypes.length) {
				continue;
			}
			setAccessible(constructor);
			try {
				return constructor.newInstance(ClassUtil.getDefaultValues(parameterTypes));
			} catch (Exception ignore) {
				// 构造出错时继续尝试下一种构造方式
			}
		}
		return null;
	}

	// --------------------------------------------------------------------------------------------------------- invoke
	/**
	 * 执行静态方法
	 * 
	 * @param <T> 对象类型
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 多种异常包装
	 */
	public static <T> T invokeStatic(Method method, Object... args) throws UtilException {
		return invoke(null, method, args);
	}

	/**
	 * 执行方法<br>
	 * 执行前要检查给定参数：
	 * 
	 * <pre>
	 * 1. 参数个数是否与方法参数个数一致
	 * 2. 如果某个参数为null但是方法这个位置的参数为原始类型，则赋予原始类型默认值
	 * </pre>
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 对象，如果执行静态方法，此值为<code>null</code>
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 一些列异常的包装
	 */
	public static <T> T invokeWithCheck(Object obj, Method method, Object... args) throws UtilException {
		final Class<?>[] types = method.getParameterTypes();
		if (null != args) {
			Assert.isTrue(args.length == types.length, "Params length ["+args.length+"] is not fit for param length ["+types.length+"] of method !");
			Class<?> type;
			for (int i = 0; i < args.length; i++) {
				type = types[i];
				if (type.isPrimitive() && null == args[i]) {
					// 参数是原始类型，而传入参数为null时赋予默认值
					args[i] = ClassUtil.getDefaultValue(type);
				}
			}
		}

		return invoke(obj, method, args);
	}

	/**
	 * 执行方法
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 对象，如果执行静态方法，此值为<code>null</code>
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 一些列异常的包装
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object obj, Method method, Object... args) throws UtilException {
		setAccessible(method);

		try {
			return (T) method.invoke(ClassUtil.isStatic(method) ? null : obj, args);
		} catch (Exception e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 执行对象中指定方法
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 方法所在对象
	 * @param methodName 方法名
	 * @param args 参数列表
	 * @return 执行结果
	 * @throws UtilException IllegalAccessException包装
	 *
	 */
	public static <T> T invoke(Object obj, String methodName, Object... args) throws UtilException {
		final Method method = getMethodOfObj(obj, methodName, args);
		if (null == method) {
			throw new UtilException(StringUtil.format("No such method: [{}]", methodName));
		}
		return invoke(obj, method, args);
	}

	/**
	 * 设置方法为可访问（私有方法可以被外部调用）
	 *
	 * @param <T>              AccessibleObject的子类，比如Class、Method、Field等
	 * @param accessibleObject 可设置访问权限的对象，比如Class、Method、Field等
	 * @return 被设置可访问的对象
	 *
	 */
	public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
		if (null != accessibleObject && false == accessibleObject.isAccessible()) {
			accessibleObject.setAccessible(true);
		}
		return accessibleObject;
	}
}
