package com.homo.core.landing;

import com.homo.concurrent.thread.ThreadPoolFactory;
import com.homo.core.facade.storege.landing.DBDataHolder;
import com.homo.core.facade.storege.landing.DataObjHelper;
import com.homo.core.landing.mapper.ISchemeMapper;
import com.homo.core.mysql.entity.DataObject;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.factory.RedisInfoHolder;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

;

/**
 * 缓存mysql数据到redis处理器
 */
@Log4j2
public class MysqlLoadDataHolder implements DBDataHolder<DataObject> {
    Map<String, Boolean> tableTags = new ConcurrentHashMap<>();
    @Autowired(required = false)
    @Qualifier("homoRedisPool")
    private HomoRedisPool redisPool;
    @Autowired(required = false)
    private RedisInfoHolder redisInfoHolder;
    @Autowired(required = false)
    private ISchemeMapper schemeMapper;
    @Autowired(required = false)
    private DataLandingProcess dataLandingProcess;


    //SynchronousQueue的一个使用场景是在线程池里。Executors.newCachedThreadPool()就使用了SynchronousQueue，
    // 这个线程池根据需要（新任务到来时）创建新的线程，如果有空闲线程则会重复使用，线程空闲了60秒后会被回收。
    final ExecutorService executorService = Executors.newCachedThreadPool(ThreadPoolFactory.newThreadFactory("MysqlLoadDataHolder-Thread"));

