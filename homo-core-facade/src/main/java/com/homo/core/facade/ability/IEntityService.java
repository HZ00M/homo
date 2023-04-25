package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.PingRequest;
import io.homo.proto.entity.PongRequest;

public interface IEntityService {

    Homo entityCall(Integer podIndex, ParameterMsg parameterMsg, EntityRequest request) throws Exception;


    Homo<PongRequest> ping(Integer podIndex, ParameterMsg parameterMsg, PingRequest request);
}
