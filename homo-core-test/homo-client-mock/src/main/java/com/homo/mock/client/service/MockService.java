package com.homo.mock.client.service;

import com.core.ability.base.StorageEntityMgr;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.utils.rector.Homo;
import com.homo.mock.client.entity.ClientEntity;
import com.homo.mock.client.facade.MockServiceFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockService extends BaseService implements MockServiceFacade {
    @Autowired
    StorageEntityMgr entityMgr;

    @Override
    public void afterServerInit(){
        entityMgr.registerEntityNotFoundProcess(ClientEntity.class,((aClass, id) -> entityMgr.createEntityPromise(aClass,id)));
    }
    @Override
    public Homo<Integer> mock(Integer param) {
        return Homo.result(param);
    }
}
