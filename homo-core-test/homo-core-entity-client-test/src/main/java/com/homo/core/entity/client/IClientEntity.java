package com.homo.core.entity.client;

import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.rector.Homo;

@EntityType(type = "client")
public interface IClientEntity {
    Homo<String> clientCall(String param);
}