package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import io.homo.proto.entity.Ping;
import io.homo.proto.entity.Pong;

public interface IEntityService {
    String default_entity_call_method = "entityCall";
    String default_entity_ping_method = "ping";

    Homo<EntityResponse> entityCall(Integer podIndex, EntityRequest request) throws Exception;


    Homo<Pong> ping(Integer podIndex, ParameterMsg parameterMsg, Ping request);
}
