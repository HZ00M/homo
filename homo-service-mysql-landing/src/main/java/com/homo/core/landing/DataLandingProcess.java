package com.homo.core.landing;

import com.homo.core.facade.module.Module;
import com.homo.core.facade.storege.DataOpType;
import com.homo.core.facade.storege.dirty.DirtyHelper;
import com.homo.core.facade.storege.landing.DataObjHelper;
import com.homo.core.mysql.entity.DataObject;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.spring.GetBeanUtil;
import io.lettuce.core.KeyValue;
import lombok.extern.log4j.Log4j2;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.util.*;

;

@Log4j2
public class DataLandingProcess implements  Module {

    private static HomoAsyncRedisPool redisPool;

    @Override
    public void init(){
        redisPool = GetBeanUtil.getBean(HomoAsyncRedisPool.class);
    }

    public Map<String, List<DataObject>> processBatch(List<String> dataList) {
        return LandingContext.create(dataList)
                .queryUpdateKeys()
                .checkDelKeys()
                .build();
    }

    public Tuple2<String, DataObject> processOne(String queryKey, String option) {
        ArrayList<String> list = new ArrayList<>();
        String[] segmentation = DirtyHelper.splitQueryKey(queryKey);
        String addId = segmentation[0];
        String regionId = segmentation[1];
        String tableName = DataObjHelper.buildTableName(addId,regionId);

        list.add(queryKey);
        list.add(option);
        return LandingContext.create(list).queryUpdateKeys().checkDelKeys().buildSingle(tableName);
    }

    static class LandingContext {
        //需要从redis查询最新的update的值
        Map<String, List<String>> batchQueryUpdateMap = new HashMap<>();
        //需要从redis查询删除的值
        Map<String, List<String>> batchQueryDelMap = new HashMap<>();
        //准备落地同一个表中的多条数据list，key为表名，value为数据list
        Map<String, List<DataObject>> batchLandMap = new HashMap<>();

         private LandingContext(){}

        public static LandingContext create(List<String> dataList){
            LandingContext landingContext = new LandingContext();
            for(int i = 0; i < dataList.size(); i += 2){
                //获得这个key的操作类型
                String queryKey = dataList.get(i);
                String option = dataList.get(i + 1);
                landingContext.process(queryKey,option);
            }
            return landingContext;
        }

        public LandingContext process(String dirtyField,String option){
            if(option.equals(DataOpType.UPDATE.name())){
                buildUpdateQueryKey(dirtyField);
            }else if(option.equals(DataOpType.REMOVE.name())){
                buildDeleteQueryKey(dirtyField);
            }else{
                buildOneLandData(dirtyField,option.getBytes(StandardCharsets.UTF_8),0,0L);
            }
            return this;
        }

         /**
          * 根据脏表中的key去redis查询value
          */
         public LandingContext queryUpdateKeys() {
             for(Map.Entry<String, List<String>> entry: batchQueryUpdateMap.entrySet()){
                 List<String> keyList = entry.getValue();
                 String queryKey = entry.getKey();
                 //准备key
                 String redisTableName = DataObjHelper.buildTableName(queryKey);
                 //准备field
                 String[] fields = new String[keyList.size()];
                 for(int i = 0; i < keyList.size(); i++){
                     fields[i] = keyList.get(i);
                 }
                 List<KeyValue<String, byte[]>> result = redisPool.hmgetStringByte(redisTableName, fields);
                 for(KeyValue<String, byte[]> keyValue: result){
                     if(keyValue.hasValue()){
                         String field = keyValue.getKey();
                         String dirtyKey = DirtyHelper.buildDirtyKey(queryKey, field);
                         buildOneLandData(dirtyKey, keyValue.getValue(), 0, 0L);
                     }
                 }
             }
             return this;
         }

         public LandingContext checkDelKeys(){
             for(Map.Entry<String, List<String>> entry: batchQueryDelMap.entrySet()){
                 //准备key
                 String queryKey = entry.getKey();
                 String redisKey = DataObjHelper.buildTableName(queryKey);
                 //查询现网这个key是否存在
                 Set<String> exitKeys = queryUpdateByDelKey(entry, redisKey);
                 List<String> fileList = entry.getValue();
                 //如果获得的值够了，就跳过，不去找删除的值
                 if(exitKeys.size() >= fileList.size()){
                     continue;
                 }
                 //否则，说明有数据确实需要进行删除,将需要删除的数据找出来并删除
                 queryDelKey(exitKeys, entry, redisKey);
             }
             return this;
         }

