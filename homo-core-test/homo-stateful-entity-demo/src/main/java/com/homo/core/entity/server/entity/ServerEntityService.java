package com.homo.core.entity.server.entity;

import com.homo.core.entity.facade.EntityServiceFacade;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.rpc.base.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerEntityService extends BaseService implements EntityServiceFacade {
    @Autowired
    AbilityEntityMgr abilityEntityMgr;
    @Override
    public void afterServerInit() {
        abilityEntityMgr.registerEntityNotFoundProcess(UserEntity.class,((aClass, id) -> abilityEntityMgr.createEntityPromise(aClass,id)));
    }
}
