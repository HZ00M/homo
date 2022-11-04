package com.homo.core.rpc.client.service;

import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.rpc.client.facade.RpcClientFacade;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class RpcClientService extends BaseService implements RpcClientFacade {
    @Override
    public Homo<Integer> objCall(Integer param) {
        return Homo.result(param);
    }
}
