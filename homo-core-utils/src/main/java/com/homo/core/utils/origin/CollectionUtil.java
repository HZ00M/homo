package com.homo.core.utils.origin;



import com.homo.core.utils.origin.exceptions.UtilException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 集合相关工具类，包括数组
 * 
 *
 */
public  final class CollectionUtil  {
    private CollectionUtil(){}
    /**
     * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
     * 空集合使用{@link Collections#emptySet()}
     *
     * @param <T> 集合元素类型
     * @param set 提供的集合，可能为null
     * @return 原集合，若为null返回空集合
     *
     */
    public static <T> Set<T> emptyIfNull(final Set<T> set) {
        return (null == set) ? Collections.<T>emptySet() : set;
    }

    /**
     * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
     * 空集合使用{@link Collections#emptyList()}
     *
     * @param <T> 集合元素类型
     * @param set 提供的集合，可能为null
     * @return 原集合，若为null返回空集合
     *
     */
    public static <T> List<T> emptyIfNull(final List<T> set) {
        return (null == set) ? Collections.<T>emptyList() : set;
    }


    /**
     * 判断指定集合是否包含指定值，如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     *
     * @param collection 集合
     * @param value 需要查找的值
     * @return 如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     */
    public static boolean contains(final Collection<?> collection, Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }
    /**
     * 其中一个集合在另一个集合中是否至少包含一个元素，既是两个集合是否至少有一个共同的元素
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 其中一个集合在另一个集合中是否至少包含一个元素

     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        if (coll1.size() < coll2.size()) {
            for (Object object : coll1) {
                if (coll2.contains(object)) {
                    return true;
                }
            }
        } else {
            for (Object object : coll2) {
                if (coll1.contains(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 集合1中是否包含集合2中所有的元素，既集合2是否为集合1的子集
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 集合1中是否包含集合2中所有的元素
     *
     */
    public static boolean containsAll(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2) || coll1.size() < coll2.size()) {
            return false;
        }

        for (Object object : coll2) {
            if (false == coll1.contains(object)) {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------------------------------------------- new HashMap
    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     * @see MapUtil#newHashMap()
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return MapUtil.newHashMap();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
     * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象

     * @see MapUtil#newHashMap(int, boolean)
     */
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
        return MapUtil.newHashMap(size, isOrder);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
     * @return HashMap对象
     * @see MapUtil#newHashMap(int)
     */
    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return MapUtil.newHashMap(size);
    }

    // ----------------------------------------------------------------------------------------------- new HashSet
    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param ts 元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    public static <T> HashSet<T> newHashSet(T... ts) {
        return newHashSet(false, ts);
    }

    /**
     * 新建一个LinkedHashSet
     *
     * @param <T> 集合元素类型
     * @param ts 元素数组
     * @return HashSet对象
     *
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> newLinkedHashSet(T... ts) {
        return (LinkedHashSet<T>)newHashSet(true, ts);
    }
    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回 {@link HashSet}
     * @param ts 元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    public static <T> HashSet<T> newHashSet(boolean isSorted, T... ts) {
        if (null == ts) {
            return isSorted ? new LinkedHashSet<T>() : new HashSet<T>();
        }
        int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        final HashSet<T> set = isSorted ? new LinkedHashSet<>(initialCapacity) : new HashSet<T>(initialCapacity);
        Collections.addAll(set, ts);
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(Collection<T> collection) {
        return newHashSet(false, collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param collection 集合，用于初始化Set
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Collection<T> collection) {
        return isSorted ? new LinkedHashSet<>(collection) : new HashSet<>(collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param iter {@link Iterator}
     * @return HashSet对象

     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Iterator<T> iter) {
        if (null == iter) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<>() : new HashSet<>();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param enumration {@link Enumeration}
     * @return HashSet对象

     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Enumeration<T> enumration) {
        if (null == enumration) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<>() : new HashSet<>();
        while (enumration.hasMoreElements()) {
            set.add(enumration.nextElement());
        }
        return set;
    }

    // ----------------------------------------------------------------------------------------------- List
    /**
     * 新建一个空List
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @return List对象
     *
     */
    public static <T> List<T> list(boolean isLinked) {
        return isLinked ? new LinkedList<>() : new ArrayList<>();
    }

    /**
     * 新建一个List
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param values 数组
     * @return List对象
     *
     */
    @SafeVarargs
    public static <T> List<T> list(boolean isLinked, T... values) {
        if (ArrayUtil.isEmpty(values)) {
            return list(isLinked);
        }
        final List<T> arrayList = isLinked ? new LinkedList<T>() : new ArrayList<T>(values.length);
        Collections.addAll(arrayList, values);
        return arrayList;
    }

    /**
     * 新建一个List
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param collection 集合
     * @return List对象
     *
     */
    public static <T> List<T> list(boolean isLinked, Collection<T> collection) {
        if (null == collection) {
            return list(isLinked);
        }
        return isLinked ? new LinkedList<>(collection) : new ArrayList<>(collection);
    }

    /**
     * 新建一个List<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iterable {@link Iterable}
     * @return List对象
     *
     */
    public static <T> List<T> list(boolean isLinked, Iterable<T> iterable) {
        if(null == iterable) {
            return list(isLinked);
        }
        return list(isLinked, iterable.iterator());
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iter {@link Iterator}
     * @return ArrayList对象
     *
     */
    public static <T> List<T> list(boolean isLinked, Iterator<T> iter) {
        final List<T> list = list(isLinked);
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * 新建一个List<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     *
     */
    public static <T> List<T> list(boolean isLinked, Enumeration<T> enumration) {
        final List<T> list = list(isLinked);
        if (null != enumration) {
            while (enumration.hasMoreElements()) {
                list.add(enumration.nextElement());
            }
        }
        return list;
    }

    /**
     * 新建一个ArrayList
     *
     * @param <T> 集合元素类型
     * @param values 数组
     * @return ArrayList对象
     */
    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... values) {
        return (ArrayList<T>) list(false, values);
    }
    /**
     * 数组转为ArrayList
     *
     * @param <T> 集合元素类型
     * @param values 数组
     * @return ArrayList对象
     */
    @SafeVarargs
    public static <T> ArrayList<T> toList(T... values) {
        return newArrayList(values);
    }
    /**
     * 新建一个ArrayList
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Collection<T> collection) {
        return (ArrayList<T>) list(false, collection);
    }
    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param iterable {@link Iterable}
     * @return ArrayList对象
     *
     */
    public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
        return (ArrayList<T>) list(false, iterable);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param iter {@link Iterator}
     * @return ArrayList对象
     *
     */
    public static <T> ArrayList<T> newArrayList(Iterator<T> iter) {
        return (ArrayList<T>) list(false, iter);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T> 集合元素类型
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     *
     */
    public static <T> ArrayList<T> newArrayList(Enumeration<T> enumration) {
        return (ArrayList<T>) list(false, enumration);
    }

    // ----------------------------------------------------------------------new LinkedList
    /**
     * 新建LinkedList
     *
     * @param values 数组
     * @param <T> 类型
     * @return LinkedList
     *
     */
    @SafeVarargs
    public static <T> LinkedList<T> newLinkedList(T... values) {
        return (LinkedList<T>) list(true, values);
    }
    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @return {@link CopyOnWriteArrayList}
     */
    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(Collection<T> collection) {
        return (null == collection) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(collection));
    }

    /**
     * 新建{@link BlockingQueue}<br>
     * 在队列为空时，获取元素的线程会等待队列变为非空。当队列满时，存储元素的线程会等待队列可用。
     *
     * @param capacity 容量
     * @param isLinked 是否为链表形式
     * @return {@link BlockingQueue}
     *
     */
    public static <T> BlockingQueue<T> newBlockingQueue(int capacity, boolean isLinked){
        BlockingQueue<T> queue;
        if(isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        }else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }
    /**
     * 创建新的集合对象
     *
     * @param <T> 集合类型
     * @param collectionType 集合类型
     * @return 集合类型对应的实例

     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Collection<T> create(Class<?> collectionType) {
        Collection<T> list ;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            // 抽象集合默认使用ArrayList
            list = new ArrayList<>();
        }

        // Set
        else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>();
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>();
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>();
        } else if (collectionType.isAssignableFrom(EnumSet.class)) {
            list = (Collection<T>) EnumSet.noneOf((Class<Enum>) ClassUtil.getTypeArgument(collectionType));
        }

        // List
        else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        }

        // Others，直接实例化
        else {
            try {
                list = (Collection<T>) ReflectUtil.newInstance(collectionType);
            } catch (Exception e) {
                throw new UtilException(e);
            }
        }
        return list;
    }

    /**
     * 创建Map<br>
     * 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K> map键类型
     * @param <V> map值类型
     * @param mapType map类型
     * @return {@link Map}实例
     * @see MapUtil#createMap(Class)
     */
    public static <K, V> Map<K, V> createMap(Class<?> mapType) {
        return MapUtil.createMap(mapType);
    }

    /**
     * 去重集合
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @return {@link ArrayList}
     */
    public static <T> ArrayList<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        } else if (collection instanceof Set) {
            return new ArrayList<>(collection);
        } else {
            return new ArrayList<>(new LinkedHashSet<>(collection));
        }
    }

    /**
     * 截取集合的部分
     *
     * @param <T> 集合元素类型
     * @param list 被截取的数组
     * @param start 开始位置（包含）
     * @param end 结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     */
    public static <T> List<T> sub(List<T> list, int start, int end) {
        return sub(list, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T> 集合元素类型
     * @param list 被截取的数组
     * @param start 开始位置（包含）
     * @param end 结束位置（不包含）
     * @param step 步进
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     *
     */
    public static <T> List<T> sub(List<T> list, int start, int end, int step) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return new ArrayList<>(0);
        }

        final int size = list.size();
        if (start < 0) {
            start += size;
        }
        if (end < 0) {
            end += size;
        }
        if (start == size) {
            return new ArrayList<>(0);
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > size) {
            if (start >= size) {
                return new ArrayList<>(0);
            }
            end = size;
        }

        if (step <= 1) {
            return list.subList(start, end);
        }

        final List<T> result = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * 截取集合的部分
     *
     * @param <T> 集合元素类型
     * @param collection 被截取的数组
     * @param start 开始位置（包含）
     * @param end 结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回null
     */
    public static <T> List<T> sub(Collection<T> collection, int start, int end) {
        return sub(collection, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T> 集合元素类型
     * @param list 被截取的数组
     * @param start 开始位置（包含）
     * @param end 结束位置（不包含）
     * @param step 步进
     * @return 截取后的数组，当开始位置超过最大时，返回空集合
     *
     */
    public static <T> List<T> sub(Collection<T> list, int start, int end, int step) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        return sub(new ArrayList<>(list), start, end, step);
    }


    /**
     * 对集合按照指定长度分段，每一个段为单独的集合，返回这个集合的列表
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @param size 每个段的长度
     * @return 分段列表
     */
    public static <T> List<List<T>> split(Collection<T> collection, int size) {
        final List<List<T>> result = new ArrayList<>();

        ArrayList<T> subList = new ArrayList<>(size);
        for (T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }
    public static <T>  List<T[]> split(T[] ary,int subSize)
    {
        int len= ary.length;
        int count = len % subSize == 0 ? len / subSize: len / subSize + 1;
        List<T[]> subAryList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int begin = i * subSize;
            int end = begin + subSize;
            if (end > len) {
                end = len;
            }
            T[] item = Arrays.copyOfRange(ary, begin, end);
            subAryList.add(item);
        }
        return subAryList;
    }



    /**
     * 去掉集合中的多个元素，此方法直接修改原集合
     *
     * @param <T>         集合类型
     * @param <E>         集合元素类型
     * @param collection  集合
     * @param elesRemoved 被去掉的元素数组
     * @return 原集合
     */
    @SuppressWarnings("unchecked")
    public static <T extends Collection<E>, E> T removeAny(T collection, E... elesRemoved) {
        collection.removeAll(newHashSet(elesRemoved));
        return collection;
    }


    // ---------------------------------------------------------------------- isEmpty
    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    public static boolean isEmpty(Iterable<?> iterable) {
        return null == iterable || isEmpty(iterable.iterator());
    }
    public static boolean isEmpty(Iterator<?> Iterator) {
        return null == Iterator || false == Iterator.hasNext();
    }
	/**
	 * 如果给定集合为空，返回默认集合
	 *
	 * @param <T>               集合类型
	 * @param <E>               集合元素类型
	 * @param collection        集合
	 * @param defaultCollection 默认数组
	 * @return 非空（empty）的原集合或默认集合
	 *
	 */
	public static <T extends Collection<E>, E> T defaultIfEmpty(T collection, T defaultCollection) {
		return isEmpty(collection) ? defaultCollection : collection;
	}

	/**
	 * Map是否为空
	 *
	 * @param map 集合
	 * @return 是否为空
	 * @see MapUtil#isEmpty(Map)
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return MapUtil.isEmpty(map);
	}



    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isEmpty(Enumeration<?> enumeration) {
        return null == enumeration || false == enumeration.hasMoreElements();
    }

    // ---------------------------------------------------------------------- isNotEmpty

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return false == isEmpty(collection);
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     * @see MapUtil#isNotEmpty(Map)
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtil.isNotEmpty(map);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isNotEmpty(Enumeration<?> enumeration) {
        return null != enumeration && enumeration.hasMoreElements();
    }

    public static boolean isNotEmpty(Iterable<?> iterable) {
        return null != iterable && isNotEmpty(iterable.iterator());
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(Iterator<?> Iterator) {
        return null != Iterator && Iterator.hasNext();
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iter 被检查的{@link Iterable}对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     */
    public static boolean hasNull(Iterable<?> iter) {
        return hasNull(null == iter ? null : iter.iterator());
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iter 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     */
    public static boolean hasNull(Iterator<?> iter) {
        if (null == iter) {
            return true;
        }
        while (iter.hasNext()) {
            if (null == iter.next()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iter 被检查的{@link Iterable}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     */
    public static boolean isAllNull(Iterable<?> iter) {
        return isAllNull(null == iter ? null : iter.iterator());
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iter 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     */
    public static boolean isAllNull(Iterator<?> iter) {
        if (null == iter) {
            return true;
        }

        while (iter.hasNext()) {
            if (null != iter.next()) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------------- zip

    /**
     * 映射键值（参考Python的zip()函数）<br>
     * 例如：<br>
     * keys = a,b,c,d<br>
     * values = 1,2,3,4<br>
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys 键列表
     * @param values 值列表
     * @param delimiter 分隔符
     * @param isOrder 是否有序
     * @return Map

     */
    public static Map<String, String> zip(String keys, String values, String delimiter, boolean isOrder) {
        return ArrayUtil.zip(StringUtil.split(keys, delimiter), StringUtil.split(values, delimiter), isOrder);
    }

    /**
     * 映射键值（参考Python的zip()函数），返回Map无序<br>
     * 例如：<br>
     * keys = a,b,c,d<br>
     * values = 1,2,3,4<br>
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys 键列表
     * @param values 值列表
     * @param delimiter 分隔符
     * @return Map
     */
    public static Map<String, String> zip(String keys, String values, String delimiter) {
        return zip(keys, values, delimiter, false);
    }

    /**
     * 映射键值（参考Python的zip()函数）<br>
     * 例如：<br>
     * keys = [a,b,c,d]<br>
     * values = [1,2,3,4]<br>
     * 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param keys 键列表
     * @param values 值列表
     * @return Map
     */
    public static <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
        if (isEmpty(keys) || isEmpty(values)) {
            return null;
        }

        int entryCount = Math.min(keys.size(), values.size());
        final Map<K, V> map = newHashMap(entryCount);

        final Iterator<K> keyIterator = keys.iterator();
        final Iterator<V> valueIterator = values.iterator();
        while (entryCount > 0) {
            map.put(keyIterator.next(), valueIterator.next());
            entryCount--;
        }

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
     * Map&lt;Object, Object&gt; colorMap = CollectionUtil.toMap(new String[][] {{
     *     {"RED", "#FF0000"},
     *     {"GREEN", "#00FF00"},
     *     {"BLUE", "#0000FF"}});
     * </pre>
     *
     * 参考：commons-lang
     *
     * @param array 数组。元素类型为Map.Entry、数组、Iterable、Iterator
     * @return {@link HashMap}

     * @see MapUtil#of(Object[])
     */
    public static HashMap<Object, Object> toMap(Object[] array) {
        return MapUtil.of(array);
    }

    /**
     * 将集合转换为排序后的TreeSet
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> TreeSet<T> toTreeSet(Collection<T> collection, Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        return treeSet;
    }



    /**
     * {@link Iterable}转为{@link Collection}<br>
     * 首先尝试强转，强转失败则构建一个新的{@link ArrayList}
     *
     * @param <E> 集合元素类型
     * @param iterable {@link Iterable}
     * @return {@link Collection} 或者 {@link ArrayList}

     */
    public static <E> Collection<E> toCollection(Iterable<E> iterable) {
        return (iterable instanceof Collection) ? (Collection<E>) iterable : newArrayList(iterable.iterator());
    }




    /**
     * 加入全部
     *
     * @param <T> 集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterator 要加入的{@link Iterator}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterator<T> iterator) {
        if (null != collection && null != iterator) {
            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T> 集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterable 要加入的内容{@link Iterable}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterable<T> iterable) {
        return addAll(collection, iterable.iterator());
    }

    /**
     * 加入全部
     *
     * @param <T> 集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param enumeration 要加入的内容{@link Enumeration}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Enumeration<T> enumeration) {
        if (null != collection && null != enumeration) {
            while (enumeration.hasMoreElements()) {
                collection.add(enumeration.nextElement());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T> 集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param values 要加入的内容数组
     * @return 原集合

     */
    public static <T> Collection<T> addAll(Collection<T> collection, T[] values) {
        if (null != collection && null != values) {
            Collections.addAll(collection, values);
        }
        return collection;
    }

    /**
     * 将另一个列表中的元素加入到列表中，如果列表中已经存在此元素则忽略之
     *
     * @param <T> 集合元素类型
     * @param list 列表
     * @param otherList 其它列表
     * @return 此列表
     */
    public static <T> List<T> addAllIfNotContains(List<T> list, List<T> otherList) {
        for (T t : otherList) {
            if (false == list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
    /**
     * 获取集合中指定下标的元素值，下标可以为负数，例如-1表示最后一个元素
     *
     * @param <T> 元素类型
     * @param collection 集合
     * @param index 下标，支持负数
     * @return 元素值
     */
    public static <T> T get(Collection<T> collection, int index) {
        if (null == collection) {
            return null;
        }

        final int size = collection.size();
        if (0 == size) {
            return null;
        }

        if (index < 0) {
            index += size;
        }

        // 检查越界
        if (index >= size) {
            return null;
        }

        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            return list.get(index);
        } else {
            int i = 0;
            for (T t : collection) {
                if (i > index) {
                    break;
                } else if (i == index) {
                    return t;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * 获取集合中指定多个下标的元素值，下标可以为负数，例如-1表示最后一个元素
     *
     * @param <T> 元素类型
     * @param collection 集合
     * @param indexes 下标，支持负数
     * @return 元素值列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAny(Collection<T> collection, int... indexes) {
        final int size = collection.size();
        final ArrayList<T> result = new ArrayList<>();
        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            for (int index : indexes) {
                if(index < 0) {
                    index += size;
                }
                result.add(list.get(index));
            }
        } else {
            final Object[] array = collection.toArray();
            for (int index : indexes) {
                if(index < 0) {
                    index += size;
                }
                result.add((T)array[index]);
            }
        }
        return result;
    }


    /**
     * 获取集合的最后一个元素
     *
     * @param <T> 集合元素类型
     * @param collection {@link Collection}
     * @return 最后一个元素
     */
    public static <T> T getLast(Collection<T> collection) {
        return get(collection, -1);
    }


    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map {@link Map}
     * @param keys 键列表
     * @return 值列表

     */
    @SuppressWarnings("unchecked")
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, K... keys) {
        final ArrayList<V> values = new ArrayList<>();
        for (K k : keys) {
            values.add(map.get(k));
        }
        return values;
    }

    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map {@link Map}
     * @param keys 键列表
     * @return 值列表

     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterable<K> keys) {
        return valuesOfKeys(map, keys.iterator());
    }

    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map {@link Map}
     * @param keys 键列表
     * @return 值列表

     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterator<K> keys) {
        final ArrayList<V> values = new ArrayList<>();
        while (keys.hasNext()) {
            values.add(map.get(keys.next()));
        }
        return values;
    }

    // ------------------------------------------------------------------------------------------------- sort
    /**
     * 将多个集合排序并显示不同的段落（分页）<br>
     *
     * @param <T> 集合元素类型
     * @param pageNo 页码，从1开始计数，0和1效果相同
     * @param pageSize 每页的条目数
     * @param comparator 比较器
     * @param colls 集合数组
     * @return 分页后的段落内容
     */
    @SafeVarargs
    public static <T> List<T> sortPageAll(int pageNo, int pageSize, Comparator<T> comparator, Collection<T>... colls) {
        final List<T> list = new ArrayList<>(pageNo * pageSize);
        for (Collection<T> coll : colls) {
            list.addAll(coll);
        }
        if(null != comparator) {
            Collections.sort(list, comparator);
        }

        return page(pageNo, pageSize, list);
    }

    /**
     * 对指定List分页取值
     *
     * @param <T> 集合元素类型
     * @param pageNo 页码，从1开始计数，0和1效果相同
     * @param pageSize 每页的条目数
     * @param list 列表
     * @return 分页后的段落内容
     *
     */
    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        if(isEmpty(list)) {
            return new ArrayList<>(0);
        }

        int resultSize = list.size();
        // 每页条目数大于总数直接返回所有
        if (resultSize <= pageSize) {
            if(pageNo <=1) {
                return Collections.unmodifiableList(list);
            } else {
                // 越界直接返回空
                return new ArrayList<>(0);
            }
        }
        final int[] startEnd = PageUtil.transToStartEnd(pageNo, pageSize);
        if (startEnd[1] > resultSize) {
            startEnd[1] = resultSize;
        }
        return list.subList(startEnd[0], startEnd[1]);
    }

    /**
     * 排序集合，排序不会修改原集合
     *
     * @param <T> 集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<? super T> comparator) {
        List<T> list = new ArrayList<>(collection);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * 针对List排序，排序会修改原List
     *
     * @param <T> 元素类型
     * @param list 被排序的List
     * @param c {@link Comparator}
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(List<T> list, Comparator<? super T> c) {
        Collections.sort(list, c);
        return list;
    }

    /**
     * 排序Map
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map Map
     * @param comparator Entry比较器
     * @return {@link TreeMap}

     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
        final TreeMap<K, V> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param entryCollection Entry集合
     * @param comparator {@link Comparator}
     * @return {@link LinkedList}

     */
    public static <K, V> LinkedHashMap<K, V> sortToMap(Collection<Map.Entry<K, V>> entryCollection, Comparator<Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> list = new LinkedList<>(entryCollection);
        Collections.sort(list, comparator);

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map 被排序的Map
     * @param comparator {@link Comparator}
     * @return {@link LinkedList}

     */
    public static <K, V> LinkedHashMap<K, V> sortByEntry(Map<K, V> map, Comparator<Map.Entry<K, V>> comparator) {
        return sortToMap(map.entrySet(), comparator);
    }

    /**
     * 将Set排序（根据Entry的值）
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param collection 被排序的{@link Collection}
     * @return 排序后的Set
     */
    public static <K, V> List<Map.Entry<K, V>> sortEntryToList(Collection<Map.Entry<K, V>> collection) {
        List<Map.Entry<K, V>> list = new LinkedList<>(collection);
        Collections.sort(list, new Comparator<Map.Entry<K, V>>(){

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                V v1 = o1.getValue();
                V v2 = o2.getValue();

                if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                } else {
                    return v1.toString().compareTo(v2.toString());
                }
            }
        });
        return list;
    }

    // ------------------------------------------------------------------------------------------------- forEach

    /**
     * 循环遍历 {@link Iterator}，使用{@link Consumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T> 集合元素类型
     * @param iterator {@link Iterator}
     * @param consumer {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Iterator<T> iterator, Consumer<T> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(iterator.next(), index);
            index++;
        }
    }

    /**
     * 循环遍历 {@link Enumeration}，使用{@link Consumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T> 集合元素类型
     * @param enumeration {@link Enumeration}
     * @param consumer {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Enumeration<T> enumeration, Consumer<T> consumer) {
        int index = 0;
        while (enumeration.hasMoreElements()) {
            consumer.accept(enumeration.nextElement(), index);
            index++;
        }
    }

    /**
     * 循环遍历Map，使用{@link KVConsumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map {@link Map}
     * @param kvConsumer {@link KVConsumer} 遍历的每条数据处理器
     */
    public static <K, V> void forEach(Map<K, V> map, KVConsumer<K, V> kvConsumer) {
        int index = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            kvConsumer.accept(entry.getKey(), entry.getValue(), index);
            index++;
        }
    }
    /**
     * 分组，按照{@link Hash}接口定义的hash算法，集合中的元素放入hash值对应的子列表中
     *
     * @param collection 被分组的集合
     * @param hash Hash值算法，决定元素放在第几个分组的规则
     * @return 分组后的集合
     */
    public static <T> List<List<T>> group(Collection<T> collection, Hash<T> hash){
        final List<List<T>> result = new ArrayList<>();
        if(isEmpty(collection)) {
            return result;
        }
        if (null == hash) {
            // 默认hash算法，按照元素的hashCode分组
            hash = t -> (null == t) ? 0 : t.hashCode();
        }

        int index;
        List<T> subList;
        for (T t : collection) {
            index = hash.hash(t);
            if(result.size()-1 < index) {
                while(result.size()-1 < index) {
                    result.add(null);
                }
                result.set(index, newArrayList(t));
            }else {
                subList = result.get(index);
                if(null == subList) {
                    result.set(index, newArrayList(t));
                }else {
                    subList.add(t);
                }
            }
        }
        return result;
    }


    /**
     * 反序给定List，会在原List基础上直接修改
     *
     * @param list 被反转的List
     * @return 反转后的List
     */
    public static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }


    /**
     * 设置或增加元素。当index小于List的长度时，替换指定位置的值，否则在尾部追加
     *
     * @param list List列表
     * @param index 位置
     * @param element 新元素
     * @return 原List
     *
     */
    public static <T> List<T> setOrAppend(List<T> list, int index, T element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            list.add(element);
        }
        return list;
    }

	/**
	 * 获取指定Map列表中所有的Key
	 *
	 * @param <K> 键类型
	 * @param mapCollection Map列表
	 * @return key集合
	 *
	 */
	public static <K> Set<K> keySet(Collection<Map<K, ?>> mapCollection) {
		if (isEmpty(mapCollection)) {
			return new HashSet<>();
		}
		final HashSet<K> set = new HashSet<>(mapCollection.size() * 16);
		for (Map<K, ?> map : mapCollection) {
			set.addAll(map.keySet());
		}

		return set;
	}

	/**
	 * 获取指定Map列表中所有的Value
	 *
	 * @param <V> 值类型
	 * @param mapCollection Map列表
	 * @return Value集合
	 *
	 */
	public static <V> List<V> values(Collection<Map<?, V>> mapCollection) {
		final List<V> values = new ArrayList<>();
		for (Map<?, V> map : mapCollection) {
			values.addAll(map.values());
		}

		return values;
	}
    /**
     * 取最大值
     *
     * @param <T> 元素类型
     * @param coll 集合
     * @return 最大值
     * @see Collections#max(Collection)
     */
    public static <T extends Comparable<? super T>> T max(Collection<T> coll) {
        return Collections.max(coll);
    }

    /**
     * 取最大值
     *
     * @param <T> 元素类型
     * @param coll 集合
     * @return 最大值
     * @see Collections#min(Collection)
     */
    public static <T extends Comparable<? super T>> T min(Collection<T> coll) {
        return Collections.min(coll);
    }
	// ---------------------------------------------------------------------------------------------- Interface start
	/**
	 * 针对一个参数做相应的操作
	 *
	 *
	 *
	 * @param <T> 处理参数类型
	 */
	public  interface Consumer<T> {
		/**
		 * 接受并处理一个参数
		 *
		 * @param value 参数值
		 * @param index 参数在集合中的索引
		 */
		void accept(T value, int index);
	}

    /**
     * 针对两个参数做相应的操作，例如Map中的KEY和VALUE
     *

     *
     * @param <K> KEY类型
     * @param <V> VALUE类型
     */
    public  interface KVConsumer<K, V> {
        /**
         * 接受并处理一对参数
         *
         * @param key 键
         * @param value 值
         * @param index 参数在集合中的索引
         */
        void accept(K key, V value, int index);
    }

    /**
     * Hash计算接口
     *
     *
     * @param <T> 被计算hash的对象类型
     *
     */
    public  interface Hash<T> {
        /**
         * 计算Hash值
         * @param t 对象
         * @return hash
         */
        int hash(T t);
    }
}
