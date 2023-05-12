package com.homo.core.entity.server.test;

import com.homo.core.entity.facade.IServerEntityService;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.rpc.base.service.BaseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ServerEntityService extends BaseService implements IServerEntityService {
    @Autowired
    AbilityEntityMgr abilityEntityMgr;
    @Override
    public void postInit() {
        abilityEntityMgr.registerEntityNotFoundProcess(ServerEntity.class,((aClass, id) -> abilityEntityMgr.createEntityPromise(aClass,id)));
    }
}