         private void queryDelKey(Set<String> exitKeys, Map.Entry<String, List<String>> entry, String redisTableName){
             List<String> filedList = entry.getValue();
             String queryKey = entry.getKey();
             String[] fields = new String[filedList.size() - exitKeys.size()];
             int index = 0;
             for (int i = 0; i < filedList.size(); i++) {
                 if(!exitKeys.contains(filedList.get(i))){
                     //需要删除的数据会带上删除后缀，需要加上后缀去查找
                     fields[index++] = filedList.get(i) + DirtyHelper.DEL_SUFFIX;
                 }
             }
             List<KeyValue<String, byte[]>> delResult = redisPool.hmgetStringByte(redisTableName, fields);
             for(KeyValue<String, byte[]> stringKeyValue: delResult){
                 if(stringKeyValue.hasValue()){
                     String fieldWithSuffix = stringKeyValue.getKey();
                     String dirtyField = fieldWithSuffix.replace(DirtyHelper.DEL_SUFFIX, "");//将后缀删除即得到原始的field
                     String dirtyKey = DirtyHelper.buildDirtyKey(queryKey,dirtyField);
                     buildOneLandData(dirtyKey, stringKeyValue.getValue(), 1, System.currentTimeMillis());
                 }
             }
         }

         private Set<String> queryUpdateByDelKey(Map.Entry<String, List<String>> entry,  String redisTableName){
             String queryKey = entry.getKey();
             String[] keys = {redisTableName};
             //准备field
             List<String> filedList = entry.getValue();
             byte[][] args = new byte[filedList.size()][];
             for (int i = 0; i < filedList.size(); i++) {
                 args[i] = filedList.get(i).getBytes(StandardCharsets.UTF_8);
             }
             Object result = redisPool.eval(LuaScriptHelper.queryExistFieldsScript, keys, args);
             Set<String> existFields = new HashSet<>();
             if(!Collections.emptyList().equals(result)){
                 ArrayList<byte[]> keyValue = (ArrayList<byte[]>)result;
                 for(int i = 0; i < keyValue.size(); i += 2){
                     String field = new String(keyValue.get(i), StandardCharsets.UTF_8);
                     String dirtyField = DirtyHelper.buildDirtyKey(queryKey, field);
                     byte[] value = keyValue.get(i + 1);
                     buildOneLandData(dirtyField, value, 0, 0L);//删除后用户又重新创建了新的数据，需要重新落地而不进行删除
                     existFields.add(field);
                 }
             }
             return existFields;
         }

        public Map<String, List<DataObject>> build(){
            return batchLandMap;
        }

        public  Tuple2<String, DataObject> buildSingle(String tableName){
            return Tuples.of(tableName,batchLandMap.get(tableName).get(0));
        }

        private void buildUpdateQueryKey(String queryKey){
            String[] segmentation = DirtyHelper.splitQueryKey(queryKey);
            batchQueryUpdateMap.computeIfAbsent(queryKey, key->new ArrayList<>()).add(segmentation[4]);
        }

         private void buildDeleteQueryKey(String queryKey){
             String[] segmentation = DirtyHelper.splitQueryKey(queryKey);
             batchQueryDelMap.computeIfAbsent(queryKey, key->new ArrayList<>()).add(segmentation[4]);
         }

        public Tuple2<String,DataObject> buildOneLandData(String queryKey, byte[] value, Integer isDel, Long delTime){
            String[] segmentation = DirtyHelper.splitQueryKey(queryKey);
            String appId = segmentation[0];
            String regionId = segmentation[1];
            String logicType = segmentation[2];
            String ownerId = segmentation[3];
            String key = segmentation[4];

            String primaryKey = DataObjHelper.buildPrimaryKey(logicType, ownerId, key);
            String queryAllKey = DataObjHelper.buildQueryAllKey(logicType, ownerId);
            DataObject data = DataObject.builder()
                    .primaryKey(primaryKey)
                    .logicType(logicType)
                    .ownerId(ownerId)
                    .key(key)
                    .value(value)
                    .isDel(isDel)
                    .delTime(delTime)
                    .queryAllKey(queryAllKey).build();
            String tableName = DataObjHelper.buildTableName(appId, regionId);
            batchLandMap.computeIfAbsent(tableName, s->new ArrayList<>()).add(data);
            return Tuples.of(tableName,data);
        }
    }

}
