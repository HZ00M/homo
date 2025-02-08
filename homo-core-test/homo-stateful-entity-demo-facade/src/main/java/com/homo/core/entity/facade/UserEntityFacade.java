package com.homo.core.entity.facade;

import com.homo.core.facade.ability.CacheTime;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.StorageTime;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.entity.test.*;

@EntityType(type = "user-entity")
@StorageTime(10000)
@CacheTime(20000)
public interface UserEntityFacade {
    Homo<QueryInfoResponse> queryInfo(QueryInfoRequest request);

    Homo<EnterGameResponse> enterGame(EnterGameRequest request);

    Homo<LeaveGameResponse> leaveGame(LeaveGameRequest request);
}
