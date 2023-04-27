package com.homo.core.entity.server.test;

import com.homo.core.entity.facade.IEntityService;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.rpc.base.service.BaseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class EntityService extends BaseService implements IEntityService {
}
