package com.homo.document.mongo.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.document.demo.*;

@ServiceExport(tagName = "mongo-document-stateless:30306",isMainServer = true,isStateful = false,driverType = RpcType.grpc)
@RpcHandler
public interface MongoDocumentServiceFacade {
    Homo<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    Homo<CreateUserResp> createInfo(CreateUserReq req);

    Homo<QueryUserInfoResp> queryUserInfo(QueryUserInfoReq req);

    Homo<AggregateUserInfoResp> aggregateInfo(AggregateUserInfoReq req);
}
