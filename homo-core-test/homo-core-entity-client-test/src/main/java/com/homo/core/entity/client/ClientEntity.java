package com.homo.core.entity.client;

import com.core.ability.base.AbstractAbilityEntity;
import com.core.ability.base.storage.SaveAble;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClientEntity extends AbstractAbilityEntity implements IClientEntity, SaveAble {
    @Override
    public Homo<String> clientCall(String param) {
        log.info("clientCall {}", param);
        return Homo.result(param);
    }
}
