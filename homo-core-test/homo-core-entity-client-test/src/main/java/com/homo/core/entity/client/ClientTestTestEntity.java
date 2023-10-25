package com.homo.core.entity.client;

import com.core.ability.base.AbstractAbilityEntity;
import com.homo.core.facade.ability.SaveAble;
import com.homo.core.facade.ability.TimeAble;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientTestTestEntity extends AbstractAbilityEntity implements IClientTestEntity, SaveAble, TimeAble {
    @Override
    public Homo<String> clientCall(String param) {
        log.info("clientCall {}", param);
        return Homo.result(param);
    }
}
