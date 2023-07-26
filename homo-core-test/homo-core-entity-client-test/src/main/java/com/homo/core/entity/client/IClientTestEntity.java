package com.homo.core.entity.client;

import com.homo.core.facade.ability.CacheTime;
import com.homo.core.facade.ability.StorageTime;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.rector.Homo;

@EntityType(type = "client")
@StorageTime(10000)
@CacheTime(10000)
public interface IClientTestEntity {
    Homo<String> clientCall(String param);
}
