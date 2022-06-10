package com.homo.core.persisten.handler;

import com.homo.core.common.pojo.DataObject;
import com.homo.core.mysql.annotation.SQLGen;
import com.homo.core.persisten.mapper.ISchemeMapper;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.factory.RedisInfoHolder;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.callback.CallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存mysql数据到redis处理器
 */
@Component
@Slf4j
public class LoadDataHolder {
    Map<String, Boolean> tableTags = new ConcurrentHashMap<>();
    @Autowired
    private HomoAsyncRedisPool redisPool;
    @Autowired
    private LuaScriptHelper luaScriptHelper;
    @Autowired
    private RedisInfoHolder redisInfoHolder;
    @Autowired
    private ISchemeMapper iSchemeMapper;

    //SynchronousQueue的一个使用场景是在线程池里。Executors.newCachedThreadPool()就使用了SynchronousQueue，
    // 这个线程池根据需要（新任务到来时）创建新的线程，如果有空闲线程则会重复使用，线程空闲了60秒后会被回收。
    final ExecutorService executorService = Executors.newCachedThreadPool();

    public void hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey, CallBack<Boolean> callBack) {
        log.trace("hotAllField begin appId_{} regionId_{} logicType_{} ownerId_{} redisKey_{}", appId, regionId, logicType, ownerId, redisKey);
        String tableName = DataObject.buildTableName(appId, regionId);
        if (checkTableNotExist(tableName)) {
            SQLGen.create(DataObject.builder().build(), tableName);
            mark(tableName);
        }
        executorService.execute(() -> {
            try {
                List<DataObject> fieldList = iSchemeMapper.loadAllDataObject(appId, regionId, logicType, ownerId);
                log.trace("hotAllField process appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());
                byte[] hotAllFieldScript = luaScriptHelper.getHotAllFieldScript().getBytes(StandardCharsets.UTF_8);
                List<byte[]> keys = new ArrayList<>();
                keys.add(redisKey.getBytes(StandardCharsets.UTF_8));
                List<byte[]> args = new ArrayList<>();//expireTime + fieldList
                args.add(redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8));
                for (DataObject dataObject : fieldList) {
                    args.add(dataObject.getKey().getBytes(StandardCharsets.UTF_8));
                    args.add(dataObject.getValue());
                }
                redisPool.eval(hotAllFieldScript, keys, args);
                log.trace("hotAllField complete appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());

            } catch (Exception e) {
                callBack.onError(e);
            }
        });
    }

    public void hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields, CallBack<List<DataObject>> callBack) {
        log.trace("hotFields begin appId_{} regionId_{} logicType_{} ownerId_{} redisKey_{}", appId, regionId, logicType, ownerId, redisKey);
        String tableName = DataObject.buildTableName(appId, regionId);
        if (checkTableNotExist(tableName)) {
            SQLGen.create(DataObject.builder().build(), tableName);
            mark(tableName);
        }
        executorService.execute(() -> {
            try {
                List<DataObject> fieldList = iSchemeMapper.loadDataObjectsByField(appId, regionId, logicType, ownerId, fields);
                log.trace("hotFields process appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());
                byte[] hotFieldsScript = luaScriptHelper.getHotFieldsScript().getBytes(StandardCharsets.UTF_8);
                List<byte[]> keys = new ArrayList<>();
                keys.add(redisKey.getBytes(StandardCharsets.UTF_8));
                List<byte[]> args = new ArrayList<>();//expireTime + fieldList
                args.add(redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8));
                for (DataObject dataObject : fieldList) {
                    args.add(dataObject.getKey().getBytes(StandardCharsets.UTF_8));
                    args.add(dataObject.getValue());
                }
                redisPool.eval(hotFieldsScript, keys, args);
                log.trace("hotFields complete appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());
            } catch (Exception e) {
                callBack.onError(e);
            }
        });
    }

    public boolean checkTableNotExist(String tableName) {
        return !tableTags.containsKey(tableName);
    }

    public void mark(String tableName) {
        tableTags.put(tableName, true);
    }
}
