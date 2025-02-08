package com.homo.core.rpc.server.service;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.rpc.server.facade.GrpcRpcServiceFacade;
import com.homo.core.rpc.server.vo.ParamVO;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class GrpcRpcService extends BaseService implements GrpcRpcServiceFacade {

    @Override
    public Homo<String> valueCall(Integer podId, ParameterMsg parameterMsg, String param) {
        ParamVO paramVO = new ParamVO();
        log.info("jsonCall podId {} parameterMsg {} param {} paramVO {}", podId, parameterMsg, param,paramVO);
        return Homo.result(JSONObject.toJSONString(paramVO));
    }

    @Override
    public Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO) {
        log.info("objCall podId {} parameterMsg {} paramVO {}", podId, parameterMsg, paramVO);
        return Homo.result(1);
    }

    @Override
    public Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request) {
        log.info("pbCall podId {} parameterMsg {} request {}", podId, parameterMsg, request);
        return Homo.result(TestServerResponse.newBuilder().setCode(123).build());
    }

    @Override
    public Homo<Tuple2<String, Integer>> tuple2ReturnCall(Integer podId, ParameterMsg parameterMsg) {
        Tuple2<String, Integer> tuple2 = Tuples.of("ok", 1);
        log.info("tuple2ReturnCall podId {} parameterMsg {} tuple2 {}", podId, parameterMsg,tuple2);
        return Homo.result(tuple2);
    }
}
