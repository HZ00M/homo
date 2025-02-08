package com.homo.mock.client.entity;

import com.core.ability.base.BaseAbilityEntity;
import com.homo.core.facade.ability.SaveAble;
import com.homo.core.facade.ability.TimeAble;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientEntity extends BaseAbilityEntity implements ClientEntityFacade, SaveAble, TimeAble {
    @Override
    public Homo<String> clientCall(String param) {
        log.info("clientCall invoke {}", param);
        return Homo.result(param);
    }
}
