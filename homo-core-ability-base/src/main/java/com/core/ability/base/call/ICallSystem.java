package com.core.ability.base.call;

import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.entity.EntityRequest;

import java.lang.reflect.Method;

public interface ICallSystem extends AbilitySystem {
    Homo call(String srcName, EntityRequest entityRequest, Integer podId, Object parameterMsg) throws Exception;

    Homo callLocalMethod(String type, String id, Method method,Object[] objects);

    Homo<Boolean> add(CallAbility callAbility);

    Homo<CallAbility> remove(CallAbility callAbility);

    CallAbility get(String type,String id);
}