    public Homo<Boolean> hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey) {
        log.trace("hotAllField begin appId_{} regionId_{} logicType_{} ownerId_{} redisKey_{}", appId, regionId, logicType, ownerId, redisKey);
        String tableName = DataObject.buildTableName(appId, regionId);
        if (checkTableNotExist(tableName)) {
            schemeMapper.create(DataObject.class, tableName);
            mark(tableName);
        }
        Sinks.One<Boolean> one = Sinks.one();
        Homo<Boolean> homo = new Homo<Boolean>(one.asMono());
        executorService.execute(() -> {
            try {
                List<DataObject> fieldList = schemeMapper.loadAllDataObject(appId, regionId, logicType, ownerId);
                log.trace("hotAllField process appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());
                byte[] hotAllFieldScript = LuaScriptHelper.hotAllFieldScript.getBytes(StandardCharsets.UTF_8);
                List<byte[]> keys = new ArrayList<>();
                keys.add(redisKey.getBytes(StandardCharsets.UTF_8));
                List<byte[]> args = new ArrayList<>();//expireTime + fieldList
                args.add(redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8));
                for (DataObject dataObject : fieldList) {
                    args.add(dataObject.getKey().getBytes(StandardCharsets.UTF_8));
                    args.add(dataObject.getValue());
                }
                Object ret = redisPool.eval(hotAllFieldScript, keys, args);
                log.trace("hotAllField complete appId_{} regionId_{} logicType_{} ownerId_{} list size is {} ret {}", appId, regionId, logicType, ownerId, fieldList.size(),ret);
                one.tryEmitValue(true);
            } catch (Exception e) {
                 one.emitError(e,(signalType, emitResult) -> true);
            }
        });
        return homo;
    }

    public Homo<List<DataObject>> hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields) {
        log.trace("hotFields begin appId_{} regionId_{} logicType_{} ownerId_{} redisKey_{}", appId, regionId, logicType, ownerId, redisKey);
        String tableName = DataObject.buildTableName(appId, regionId);
        if (checkTableNotExist(tableName)) {
            schemeMapper.create(DataObject.class, tableName);
            mark(tableName);
        }
        Sinks.One<List<DataObject>> one = Sinks.one();
        Homo<List<DataObject>> homo = new Homo<List<DataObject>>(one.asMono());
        executorService.execute(() -> {
            try {
                List<DataObject> fieldList = schemeMapper.loadDataObjectsByField(appId, regionId, logicType, ownerId, fields);
                log.trace("hotFields process appId_{} regionId_{} logicType_{} ownerId_{} list size is {}", appId, regionId, logicType, ownerId, fieldList.size());
                byte[] hotFieldsScript = LuaScriptHelper.hotFieldsScript.getBytes(StandardCharsets.UTF_8);
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
                one.tryEmitValue(fieldList);
            } catch (Exception e) {
                one.emitError(e,(signalType, emitResult) -> true);
            }
        });
        return homo;
    }

    @Override
    public boolean batchLanding(String dirtyTableName, List<String> dirtyList) {
        try {
            // 获取需要落地的数据，这里是否有必要加锁待定
            Map<String, List<DataObject>> dataMap = dataLandingProcess.processBatch(dirtyList);
            // 遍历所有数据
            for (Map.Entry<String, List<DataObject>> saveData : dataMap.entrySet()) {
                if (checkTableNotExist(saveData.getKey())) {
                    // 如果不存在就尝试创建
                    schemeMapper.create(DataObject.class,saveData.getKey());
                    // 标记已经存在
                    mark(saveData.getKey());
                }
                Integer size = saveData.getValue().size();
                // 开始保存
                log.info("begin save [{}] size[{}] >>>>", saveData.getKey(), size);
                saveData.getValue().forEach(dataObject ->
                        log.info("data: getLogicType [{}] getOwnerId [{}] getKey [{}] getIsDel [{}] valueSize [{}]",
                                dataObject.getLogicType(),
                                dataObject.getOwnerId(),
                                dataObject.getKey(),
                                dataObject.getIsDel(),
                                dataObject.getValue() != null ? dataObject.getValue().length : 0
                        ));
                log.info("do save to mysql [{}] size[{}] >>>>", saveData.getKey(), size);
                Integer result = schemeMapper.batchUpdate(saveData.getKey(), saveData.getValue());
                log.info("[{}] batchUpdate result is [{}]/[{}]", saveData.getKey(), result, size);
                if(result <= 0){
                    // 没有保存成功
                    log.error("[{}] batchUpdate error! [{}]", saveData.getKey(), result);
                    return false;
                }
            }
            return true;
        }catch (Throwable e){
            log.error("batchLanding is error ", e);
            return false;
        }
    }

    @Override
    public boolean singleLanding(List<String> dirtyList, String dirtyName) {
        // 获取需要落地的数据，这里是否有必要加锁待定
        for(int i = 0; i < dirtyList.size(); i+=2){
            String queryKey = dirtyList.get(i);
            String option = dirtyList.get(i + 1);
            try {
                log.info("begin build data pair dirtyKey is {} option is {}", queryKey, option);
                Tuple2<String, DataObject> result = dataLandingProcess.processOne(queryKey, option);
                log.info("end build data pair dirtyKey is {} option is {}", queryKey, option);
                String tableName = result.getT1();
                DataObject dataObject = result.getT2();
                if (checkTableNotExist(tableName)) {
                    log.info("NotExist Table {}", tableName);
                    schemeMapper.create(DataObject.class,tableName);
                    mark(tableName);
                }
                log.info("begin save {} >>>> {}", tableName, dataObject);
                int executeResult = schemeMapper.add(dataObject,tableName);
                log.info("{} single Update result is {}", tableName, result);
                if(executeResult<=0){
                    log.error("{} {} execute result is false", tableName, dataObject);
                    throw new Exception("execute result is false");
                }
            }catch (Throwable e){
                //保存错误的key
                String errorTableName = DataObjHelper.buildErrorTableName(dirtyName);
                redisPool.hset(errorTableName, queryKey, option);
                log.error("dirtyKey is {} option is {} is exception", queryKey, option, e);
            }
        }
        return true;
    }


    public boolean checkTableNotExist(String tableName) {
        return !tableTags.containsKey(tableName);
    }

    public void mark(String tableName) {
        tableTags.put(tableName, true);
    }
}
