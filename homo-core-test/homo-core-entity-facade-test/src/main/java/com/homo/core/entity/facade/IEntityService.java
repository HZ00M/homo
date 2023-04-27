package com.homo.core.entity.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;

@ServiceExport(tagName = "entity-server:30012",isMainServer = true,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface IEntityService {
}
