package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;

import java.lang.reflect.Method;

public interface ICallSystem extends AbilitySystem {
    Homo call(String srcName, EntityRequest entityRequest, Integer podId, ParameterMsg parameterMsg) throws Exception;

    Homo callLocalMethod(String type, String id, Method method,Object[] objects);

    Homo<Boolean> add(ICallAbility callAbility);

    Homo<ICallAbility> remove(ICallAbility callAbility);

    ICallAbility get(String type,String id);
}
