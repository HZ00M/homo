//package com.core.ability.base;
//
//import com.core.ability.base.call.CallSystem;
//import com.homo.core.facade.ability.IEntityService;
//import com.homo.core.rpc.base.service.BaseService;
//import com.homo.core.utils.rector.Homo;
//import com.homo.core.utils.trace.ZipkinUtil;
//import io.homo.proto.client.ParameterMsg;
//import io.homo.proto.entity.EntityRequest;
//import io.homo.proto.entity.PingRequest;
//import io.homo.proto.entity.PongRequest;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Log4j2
//public class EntityService extends BaseService implements IEntityService {
//    @Autowired
//    CallSystem callSystem;
//
//    @Override
//    public void postInit() {
//        log.info("EntityService postInit");
//    }
//
//    @Override
//    public Homo entityCall(Integer podIndex, ParameterMsg parameterMsg, EntityRequest request) throws Exception{
//        if (log.isDebugEnabled()) {
//            log.debug("entityCall podIndex {} type {} id {} request {}", podIndex, request.getType(), request.getId(), request);
//        }
//        ZipkinUtil.getTracing().tracer().currentSpan().tag("entityCall", request.getFunName());
//        return callSystem.call(request.getSrcName(), request, podIndex, parameterMsg);
//    }
//
//    @Override
//    public Homo<PongRequest> ping(Integer podIndex, ParameterMsg parameterMsg, PingRequest request) {
//        if (log.isDebugEnabled()) {
//            log.debug("ping podIndex {} request time {}", podIndex, request.getTime());
//        }
//        return Homo.result(PongRequest.newBuilder().setTime(System.currentTimeMillis()).build());
//    }
//}
