package com.homo.core.rpc.server.service;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.rpc.server.facade.RpcServiceFacade;
import com.homo.core.rpc.server.facade.TestObjParam;
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
public class RpcServerService extends BaseService implements RpcServiceFacade {

    @Override
    public Homo<String> jsonCall(String jsonStr) {
        TestObjParam testObjParam = new TestObjParam();
        return Homo.result(JSONObject.toJSONString(testObjParam));
    }

    @Override
    public Homo<Integer> objCall(TestObjParam testObjParam) {
        return Homo.result(1);
    }

    @Override
    public Homo<TestServerResponse> pbCall(TestServerRequest request) {
        return Homo.result(TestServerResponse.newBuilder().setCode(1).build());
    }

    @Override
    public Homo<TestServerResponse> targetCall(Integer podId, ParameterMsg parameterMsg, TestServerRequest request) {
        return Homo.result(TestServerResponse.newBuilder().setCode(1).build());
    }

    @Override
    public Homo<Tuple2<String, Integer>> tupleCall() {
        return Homo.result(Tuples.of("ok",1));
    }
}
