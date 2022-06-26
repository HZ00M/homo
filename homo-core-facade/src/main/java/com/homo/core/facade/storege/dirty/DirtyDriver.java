package com.homo.core.facade.storege.dirty;


import com.homo.core.utils.rector.Homo;

import java.util.List;

/**
 * 设计思路
 * 1 脏表逻辑
 *   当使用storage进行更新(更新包括增加、更新、自增、删除)操作时，将hset对应的filed的key保存在一张待更新的dirtyMap（hset结构中）中，
 *   这个hset结构需要有多个，用户的更新操作按照一定规则（按照key进行hash）随机存储在任一个脏表结构中（dirtyMap）,等待落地程序执行
 *   落地操作（PersistentDriver负责根据脏表的持久化操作）


 /**
 * 2 落地逻辑
 *   一个落地程序只负责一定数量的脏表（dirtyMap）的持久化操作，在执行持久化前会判断dirtyMap是否已经有其他落地程序负责
 *   在获取脏表的持久化权限后，起一个定时程序进行定时落地操作，落地前会将当前脏表进行重命名操作（相当于快照操作，不与正在更新
 *   的资源同时操作同一个hset），然后每次落地会按照配置的落地数去该脏表执行Hscan指定数量的key,
 *   然后根据这些key去redis中拿到对应的数据，构造出批量更新的sql，然后执行sql进行落地，
 *   批量落地如果失败了。切换成单个更新模式，单个更新失败了，切换成失败模式（将失败的key存在另一个errorMap的异常表中hset）
 *   循环执行上述落地流程
 */
public interface DirtyDriver {
    /**
     * hset操作,存脏表数据
     */
    Homo<Long> dirtyUpdate(Dirty dirty);

    String chooseDirtyMap() throws InterruptedException;

    Boolean lockDirtyMap(String dirtyName);

    /**
     * 迭代获得脏表的数据
     * @param key
     * @param index
     * @param count
     * @return
     */
    List getDirtyList(String key, String index, String count);

    /**
     * 修改脏表的名字
     * @param dirtyName
     * @return
     */
    String snapShot(String dirtyName);


    boolean landing(String dirtyName, String dirtySaving);
}
