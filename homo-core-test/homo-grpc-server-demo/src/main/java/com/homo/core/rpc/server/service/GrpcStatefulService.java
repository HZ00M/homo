//package com.homo.core.rpc.server.service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.homo.core.rpc.base.service.BaseService;
//import com.homo.core.rpc.server.facade.GrpcStatefulServiceFacade;
//import com.homo.core.rpc.server.vo.ParamVO;
//import com.homo.core.utils.rector.Homo;
//import io.homo.proto.client.ParameterMsg;
//import io.homo.proto.rpc.test.TestServerRequest;
//import io.homo.proto.rpc.test.TestServerResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import reactor.util.function.Tuple2;
//import reactor.util.function.Tuples;
//
//@Slf4j
//@Component
//public class GrpcStatefulService extends BaseService implements GrpcStatefulServiceFacade {
//
//    @Override
//    public Homo<JSONObject> jsonCall(Integer podId, ParameterMsg parameterMsg,JSONObject jsonStr) {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("2","2");
//        return Homo.result(jsonObject);
//    }
//
//    @Override
//    public Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO) {
//        return Homo.result(1);
//    }
//
//    @Override
//    public Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request) {
//        return Homo.result(TestServerResponse.newBuilder().setCode(123).build());
//    }
//
//    @Override
//    public Homo<Tuple2<String, Integer>> tupleCall(Integer podId, ParameterMsg parameterMsg
//    ) {
//        return Homo.result(Tuples.of("ok",1));
//    }
//}
