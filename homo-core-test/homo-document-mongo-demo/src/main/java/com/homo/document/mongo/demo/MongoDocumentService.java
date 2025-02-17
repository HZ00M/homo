package com.homo.document.mongo.demo;

import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.storage.DocumentStorage;
import com.homo.core.utils.rector.Homo;
import com.homo.document.mongo.demo.document.UserDocument;
import com.homo.document.mongo.demo.vo.UserVO;
import com.homo.document.mongo.facade.MongoDocumentServiceFacade;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UnwindOptions;
import io.homo.proto.document.demo.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MongoDocumentService extends BaseService implements MongoDocumentServiceFacade {
    /**
     * 使用响应式Mongo文档存储组件
     */
    @Autowired
    DocumentStorage<Bson, Bson, Bson, List<Bson>> documentStorage;
    public static String USER_LOGIC_TYPE = "UserLogicType";
    /**
     * 查询一条记录
     * @param req
     * @return
     */
    public Homo<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        log.info("getUserInfo req userId {} req {}", req.getUserId(), req);
        return documentStorage
                .get(USER_LOGIC_TYPE, req.getUserId(), "data", UserDocument.class)
                .nextDo(retUserDocument -> {
                    GetUserInfoResp res;
                    if (retUserDocument != null) {
                        res = GetUserInfoResp.newBuilder()
                                .setErrorCode(0)
                                .setErrorDesc("获取成功")
                                .setUserInfo(UserDocument.covertUserInfoToProto(retUserDocument))
                                .build();
                    } else {
                        res = GetUserInfoResp.newBuilder()
                                .setErrorCode(1)
                                .setErrorDesc("没有该用户信息")
                                .build();
                    }
                    log.info("getUserInfo res userId {} res {}", req.getUserId(), res);
                    return Homo.result(res);
                }).onErrorContinue(throwable -> {
                    GetUserInfoResp res = GetUserInfoResp.newBuilder()
                            .setErrorCode(2)
                            .setErrorDesc("服务器异常")
                            .build();
                    log.error("getUserInfo error userId {} req {} res {}", req.getUserId(), req, res);
                    return Homo.result(res);
                });
    }

    @Override
    public Homo<CreateUserResp> createInfo(CreateUserReq req) {
        log.info("createInfo req userId {} req {}", req.getUserId(), req);
        String userId = req.getUserId();
        return documentStorage.get(USER_LOGIC_TYPE, userId, "data", UserDocument.class)
                .nextDo(newUserDocument -> {
                    if (newUserDocument != null) {
                        return Homo.error(new Exception("用户已存在"));

                    }
                    newUserDocument = UserDocument.createUser(req.getUserId());
                    UserDocument finalNewUserDocument = newUserDocument;
                    return documentStorage.save(USER_LOGIC_TYPE, userId, "data", newUserDocument, UserDocument.class)
                            .nextDo(updateRet ->
                                    Homo.result(CreateUserResp.newBuilder().setErrorCode(0).setErrorDesc("创建成功").setUserInfo(UserDocument.covertUserInfoToProto(finalNewUserDocument)).build())
                                            .onErrorContinue(throwable -> {
                                                CreateUserResp res = CreateUserResp.newBuilder()
                                                        .setErrorCode(1)
                                                        .setErrorDesc(throwable.getMessage())
                                                        .build();
                                                log.info("createUserInfo fail userId {} req {} res {}", userId, req, res);
                                                return Homo.result(res);
                                            })
                            );
                });

    }

    @Override
    public Homo<QueryUserInfoResp> queryUserInfo(QueryUserInfoReq req) {
        Bson filter;
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("value.userId",1)); // 查找
        filter = Filters.and(filters);

        List<Bson> sorts = new ArrayList<>(); // 按顺序
        Bson ascending = Sorts.ascending("value.age");
        sorts.add(ascending);
        Bson sort = Sorts.orderBy(sorts);

        return documentStorage.query(filter, sort, 0, 0, UserDocument.class)
                .nextDo(users->{
                    QueryUserInfoResp res;
                    List<UserInfoPb> userInfos = new ArrayList<>();
                    for (UserDocument userDocument : users) {
                        userInfos.add(UserDocument.covertUserInfoToProto(userDocument));
                    }
                    res = QueryUserInfoResp.newBuilder()
                            .setErrorCode(0)
                            .setErrorDesc("查询成功")
                            .addAllUserInfo(userInfos)
                            .build();
                    log.info("queryUserInfo res  users {}", users);
                    return Homo.result(res);
                });
    }

    @Override
    public Homo<AggregateUserInfoResp> aggregateInfo(AggregateUserInfoReq req) {
        List<Bson> aggregateList = new ArrayList();
        aggregateList.add(Aggregates.sort(Sorts.descending("value.age")));
        aggregateList.add(Aggregates.skip(1));
        aggregateList.add(Aggregates.limit(10));
        // 关联
        aggregateList.add(Aggregates.lookup("user", "value.createId", "value.userId", "create_result"));
        // 保留空数组
        UnwindOptions unwindOptions = new UnwindOptions();
        unwindOptions.preserveNullAndEmptyArrays(Boolean.TRUE);
        // 展开数组
        aggregateList.add(Aggregates.unwind("$create_result", unwindOptions));
        // 返回值处理
        Document project = new Document();
        project.put("dynamicComment", "$value");
        project.put("createUser", "$create_result.value");
        aggregateList.add(Aggregates.project(project));
        return documentStorage.aggregate(aggregateList, UserDocument.class, UserVO.class)
                .nextDo(vo->{
                    AggregateUserInfoResp resp = AggregateUserInfoResp.newBuilder().build();
                    return Homo.result(resp);
                });
    }
}
