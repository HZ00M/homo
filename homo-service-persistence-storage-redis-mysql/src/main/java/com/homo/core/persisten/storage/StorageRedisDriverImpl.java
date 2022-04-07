package com.homo.core.persisten.storage;

import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.lang.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 设计思路
 * 1数据会按照指定格式进行存储到hSet中，该hSet称之为ownerKey,其中的filed称之为logicKey,
 *      存储流程：先存redis，再由另一台落地程序定时将redis上的数据落地到mysql
 *      存储规则： 当调用update或incr更新数据时，会在redis上打个不过期的string类型标签，标志该key
 *      存在在mysql（persistenceKey）persistenceKey用于判断数据是否存在于mysql
 * 2当调用get方法获取数据时，如果redis没有数据，会通过persistenceKey判定mysql是否存在该数据，
 * 3如果存在persistenceKey,就进行数据加热操作（hotkey），不存在则直接返回
 * 4从mysql获取到数据后，会将其存入到redis中（数据结构是hSet），然后重新执行get方法从redis获取数据.
 *  与此同时会在数据结构上增加一个field标识(成员名为cacheKey)，cacheKey用于判断数据是否存在于redis，存在才会从redis获取数据
 *  如果cacheKey存在，就从redis捞取后返回，如果不存在，则执行步骤3
 * 5数据移除会将需要移除的数据迁移到hSet的另一个字段上（logicKey+del标识）（逻辑删除）,然后原先的logicKey的值会被打上删除标记（del标识）
 */
@Slf4j
public class StorageRedisDriverImpl implements StorageDriver {
    @Override
    public void asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, CallBack<Map<String, byte[]>> callBack) {

    }

    @Override
    public void asyncGetAll(String appId, String regionId, String logicType, String ownerId, CallBack<Map<String, byte[]>> callBack) {

    }

    @Override
    public void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, CallBack<Pair<Boolean, Map<String, byte[]>>> callBack) {

    }

    @Override
    public void asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData, CallBack<Pair<Boolean, Map<String, Long>>> callBack) {

    }

    @Override
    public void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, CallBack<Boolean> callBack) {

    }
}
