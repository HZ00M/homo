package com.homo.core.entity.client;

import com.core.ability.base.storage.CacheTime;
import com.core.ability.base.storage.StorageTime;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.rector.Homo;

@EntityType(type = "client")
@StorageTime(10000)
@CacheTime(10000)
public interface IClientEntity {
    Homo<String> clientCall(String param);
}
