package com.homo.core.mongo.util;

import com.homo.core.configurable.mongo.MongoDriverProperties;
import com.homo.core.facade.document.Document;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

;

@Slf4j
public class MongoHelper {

    private MongoDriverProperties properties;

    private MongoClient mongoClient;
    @Getter
    protected MongoDatabase mongoDatabase;

    public MongoHelper(MongoDriverProperties properties,MongoClient mongoClient){
        this.properties = properties;
        this.mongoClient = mongoClient;
    }

    /**
     * ConcurrentSkipListSet是线程安全的有序的集合，适用于高并发的场景。
     */
    private volatile Set<String> collectSet = new ConcurrentSkipListSet<>();

    public void init() {
        mongoDatabase = mongoClient.getDatabase(properties.getDatabase());
    }

    /**
     * 获取默认的update内容
     *
     * @param primaryValue 主键值
     * @param key          存入的key
     * @param values       存入的value
     * @param logicType    逻辑id
     * @param ownerId      所属id
     * @return Bson 格式的更新内容
     */
    public <T> Bson getDefaultUpdateBson(String primaryValue, String key, T values, String logicType, String ownerId) {
        return Updates.combine(Updates.set(Key.PRIMARY_KEY, primaryValue),
                Updates.set(Key.KEY_KEY, key),
                Updates.set(Key.VALUE_KEY, values),
                Updates.set(Key.DELETE_KEY, Key.DELETED_FALSE),
                Updates.set(Key.QUERY_ALL_KEY, Key.getQueryAllValue(logicType, ownerId)));
    }

    /**
     * 获取默认的自增内容
     *
     * @param primaryValue 主键值
     * @param key          存入的key
     * @param incrValue    需要自增的keyMap
     * @param logicType    逻辑id
     * @param ownerId      所属id
     * @return Bson格式的自增内容
     */
    public Bson getDefaultIncrBson(String primaryValue, String key, Long incrValue, String logicType, String ownerId) {
        return Updates.combine(Updates.set(Key.PRIMARY_KEY, primaryValue),
                Updates.set(Key.KEY_KEY, key),
                Updates.inc(Key.VALUE_KEY, incrValue),
                Updates.set(Key.DELETE_KEY, Key.DELETED_FALSE),
                Updates.set(Key.QUERY_ALL_KEY, Key.getQueryAllValue(logicType, ownerId)));
    }

    /**
     * 获取需要创建的索引
     *
     * @return 需要创建的索引
     */
    public List<IndexModel> getDefaultIndexes() {
        List<IndexModel> defaultIndexes = new ArrayList<>();
        //主键index
        Bson primaryIndexBson = Indexes.compoundIndex(Indexes.hashed(Key.PRIMARY_KEY), Indexes.ascending((Key.DELETE_KEY)));
        IndexModel primaryIndex = new IndexModel(primaryIndexBson);
        //查询键
        Bson queryIndexBson = Indexes.compoundIndex(Indexes.hashed(Key.QUERY_ALL_KEY), Indexes.ascending((Key.DELETE_KEY)));
        IndexModel queryIndex = new IndexModel(queryIndexBson);
        defaultIndexes.add(primaryIndex);
        defaultIndexes.add(queryIndex);
        return defaultIndexes;
    }

    public <T> void checkIndex(Class<T> clazz) {
        Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
        String collectionName = document == null ? clazz.getSimpleName() : document.collectionName();
        List<IndexModel> indexes;
        if (collectSet.contains(collectionName)){
            return;
        }
        if (document != null) {
            indexes = getIndexes(document);
        } else {
            indexes = getDefaultIndexes();
        }
        //内存中无此collection，创建它
        Mono.from(mongoDatabase.getCollection(collectionName).createIndexes(indexes))
                .subscribe(s -> {
                    collectSet.add(collectionName);
                    log.info("create indexes: {}", s);
                }, throwable -> log.warn("create indexes error: ", throwable));
    }

    /**
     * 获取需要创建的索引
     *
     * @return 需要创建的索引
     */
    public List<IndexModel> getIndexes(Document document) {
        List<IndexModel> defaultIndexes = new ArrayList<>();
        //主键index
        Bson primaryIndexBson = Indexes.compoundIndex(Indexes.ascending(Key.PRIMARY_KEY), Indexes.ascending((Key.DELETE_KEY)));
        IndexModel primaryIndex = new IndexModel(primaryIndexBson, new IndexOptions().unique(true));
        //查询键
        Bson queryIndexBson = Indexes.compoundIndex(Indexes.ascending(Key.QUERY_ALL_KEY), Indexes.ascending((Key.DELETE_KEY)));
        Arrays.stream(document.indexes()).forEach(index -> {
            String[] fields = index.split(",");
            Bson indexDefine;
            if (document.indexType().equals(Document.IndexType.ASC)) {
                indexDefine = Indexes.ascending(fields);
            } else {
                indexDefine = Indexes.descending(fields);
            }
            IndexModel queryIndex = new IndexModel(indexDefine);
            defaultIndexes.add(queryIndex);
        });
        IndexModel queryIndex = new IndexModel(queryIndexBson);
        defaultIndexes.add(primaryIndex);
        defaultIndexes.add(queryIndex);
        return defaultIndexes;
    }
}