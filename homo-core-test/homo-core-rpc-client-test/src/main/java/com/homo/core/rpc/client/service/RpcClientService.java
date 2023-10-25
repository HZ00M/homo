package com.homo.core.rpc.client.service;

import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.rpc.client.facade.RpcClientFacade;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RpcClientService extends BaseService implements RpcClientFacade {
    @Override
    public Homo<Integer> objCall(Integer param) {
        return Homo.result(param);
    }
}
