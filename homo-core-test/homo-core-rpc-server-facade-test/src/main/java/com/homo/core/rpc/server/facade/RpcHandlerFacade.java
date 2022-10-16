//package com.homo.core.rpc.server.facade;
//
//
//import com.homo.core.facade.rpc.RpcHandler;
//import com.homo.core.utils.rector.Homo;
//import io.homo.proto.client.ParameterMsg;
//import io.homo.proto.rpc.test.TestServerRequest;
//import io.homo.proto.rpc.test.TestServerResponse;
//import reactor.util.function.Tuple2;
//
//@RpcHandler
//public interface RpcHandlerFacade {
//    Homo<String> jsonCall2(String jsonStr);
//
//    Homo<Integer> objCall2(TestObjParam testObjParam);
//
//    Homo<TestServerResponse> pbCall2(TestServerRequest request);
//
//    Homo<TestServerResponse> targetCall2(Integer podId, ParameterMsg parameterMsg, TestServerRequest request);
//
//    Homo<Tuple2<String,Integer>> tupleCall2();
//}
