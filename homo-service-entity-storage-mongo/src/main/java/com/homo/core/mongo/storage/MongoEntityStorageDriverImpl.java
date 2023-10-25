package com.homo.core.mongo.storage;

import com.homo.core.facade.document.Document;
import com.homo.core.facade.document.EntityStorageDriver;
import com.homo.core.mongo.util.BsonUtil;
import com.homo.core.mongo.util.Key;
import com.homo.core.mongo.util.MongoHelper;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.rector.Homo;
import com.mongodb.client.model.*;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

;

/**
 * 文档存储驱动层 由mongoDB实现
 */
@Slf4j
public class MongoEntityStorageDriverImpl implements EntityStorageDriver<Bson,Bson,Bson,List<Bson>> {
    @Autowired
    private MongoHelper mongoHelper;

    @Override
    public <T> void asyncQuery(Bson filter, Bson sort, Integer limit, Integer skip, Class<T> clazz, CallBack<List<T>> callBack) {
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            Bson filterExpr;
            if (filter != null) {
                filterExpr = Filters.and(filter,Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            } else {
                filterExpr = Filters.and(Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            }
            Bson sortExpr;
            if (sort != null) {
                sortExpr = sort;
            } else {
                sortExpr = Sorts.ascending("_id");
            }
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                    .find(filterExpr)
                    .limit(Optional.ofNullable(limit).orElse(100))
                    .skip(Optional.ofNullable(skip).orElse(0))
                    .sort(sortExpr))
                    .collectList()
                    .subscribe(ret -> {
                        List<T> result = ret.stream().map(obj-> BsonUtil.toBean(obj.get("value", org.bson.Document.class),clazz)).collect(Collectors.toList());
                        callBack.onBack(result);
                    }, callBack::onError);
        } catch (Exception e) {
            log.error("asyncQuery catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T, V> void asyncQuery(Bson filter, Bson viewFilter, Bson sort, @NotNull Integer limit, Integer skip, Class<V> viewClazz, Class<T> clazz, CallBack<List<V>> callBack) {
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            Bson filterExpr;
            if (filter != null) {
                filterExpr = Filters.and(filter, Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            } else {
                filterExpr = Filters.and(Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            }
            Bson viewFilterExpr;
            if (filter != null) {
                viewFilterExpr = viewFilter;
            } else {
                viewFilterExpr = new BsonDocument();
            }
            Bson sortExpr;
            if (sort != null) {
                sortExpr = sort;
            } else {
                sortExpr = Sorts.ascending("_id");
            }
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                    .find(filterExpr)
                    .projection(viewFilterExpr)
                    .limit(Optional.ofNullable(limit).orElse(100))
                    .skip(Optional.ofNullable(skip).orElse(0))
                    .sort(sortExpr))
                    .collectList()
                    .subscribe(ret -> {
                        List<V> result = ret.stream().map(obj -> {
                            org.bson.Document valueDocument = obj.get("value", org.bson.Document.class);
                            obj.remove("_id");
                            obj.remove(Key.PRIMARY_KEY);
                            obj.remove(Key.QUERY_ALL_KEY);
                            obj.remove(Key.DELETE_KEY);
                            obj.remove("value");
                            for (String key : obj.keySet()) {
                                valueDocument.append(key, obj.get(key));
                            }
                            return BsonUtil.toBean(valueDocument, viewClazz);
                        }).collect(Collectors.toList());
                        callBack.onBack(result);
                    }, callBack::onError);
        } catch (Exception e) {
            log.error("asyncQuery catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void asyncFindAndModify(String logicType, String ownerId, String key, Bson filter, Bson update, Class<T> clazz, CallBack<Boolean> callBack) {
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            Bson filterExpr;
            String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
            if (filter != null) {
                filterExpr = Filters.and(Filters.eq(Key.PRIMARY_KEY, primaryValue),Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE), filter);
            } else {
                filterExpr = Filters.and(Filters.eq(Key.PRIMARY_KEY, primaryValue),Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            }
            List<Bson> updateModules = new ArrayList<>();
            updateModules.add(Updates.set(Key.KEY_KEY, key));
            updateModules.add(Updates.set(Key.DELETE_KEY, Key.DELETED_FALSE));
            updateModules.add(Updates.set(Key.QUERY_ALL_KEY, Key.getQueryAllValue(logicType, ownerId)));
            updateModules.add(update);
            mongoHelper.getMongoDatabase().getCollection(collectionName)
                    .findOneAndUpdate(filterExpr, Updates.combine(updateModules),new FindOneAndUpdateOptions().upsert(true))
                    .subscribe(new Subscriber<org.bson.Document>() {
                        @Override
                        public void onSubscribe(Subscription subscription) {
                            subscription.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(org.bson.Document document) {
                            log.info("asyncFindAndModify result_{}", document.toJson());
                            callBack.onBack(true);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.error("asyncFindAndModify error !", throwable);
                            callBack.onError(throwable);
                        }

                        @Override
                        public void onComplete() {
                            log.info("asyncFindAndModify Complete");
                        }
                    });
        } catch (Exception e) {
            log.error("asyncFindAndModify catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T, V> void asyncAggregate(List<Bson> pipeLine, Class<V> viewClazz, Class<T> clazz, CallBack<List<V>> callBack) {
        Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
        String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
        mongoHelper.checkIndex(clazz);
        List<org.bson.Document> resultList = new ArrayList<>();
        try {
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                    .aggregate(pipeLine))
                    .subscribe(new Subscriber<org.bson.Document>() {
                        @Override
                        public void onSubscribe(Subscription subscription) {
                            subscription.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(org.bson.Document result) {
                            log.info("asyncAggregate result_{}", document.toString());
                            resultList.add(result);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.error("asyncAggregate error !", throwable);
                            callBack.onError(throwable);
                        }

                        @Override
                        public void onComplete() {
                            List<V> ts = BsonUtil.toBeans(resultList, viewClazz);
                            callBack.onBack(ts);
                            log.info("asyncAggregate Complete");
                        }
                    });
        } catch (Exception e) {
            log.error("asyncAggregate catch Exception ", e);
            callBack.onError(e);
        }
    }


    @Override
    public <T> void asyncGetAll(String appId, String regionId, String logicType, String ownerId, Class<T> clazz, CallBack<Map<String, T>> callBack) {
        log.info("getAllKeysAndVal, appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        try {
            MongoDatabase mongoDatabase = mongoHelper.getMongoDatabase();
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            String queryAllValue = Key.getQueryAllValue(logicType, ownerId);
            mongoHelper.checkIndex(clazz);
            Flux.from(mongoDatabase.getCollection(collectionName)
                    .find(Filters.and(Filters.eq(Key.QUERY_ALL_KEY, queryAllValue), Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE))))
                    .collectMap(doc -> doc.getString(Key.KEY_KEY), doc -> BsonUtil.toBean((org.bson.Document) doc.get(Key.VALUE_KEY), clazz))
                    .subscribe(callBack::onBack, callBack::onError);
        } catch (Exception e) {
            log.error("getAllKeysAndVal catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, T> data, Class<T> clazz, CallBack<Pair<Boolean, Map<String, T>>> callBack) {
        log.info("update, appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            List<WriteModel<org.bson.Document>> updates = new ArrayList<>();
            for (Map.Entry<String, T> dataEntry : data.entrySet()) {
                String key = dataEntry.getKey();
                String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
                UpdateOneModel updateModule = new UpdateOneModel<org.bson.Document>(Filters.eq(Key.PRIMARY_KEY, primaryValue),
                        mongoHelper.getDefaultUpdateBson(primaryValue, key, BsonUtil.toDocument(dataEntry.getValue()), logicType, ownerId),
                        new UpdateOptions().upsert(true));
                updates.add(updateModule);
            }
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName).bulkWrite(updates, new BulkWriteOptions().ordered(false)))
                    .subscribe(bulkWriteResult -> callBack.onBack(new ImmutablePair<>(bulkWriteResult.wasAcknowledged(), new HashMap<>())),
                            callBack::onError);
        } catch (Exception e) {
            log.error("update catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void asyncUpdatePartial(String appId, String regionId, String logicType, String ownerId, String key, Map<String, ?> data, Class<T> clazz, CallBack<Boolean> callBack) {
        log.info("updatePartial, appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            List<WriteModel<org.bson.Document>> updates = new ArrayList<>();
            List<Bson> updateModules = new ArrayList<>();
            String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
            for (Map.Entry<String, ?> dataEntry : data.entrySet()) {
                String filedName = dataEntry.getKey();
                Object value = dataEntry.getValue();
                updateModules.add(Updates.set(filedName, value));
            }
            updateModules.add(Updates.set(Key.KEY_KEY, key));
            updateModules.add(Updates.set(Key.DELETE_KEY, Key.DELETED_FALSE));
            updateModules.add(Updates.set(Key.QUERY_ALL_KEY, Key.getQueryAllValue(logicType, ownerId)));
            UpdateOneModel updateModule = new UpdateOneModel<org.bson.Document>(Filters.eq(Key.PRIMARY_KEY, primaryValue),
                    Updates.combine(updateModules),
                    new UpdateOptions().upsert(true));
            updates.add(updateModule);
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName).bulkWrite(updates, new BulkWriteOptions().ordered(false)))
                    .subscribe(bulkWriteResult -> callBack.onBack(bulkWriteResult.wasAcknowledged()),
                            callBack::onError);
        } catch (Exception e) {
            log.error("updatePartial catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, Class<T> clazz, CallBack<Map<String, T>> callBack) {
        log.info("asyncGet, appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            //获取filter
            Bson[] filters = new Bson[keyList.size()];
            int index = 0;
            for (String key : keyList) {
                String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
                //找到主key相同且没被删除的document
                Bson filter = Filters.and(Filters.eq(Key.PRIMARY_KEY, primaryValue), Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
                filters[index++] = filter;
            }
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                    .find(Filters.or(filters)))
                    .collectMap(doc -> doc.getString(Key.KEY_KEY), doc -> BsonUtil.toBean((org.bson.Document) doc.get(Key.VALUE_KEY), clazz))
                    .subscribe(callBack::onBack, callBack::onError);
        } catch (Exception e) {
            log.error("asyncGet catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void asyncIncr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz, CallBack<Pair<Boolean, Map<String, Long>>> callBack) {
        try {
            log.info("asyncIncr appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(true);
            Flux.fromIterable(incrData.entrySet())
                    .flatMap(dataEntry -> {
                        String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
                        String filedName = dataEntry.getKey();
                        Long value = dataEntry.getValue();
                        Bson incr = Updates.combine(Updates.set(Key.PRIMARY_KEY, primaryValue),
                                Updates.set(Key.KEY_KEY, key),
                                Updates.inc(filedName, value),
                                Updates.set(Key.DELETE_KEY, Key.DELETED_FALSE),
                                Updates.set(Key.QUERY_ALL_KEY, Key.getQueryAllValue(logicType, ownerId)));
                        return Mono.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                                .findOneAndUpdate(Filters.eq(Key.PRIMARY_KEY, primaryValue), incr, options)
                        ).map(doc-> new ImmutablePair<>(dataEntry.getKey(),getEmbeddedValue(doc,dataEntry.getKey())));
                    })
                    .collectMap(ImmutablePair::getLeft, ImmutablePair::getRight)
                    .subscribe(relMap -> callBack.onBack(new ImmutablePair<>(true, relMap)), callBack::onError);
        } catch (Exception e) {
            log.error("asyncIncr catch Exception ", e);
            callBack.onError(e);
        }
    }

    private Long getEmbeddedValue(org.bson.Document doc, String key){
        String[] keys = key.split("\\.");
        for (int i  =0 ; i < keys.length; i ++){
            if(i == (keys.length - 1)){
                return doc.getLong(keys[i]);
            }else{
                doc = doc.get(keys[i], org.bson.Document.class);
            }
        }
        throw new RuntimeException("should not reach here!");
    }

    @Override
    public <T> void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, Class<T> clazz, CallBack<Boolean> callBack) {
        log.info("asyncRemoveKeys ppId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            List<WriteModel<org.bson.Document>> updates = new ArrayList<>();
            for (String key : remKeys) {
                String primaryValue = Key.getPrimaryValue(logicType, ownerId, key);
                UpdateOneModel updateModule = new UpdateOneModel<org.bson.Document>(Filters.eq(Key.PRIMARY_KEY, primaryValue),
                        Updates.set(Key.DELETE_KEY, Key.DELETED_TRUE));
                updates.add(updateModule);
            }
            Flux.from(mongoHelper.getMongoDatabase().getCollection(collectionName).bulkWrite(updates, new BulkWriteOptions().ordered(false)))
                    .subscribe(bulkWriteResult -> callBack.onBack(bulkWriteResult.wasAcknowledged()), callBack::onError);
        } catch (Exception e) {
            log.error("asyncRemoveKeys catch Exception ", e);
            callBack.onError(e);
        }
    }

    @Override
    public <T> void getCount(Bson filter, int limit, int skip,String hint, Class<T> clazz, CallBack<Long> rel) {
        try {
            Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
            String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
            mongoHelper.checkIndex(clazz);
            Bson filterExpr;
            if (filter != null) {
                filterExpr = Filters.and(filter, Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            } else {
                filterExpr = Filters.and(Filters.eq(Key.DELETE_KEY, Key.DELETED_FALSE));
            }
            CountOptions options = new CountOptions().limit(limit).skip(skip).hintString(hint).maxTime(10, TimeUnit.SECONDS);
            Homo.warp(
                    Mono.from(mongoHelper.getMongoDatabase().getCollection(collectionName)
                            .countDocuments(filterExpr,options)))
                    .consumerValue(rel::onBack)
                    .catchError(rel::onError)
                    .start();
        } catch (Exception e) {
            log.error("getCount catch Exception ", e);
            rel.onError(e);
        }
    }
}
