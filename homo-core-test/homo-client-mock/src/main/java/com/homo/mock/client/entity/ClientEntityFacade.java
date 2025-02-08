package com.homo.mock.client.entity;

import com.homo.core.facade.ability.CacheTime;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.StorageTime;
import com.homo.core.utils.rector.Homo;

@EntityType(type = "client-entity")
@StorageTime(10000)
@CacheTime(10000)
public interface ClientEntityFacade {
    Homo<String> clientCall(String param);
}
