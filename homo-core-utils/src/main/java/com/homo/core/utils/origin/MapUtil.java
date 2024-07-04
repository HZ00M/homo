package com.homo.core.utils.origin;


import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map相关工具类
 * 
 *
 *
 */
public class MapUtil {

	private MapUtil(){}

	/** 默认初始大小 */
	public static final int DEFAULT_INITIAL_CAPACITY = 16;
	/** 默认增长因子，当Map的size达到 容量*增长因子时，开始扩充Map */
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * Map是否为空
	 * 
	 * @param map 集合
	 * @return 是否为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return null == map || map.isEmpty();
	}

	/**
	 * Map是否为非空
	 * 
	 * @param map 集合
	 * @return 是否为非空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return null != map && false == map.isEmpty();
	}

	/**
	 * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
	 * 空集合使用{@link Collections#emptyMap()}
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param set 提供的集合，可能为null
	 * @return 原集合，若为null返回空集合
	 *
	 */
	public static <K, V> Map<K, V> emptyIfNull(Map<K, V> set) {
		return (null == set) ? Collections.<K, V>emptyMap() : set;
	}

	/**
	 * 如果给定Map为空，返回默认Map
	 *
	 * @param <T>        集合类型
	 * @param <K>        键类型
	 * @param <V>        值类型
	 * @param map        Map
	 * @param defaultMap 默认Map
	 * @return 非空（empty）的原Map或默认Map
	 */
	public static <T extends Map<K, V>, K, V> T defaultIfEmpty(T map, T defaultMap) {
		return isEmpty(map) ? defaultMap : map;
	}
	// ----------------------------------------------------------------------------------------------- new HashMap
	/**
	 * 新建一个HashMap
	 * 
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return HashMap对象
	 */
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * 新建一个HashMap
	 * 
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
	 * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
	 * @return HashMap对象
	 *
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
		int initialCapacity = (int) (size / DEFAULT_LOAD_FACTOR) + 1;
		return isOrder ? new LinkedHashMap<K, V>(initialCapacity) : new HashMap<K, V>(initialCapacity);
	}

	/**
	 * 新建一个HashMap
	 * 
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
	 * @return HashMap对象
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return newHashMap(size, false);
	}

	/**
	 * 新建一个HashMap
	 * 
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
	 * @return HashMap对象
	 */
	public static <K, V> HashMap<K, V> newHashMap(boolean isOrder) {
		return newHashMap(DEFAULT_INITIAL_CAPACITY, isOrder);
	}

	/**
	 * 新建TreeMap，Key有序的Map
	 * 
	 * @param comparator Key比较器
	 * @return TreeMap
	 *
	 */
	public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
		return new TreeMap<>(comparator);
	}

	/**
	 * 新建TreeMap，Key有序的Map
	 * 
	 * @param map Map
	 * @param comparator Key比较器
	 * @return TreeMap
	 *
	 */
	public static <K, V> TreeMap<K, V> newTreeMap(Map<K, V> map, Comparator<? super K> comparator) {
		final TreeMap<K, V> treeMap = new TreeMap<>(comparator);
		if (false == isEmpty(map)) {
			treeMap.putAll(map);
		}
		return treeMap;
	}

	/**
	 * 创建键不重复Map
	 *
	 * @return {@link IdentityHashMap}
	 *
	 */
	public static <K, V> Map<K, V> newIdentityMap(int size) {
		return new IdentityHashMap<>(size);
	}

	/**
	 * 新建一个初始容量为{@link MapUtil#DEFAULT_INITIAL_CAPACITY} 的ConcurrentHashMap
	 *
	 * @param <K> key的类型
	 * @param <V> value的类型
	 * @return ConcurrentHashMap
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * 新建一个ConcurrentHashMap
	 *
	 * @param size 初始容量，当传入的容量小于等于0时，容量为{@link MapUtil#DEFAULT_INITIAL_CAPACITY}
	 * @param <K>  key的类型
	 * @param <V>  value的类型
	 * @return ConcurrentHashMap
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int size) {
		final int initCapacity = size <= 0 ? DEFAULT_INITIAL_CAPACITY : size;
		return new ConcurrentHashMap<>(initCapacity);
	}

	/**
	 * 传入一个Map将其转化为ConcurrentHashMap类型
	 *
	 * @param map map
	 * @param <K> key的类型
	 * @param <V> value的类型
	 * @return ConcurrentHashMap
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(Map<K, V> map) {
		if (isEmpty(map)) {
			return new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
		}
		return new ConcurrentHashMap<>(map);
	}

	/**
	 * 创建Map<br>
	 * 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
	 * 
	 * @param <K> map键类型
	 * @param <V> map值类型
	 * @param mapType map类型
	 * @return {@link Map}实例
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> createMap(Class<?> mapType) {
		if (mapType.isAssignableFrom(AbstractMap.class)) {
			return new HashMap<>();
		} else {
            return (Map<K, V>) ReflectUtil.newInstance(mapType);
		}
	}

	// ----------------------------------------------------------------------------------------------- value of
	/**
	 * 将单一键值对转换为Map
	 * 
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param key 键
	 * @param value 值
	 * @return {@link HashMap}
	 */
	public static <K, V> HashMap<K, V> of(K key, V value) {
		return of(key, value, false);
	}

	/**
	 * 将单一键值对转换为Map
	 * 
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @param key 键
	 * @param value 值
	 * @param isOrder 是否有序
	 * @return {@link HashMap}
	 */
	public static <K, V> HashMap<K, V> of(K key, V value, boolean isOrder) {
		final HashMap<K, V> map = newHashMap(isOrder);
		map.put(key, value);
		return map;
	}

	/**
	 * 将数组转换为Map（HashMap），支持数组元素类型为：
	 * 
	 * <pre>
	 * Map.Entry
	 * 长度大于1的数组（取前两个值），如果不满足跳过此元素
	 * Iterable 长度也必须大于1（取前两个值），如果不满足跳过此元素
	 * Iterator 长度也必须大于1（取前两个值），如果不满足跳过此元素
	 * </pre>
	 * 
	 * <pre>
	 * Map&lt;Object, Object&gt; colorMap = MapUtil.of(new String[][] { { "RED", "#FF0000" }, { "GREEN", "#00FF00" }, { "BLUE", "#0000FF" } });
	 * </pre>
	 * 
	 * 参考：commons-lang
	 * 
	 * @param array 数组。元素类型为Map.Entry、数组、Iterable、Iterator
	 * @return {@link HashMap}
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static HashMap<Object, Object> of(Object[] array) {
		if (array == null) {
			return null;
		}
		final HashMap<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
		for (int i = 0; i < array.length; i++) {
			Object object = array[i];
			if (object instanceof Map.Entry) {
				Entry entry = (Entry) object;
				map.put(entry.getKey(), entry.getValue());
			} else if (object instanceof Object[]) {
				final Object[] entry = (Object[]) object;
				if (entry.length > 1) {
					map.put(entry[0], entry[1]);
				}
			} else if (object instanceof Iterable) {
				Iterator iter = ((Iterable) object).iterator();
				if (iter.hasNext()) {
					final Object key = iter.next();
					if (iter.hasNext()) {
						final Object value = iter.next();
						map.put(key, value);
					}
				}
			} else if (object instanceof Iterator) {
				Iterator iter = ((Iterator) object);
				if (iter.hasNext()) {
					final Object key = iter.next();
					if (iter.hasNext()) {
						final Object value = iter.next();
						map.put(key, value);
					}
				}
			} else {
				throw new IllegalArgumentException(StringUtil.format("Array element {}, '{}', is not type of Map.Entry or Array or Iterable or Iterator", i, object));
			}
		}
		return map;
	}



	/**
	 * 将键值对转换为二维数组，第一维是key，第二纬是value
	 *
	 * @param map Map<?, ?> map
	 * @return 数组
	 */
	public static Object[][] toObjectArray(Map<?, ?> map) {
		if(map == null) {
			return null;
		}
		final Object[][] result = new Object[map.size()][2];
		if(map.isEmpty()) {
			return result;
		}
		int index = 0;
		for(Entry<?, ?> entry : map.entrySet()) {
			result[index][0] = entry.getKey();
			result[index][1] = entry.getValue();
			index++;
		}
		return result;
	}


	/**
	 *排序已有Map，Key有序的Map，使用默认Key排序方式（字母顺序）
	 * 
	 * @param map Map
	 * @return TreeMap
	 *
	 * @see #newTreeMap(Map, Comparator)
	 */
	public static <K, V> TreeMap<K, V> sort(Map<K, V> map){
		return sort(map, null);
	}
	
	/**
	 *排序已有Map，Key有序的Map
	 * 
	 * @param map Map
	 * @param comparator Key比较器
	 * @return TreeMap
	 *
	 * @see #newTreeMap(Map, Comparator)
	 */
	public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
		if(null == map){
			return null;
		}

		TreeMap<K, V> result;
		if(map instanceof TreeMap) {
			//已经是可排序Map，此时只有比较器一致才返回原map
			result = (TreeMap<K, V>)map;
			if(null == comparator || comparator.equals(result.comparator())) {
				return result;
			}
		}else {
			result = newTreeMap(map, comparator);
		}
		
		return result;
	}


	/**
	 * 去掉Map中指定key的键值对，修改原Map
	 *
	 * @param <K>  Key类型
	 * @param <V>  Value类型
	 * @param map  Map
	 * @param keys 键列表
	 * @return 修改后的key
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> removeAny(Map<K, V> map, final K... keys) {
		for (K key : keys) {
			map.remove(key);
		}
		return map;
	}


	/**
	 * 重命名键<br>
	 * 实现方式为一处然后重新put，当旧的key不存在直接返回<br>
	 * 当新的key存在，抛出{@link IllegalArgumentException} 异常
	 *
	 * @param <K>    key的类型
	 * @param <V>    value的类型
	 * @param map    Map
	 * @param oldKey 原键
	 * @param newKey 新键
	 * @return map
	 * @throws IllegalArgumentException 新key存在抛出此异常
	 *
	 */
	public static <K, V> Map<K, V> renameKey(Map<K, V> map, K oldKey, K newKey) {
		if (isNotEmpty(map) && map.containsKey(oldKey)) {
			if (map.containsKey(newKey)) {
				throw new IllegalArgumentException(StringUtil.format("The key '{}' exist !", newKey));
			}
			map.put(newKey, map.remove(oldKey));
		}
		return map;
	}

	/**
	 * 去除Map中值为{@code null}的键值对<br>
	 * 注意：此方法在传入的Map上直接修改。
	 *
	 * @param <K> key的类型
	 * @param <V> value的类型
	 * @param map Map
	 * @return map
	 */
	public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
		if (isEmpty(map)) {
			return map;
		}

		final Iterator<Entry<K, V>> iter = map.entrySet().iterator();
		Entry<K, V> entry;
		while(iter.hasNext()) {
			entry = iter.next();
			if(null == entry.getValue()) {
				iter.remove();
			}
		}

		return map;
	}

	/**
	 * 逆转Map的key和value
	 *
	 * @param <K> 键类型，目标的值类型
	 * @param <V> 值类型，目标的键类型
	 * @param map 被转换的Map
	 * @return 逆转后的Map
	 * @deprecated 请使用{@link MapUtil#reverse(Map)} 代替
	 */
	@Deprecated
	public static <K, V> Map<V, K> inverse(Map<K, V> map) {
		Map<V, K> inverseMap;
		if (map instanceof LinkedHashMap) {
			inverseMap = new LinkedHashMap<>(map.size());
		} else if (map instanceof TreeMap) {
			inverseMap = new TreeMap<>();
		} else {
			inverseMap = new HashMap<>(map.size());
		}

		for (Entry<K, V> entry : map.entrySet()) {
			inverseMap.put(entry.getValue(), entry.getKey());
		}
		return inverseMap;
	}

}
