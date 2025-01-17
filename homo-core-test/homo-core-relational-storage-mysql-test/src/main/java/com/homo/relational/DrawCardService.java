package com.homo.relational;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.relational.operation.RelationalTemplate;
import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.facade.relational.query.HomoSort;
import com.homo.core.facade.relational.query.HomoUpdate;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.upload.DefaultUploadFile;
import com.homo.relational.base.aggregate.HomoAggregation;
import com.homo.relational.base.criteria.HomoCriteria;
import com.homo.relational.domain.DrawCardTable;
import com.homo.relational.facade.DrawCardFacade;
import io.homo.proto.relational.test.*;
import io.homo.proto.rpc.HttpHeadInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DrawCardService extends BaseService implements DrawCardFacade {
    @Autowired
    RelationalTemplate relationalTemplate;

    @Override
    public Homo<SaveDrawCardResp> save(SaveDrawCardReq req, HttpHeadInfo header) {
        DrawCardPb drawCard = req.getDrawCard();
        int poolId = drawCard.getPoolId();
        String userId = drawCard.getUserId();
        DrawCardTable drawCardTable = new DrawCardTable();
        drawCardTable.poolId = poolId;
        drawCardTable.userId = userId;
        log.info("save start drawCardTable {}", drawCardTable);
        return relationalTemplate.save(DrawCardTable.class).value(drawCardTable)
                .nextDo(ret -> {
                    log.info("save success drawCardTable {} ret {}", drawCardTable, ret);
                    return Homo.result(SaveDrawCardResp.newBuilder().setCode(1).setId(ret.getId()).build());
                });
    }


    @Override
    public Homo<InsertDrawCardResp> insert(InsertDrawCardReq req) {
        DrawCardPb drawCardPb = req.getDrawCard();
        DrawCardTable drawCardTable = new DrawCardTable();
        drawCardTable.id = drawCardPb.getId();
        drawCardTable.poolId = drawCardPb.getPoolId();
        drawCardTable.userId = drawCardPb.getUserId();
        log.info("insert start drawCardTable {} ", drawCardTable);
        return relationalTemplate.insert(DrawCardTable.class).value(drawCardTable)
                .nextDo(ret -> {
                    log.info("insert success, drawCardTable {}  ret {}", drawCardTable, ret);
                    return Homo.result(InsertDrawCardResp.newBuilder().setCode(1).setId(ret.getId()).build());
                });
    }

    @Override
    public Homo<InsertsDrawCardResp> inserts(InsertsDrawCardReq req) {
        List<DrawCardTable> list = new ArrayList<>();
        for (DrawCardPb drawCardPb : req.getDrawCardList()) {
            DrawCardTable drawCardTable = new DrawCardTable();
            drawCardTable.id = drawCardPb.getId();
            drawCardTable.poolId = drawCardPb.getPoolId();
            drawCardTable.userId = drawCardPb.getUserId();
            list.add(drawCardTable);
        }
        DrawCardTable[] array = list.toArray(new DrawCardTable[0]);
        return relationalTemplate.insert(DrawCardTable.class).values(array)
                .nextDo(ret -> {
                    log.info("insert success, array {} ret {}", array, ret);
                    return Homo.result(InsertsDrawCardResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<QueryDrawCardResp> queryFindAll(QueryDrawCardReq req) {
        List<Long> idsList = req.getIdsList();
        log.info("queryFindAll start idsList {}", idsList);
        HomoCriteria criteria = HomoCriteria.where("id").in(idsList.toArray())
                .and("pool_id").eq("1");
        return relationalTemplate.find(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                                .sort(HomoSort.by(HomoSort.Order.desc("id"), HomoSort.Order.desc("pool_id")))
                                .limit(2)
                                .offset(1)
                )
                .findAll()
                .nextDo(ret -> {
                    log.info("queryFindAll success, ret {}", ret);
                    List<DrawCardPb> drawCardPbList = ret.stream().map(DrawCardTable::covertToPb).collect(Collectors.toList());
                    QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().addAllDrawCards(drawCardPbList).build();
                    return Homo.result(drawCardResp);
                });
    }

    @Override
    public Homo<QueryDrawCardResp> queryFindOne(QueryDrawCardReq req) {
        List<Long> idsList = req.getIdsList();
        log.info("queryFindOne start idsList {}", idsList);
        HomoCriteria criteria = HomoCriteria.where("user_id").eq(idsList.get(0))
                .or("user_id").eq(idsList.get(1));
        return relationalTemplate.find(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                                .sort(HomoSort.by(HomoSort.Order.asc("id"), HomoSort.Order.desc("pool_id")))
                )
                .findOne()
                .nextDo(ret -> {
                    log.info("queryFindOne success, ret {}", ret);
                    DrawCardPb drawCardPb = ret.covertToPb();
                    QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().addDrawCards(drawCardPb).build();
                    return Homo.result(drawCardResp);
                });
    }

    @Override
    public Homo<QueryDrawCardResp> queryFindExists(QueryDrawCardReq req) {
        List<Long> idsList = req.getIdsList();
        log.info("queryFindExists start idsList {}", idsList);
        HomoCriteria criteria = HomoCriteria.where("user_id").in(idsList.toArray())
                .or("pool_id").eq("1");
        return relationalTemplate.find(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                                .sort(HomoSort.by(HomoSort.Order.asc("id"), HomoSort.Order.desc("pool_id")))
                )
                .exists()
                .nextDo(ret -> {
                    log.info("queryFindExists success, ret {}", ret);
                    QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().build();
                    return Homo.result(drawCardResp);
                });
    }

    @Override
    public Homo<DeleteDrawCardResp> delete(DeleteDrawCardReq req) {
        log.info("delete start, req {}", req);
        List<Long> idsList = req.getIdsList();
        HomoCriteria criteria = HomoCriteria.where("user_id").in(idsList.toArray());
        return relationalTemplate
                .delete(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                )
                .all()
                .nextDo(ret->{
                    log.info("delete success, ret {}", ret);
                    return Homo.result(DeleteDrawCardResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<UpdateDrawCardResp> updateEntity(UpdateDrawCardReq req) {
        log.info("updateEntity start, req {}", req);
        DrawCardTable drawCardTable = DrawCardTable.coverPbTo(req.getDrawCard());
        HomoCriteria criteria = HomoCriteria.where("id").eq(drawCardTable.getId());
        drawCardTable.setPoolId(1111);
        return relationalTemplate
                .update(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                )
                .apply(drawCardTable)
                .nextDo(ret->{
                    log.info("updateEntity success, ret {}", ret);
                    return Homo.result(UpdateDrawCardResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<UpdateDrawCardResp> update(UpdateDrawCardReq req) {
        log.info("update start, req {}", req);
        DrawCardTable drawCardTable = DrawCardTable.coverPbTo(req.getDrawCard());
        HomoCriteria criteria = HomoCriteria.where("id").eq(drawCardTable.getId());
        HomoUpdate homoUpdate = HomoUpdate.builder().set("pool_id", drawCardTable.getPoolId()).build();
        return relationalTemplate
                .update(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                )
                .apply(homoUpdate)
                .nextDo(ret->{
                    log.info("update success, ret {}", ret);
                    return Homo.result(UpdateDrawCardResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<ExecuteSqlResp> execute(ExecuteSqlReq req) {
        String sql = req.getSql();
        return relationalTemplate
                .execute(sql)
                .all()
                .nextDo(ret->{
                    log.info("execute success, ret {}", ret);
                    return Homo.result(ExecuteSqlResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<AggregateResp> aggregate(AggregateReq req) {
        HomoAggregation aggregation = HomoAggregation.newBuilder(DrawCardTable.class)
                .project("user_id")
                .group("user_id")
                .count("id")
                .sum("pool_id")
                .match(HomoCriteria.where("id").greaterThan("1"))
                .build();
        return relationalTemplate.aggregate(aggregation,DrawCardTable.class)
                .all()
                .nextDo(ret->{
                    log.info("aggregate success, ret {}", ret);
                    return Homo.result(AggregateResp.newBuilder().setCode(1).build());
                });
    }

    @Override
    public Homo<JSONObject> updateRecord(JSONObject req, JSONObject header) {
        log.info("req {} header {}", req, header);
        JSONObject ret = new JSONObject();
        ret.put("111", "222");
        return Homo.result(ret);
    }

    @Override
    public Homo<String> uploadFile(DefaultUploadFile file) {
        return Homo.warp(file.content()
                .map(dataBuffer -> {
                    //每次处理一个dataBuffer
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String contentStr = new String(bytes, StandardCharsets.UTF_8);
                    return contentStr;
                })
                .reduce(String::concat)
                .flatMap(content -> {
                    log.info("File content received: {}", content);
                    Map<String, String> headers = file.getHeaders();
                    log.info("File headers received: {}", headers);
                    MultiValueMap<String, String> queryParams = file.queryParams();
                    log.info("File query parameters received: {}", queryParams);
                    MultiValueMap<String, String> formData = file.getFormData();
                    log.info("File form data received: {}", formData);
                    return Mono.just("Received file with content: " + content);
                }));
//        将 content写入文件（流式处理）
//        当你使用 Reactor 工具方法（如 DataBufferUtils.write）直接操作流时，不需要DataBufferUtils.release(dataBuffer)手动释放：
//        String targetPath = "/path/to/save/" + file.filename(); // 目标保存路径
//        return Homo.warp(DataBufferUtils.write(file.content(), Paths.get(targetPath), StandardOpenOption.CREATE)
//                .doOnError(e -> log.error("Error saving file: {}", e.getMessage())) // 保存错误日志
//                .then(Mono.just("File uploaded successfully!"))); // 上传成功响应
    }
}
