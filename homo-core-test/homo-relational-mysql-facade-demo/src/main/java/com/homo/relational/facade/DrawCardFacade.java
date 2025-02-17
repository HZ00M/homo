package com.homo.relational.facade;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.upload.DefaultUploadFile;
import io.homo.proto.relational.test.*;
import io.homo.proto.rpc.HttpHeadInfo;

@ServiceExport(tagName = "relational-service:30022",isMainServer = true,isStateful = false,driverType = RpcType.http)
@RpcHandler
public interface DrawCardFacade {
    Homo<String> uploadFile(DefaultUploadFile file);

    Homo<SaveDrawCardResp> save(SaveDrawCardReq req, HttpHeadInfo header);

    Homo<JSONObject> updateRecord(JSONObject req,JSONObject header);

    Homo<InsertDrawCardResp> insert(InsertDrawCardReq req);

    Homo<InsertsDrawCardResp> inserts(InsertsDrawCardReq req);

    Homo<QueryDrawCardResp> queryFindAll(QueryDrawCardReq req);

    Homo<QueryDrawCardResp> queryFindOne(QueryDrawCardReq req);

    Homo<QueryDrawCardResp> queryFindExists(QueryDrawCardReq req);

    Homo<DeleteDrawCardResp> delete(DeleteDrawCardReq req);

    Homo<UpdateDrawCardResp> updateEntity(UpdateDrawCardReq req);

    Homo<UpdateDrawCardResp> update(UpdateDrawCardReq req);

    Homo<ExecuteSqlResp> execute(ExecuteSqlReq req);

    Homo<AggregateResp> aggregate(AggregateReq req);

    Homo<AggregateResp> lookUp(AggregateReq req);
}
