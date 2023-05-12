package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.PingRequest;
import io.homo.proto.entity.PongRequest;

public interface IEntityService {
    String default_entity_call_method = "entityCall";
    String default_entity_ping_method = "ping";

    Homo entityCall(Integer podIndex, EntityRequest request) throws Exception;


    Homo<PongRequest> ping(Integer podIndex, ParameterMsg parameterMsg, PingRequest request);
}
