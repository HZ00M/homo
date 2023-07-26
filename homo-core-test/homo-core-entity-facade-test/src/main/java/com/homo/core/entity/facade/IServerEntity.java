package com.homo.core.entity.facade;

import com.homo.core.facade.ability.CacheTime;
import com.homo.core.facade.ability.StorageTime;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.entity.test.TestEntityRequest;
import io.homo.proto.entity.test.TestEntityResponse;

@EntityType(type = "server")
@StorageTime(10000)
@CacheTime(10000)
public interface IServerEntity {
    Homo<TestEntityResponse> login(TestEntityRequest request);
}
