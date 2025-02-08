package com.homo.core.rpc.server.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.rpc.server.vo.ParamVO;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import reactor.util.function.Tuple2;

/**
 * 声明一个grpc无状态的主服务
 * host grpc-server-stateless
 * 端口 30302
 */
@ServiceExport(tagName = "grpc-server-stateless:30302",isMainServer = true,isStateful = false,driverType = RpcType.grpc)
@RpcHandler
public interface GrpcRpcServiceFacade {
    /**
     * 普通值调用
     * @param podId  pod id
     * @param parameterMsg  填充参数
     * @param param json 字符串
     */
    Homo<String> valueCall(Integer podId, ParameterMsg parameterMsg, String param);

    /**
     * pojo 对象参数调用
     * @param podId  pod id
     * @param parameterMsg  填充参数
     * @param paramVO  POJO
     */
    Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO);

    /**
     * protobuf 参数调用
     * @param podId  pod id
     * @param parameterMsg  填充参数
     * @param request protobuf 二进制
     */
    Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request);

    /**
     * 多值返回
     * @param podId
     * @param podId  pod id
     * @param parameterMsg  填充参数
     * @return  Tuple2<String,Integer>
     */
    Homo<Tuple2<String,Integer>> tuple2ReturnCall(Integer podId, ParameterMsg parameterMsg);
}
