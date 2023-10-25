package com.homo.core.rpc.http.mapping;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.homo.core.facade.module.Module;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.rpc.http.FileRpcContent;
import com.homo.core.rpc.http.HttpServer;
import com.homo.core.rpc.http.upload.DefaultUploadFile;
import com.homo.core.rpc.http.upload.UploadFile;
import com.homo.core.utils.serial.FSTSerializationProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.client.ClientRouterHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class DeFaultHttpMapping extends AbstractHttpMapping implements Module, ApplicationContextAware {

    private FSTSerializationProcessor defaultProcessor = new FSTSerializationProcessor();
    private ApplicationContext applicationContext;

    @Override
    public void init() {
        log.info("DeFaultHttpMapping init start");
        super.init();
        AbstractHandlerMethodMapping<RequestMappingInfo> objHandlerMethodMapping = (AbstractHandlerMethodMapping<RequestMappingInfo>) applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> mapRet = objHandlerMethodMapping.getHandlerMethods();
        for (RequestMappingInfo requestMappingInfo : mapRet.keySet()) {
            log.info("requestMappingInfo {}", requestMappingInfo);
        }
        log.info("DeFaultHttpMapping init end");
    }

    /**
     * 健康检查
     *
     * @return
     */
    @RequestMapping("/alive/check")
    public Mono<String> alive() {
        return Mono.just("ok");
    }

    /**
     * get请求转json onCall调用，第一个参数为http请求参数，第二个参数为消息头
     *
     * @param exchange
     * @return
     */
    @GetMapping("/**")
    public Mono<Void> httpGet(ServerWebExchange exchange) throws Exception {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        int port = exportPort(request);
        String msgId = exportMsgId(request);
        //参数格式(formDataParams,headerInfo)
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        Map<String, String> formDataParams = queryParams.toSingleValueMap();
        JSONObject headerInfo = exportHeaderInfo(request);
        List<Object> list = new ArrayList<>();
        list.add(formDataParams);
        list.add(headerInfo);
        String msg = JSON.toJSONString(list);
        log.info("httpGet begin port {} msgId {} msg {}", port, msgId, msg);
        HttpServer httpServer = routerHttpServerMap.get(port);
        Mono<DataBuffer> respBuffer = httpServer.onCall(msgId, msg, response);
        return response.writeAndFlushWith(Mono.just(respBuffer));
    }


    /**
     * post请求转json onCall调用，第一个参数为http请求参数，第二个参数为消息头
     *
     * @param exchange
     * @return
     */
    @PostMapping(value = "/**", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> httpJsonPost(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        int port = exportPort(request);
        String msgId = exportMsgId(request);
        Mono<Mono<DataBuffer>> resp = DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer ->
                        Mono.create(monoSink -> {
                                    try {
                                        checkDataBufferSize(dataBuffer);
                                        byte[] msgContent = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(msgContent);
                                        DataBufferUtils.release(dataBuffer);
                                        String reqStr = new String(msgContent);
                                        String msg;
                                        JSONValidator.Type type = JSONValidator.from(reqStr).setSupportMultiValue(true).getType();
                                        List<Object> list = new ArrayList<>();

                                        if (type == JSONValidator.Type.Array) {
                                            //参数是列表(json1,json2,...,headerInfo)
                                            JSONArray bodyArr = JSON.parseArray(reqStr);
                                            for (Object item : bodyArr) {
                                                list.add(item);
                                            }
                                        } else if (type == JSONValidator.Type.Object) {
                                            //参数是单个json (json,headerInfo)
                                            JSONObject bodyJson = JSON.parseObject(reqStr);
                                            list.add(bodyJson);
                                        } else {
                                            //参数是字符串(reqStr,headerInfo)
                                            list.add(reqStr);
                                        }
                                        JSONObject headerInfo = exportHeaderInfo(request);
                                        list.add(headerInfo);
                                        msg = JSON.toJSONString(list);
                                        log.info("httpJsonPost begin port {} msgId {} msg {}", port, msgId, msg);
                                        HttpServer httpServer = routerHttpServerMap.get(port);
                                        Mono<DataBuffer> bufferMono = httpServer.onCall(msgId, msg, response);
                                        monoSink.success(bufferMono);
                                    } catch (Exception e) {
                                        monoSink.error(e);
                                    }
                                }
                        ));
        return response.writeAndFlushWith(resp);
    }

    /**
     * post请求转pb协议 onCall调用，参数为pb协议
     *
     * @param exchange
     * @return
     */
    @PostMapping(value = "/**")
    public Mono<Void> httpProtoPost(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        int port = exportPort(request);
        String msgId = exportMsgId(request);
        Mono<Mono<DataBuffer>> resp = DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer ->
                        Mono.create(monoSink -> {
                            try {
                                checkDataBufferSize(dataBuffer);
                                byte[] msgContent = new byte[dataBuffer.readableByteCount()];
                                //参数格式 (pb协议,http头信息)
                                ClientRouterHeader routerHeader = ClientRouterHeader.newBuilder()
                                        .putAllHeaders(request.getHeaders().toSingleValueMap()).build();
                                byte[][] msg = {msgContent, routerHeader.toByteArray()};
                                log.info("httpProtoPost begin port {} msgId {} ", port, msgId);
                                dataBuffer.read(msgContent);
                                HttpServer httpServer = routerHttpServerMap.get(port);
                                Mono<DataBuffer> bufferMono = httpServer.onCall(msgId, msg, response);
                                monoSink.success(bufferMono);
                            } catch (Exception e) {
                                monoSink.error(e);
                            }
                        })
                );

        return response.writeAndFlushWith(resp);
    }

    /**
     * 上传文件
     *
     * @param exchange
     * @param filePart 上传的文件内容
     * @return
     */
    @PostMapping(value = "/upload/*", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> requestBodyFlux(ServerWebExchange exchange, @RequestPart("file") FilePart filePart) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        int port = exportPort(request);
        String msgId = exportMsgId(request);
        Mono<Mono<DataBuffer>> resp = Mono.create(monoMonoSink -> {
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
            // 把form-data模式中的参数，除了文件类型之外取出来放到formData
            Mono<MultiValueMap<String, String>> multipartData = exchange.getMultipartData()
                    .map(multiValueMap -> {
                        MultiValueMap<String, String> mmMap = new LinkedMultiValueMap<>();
                        multiValueMap.forEach((key, partList) -> {
                            if (partList.get(0) instanceof FormFieldPart) {
                                List<String> paramList = partList.stream().map(item -> ((FormFieldPart) item).value()).collect(Collectors.toList());
                                mmMap.put(key, paramList);
                            }
                        });
                        return mmMap;
                    });
            Mono<MultiValueMap<String, String>> finalFormData =
                    Mono.just(new LinkedMultiValueMap<String, String>())
                            .flatMap(linkMap ->
                                    //flatMap从另一个publisher获取，（异步的转换发布的元素并返回一个新的Mono，被转换的元素和新Mono是动态绑定的。）
                                    formData.map(map -> {
                                        linkMap.putAll(map);
                                        return linkMap;
                                    })
                            ).flatMap(linkMap ->
                                    multipartData.map(map -> {
                                                linkMap.putAll(map);
                                                return linkMap;
                                            }
                                    )
                            );
            Map<String, String> headers = request.getHeaders().toSingleValueMap();
            String filename = filePart.filename();
            Flux<DataBuffer> content = filePart.content();
            Span span = ZipkinUtil.nextOrCreateSRSpan();
            UploadFile uploadFile = new DefaultUploadFile(filename, headers, queryParams, finalFormData, content);
            FileRpcContent byteRpcContent = new FileRpcContent(uploadFile, RpcContentType.FILE, span);
            try {
                Mono<DataBuffer> dataBufferMono = routerHttpServerMap.get(port).onFileUpload(msgId, byteRpcContent, response);//todo 处理文件类型请求
                monoMonoSink.success(dataBufferMono);
            } catch (Exception e) {
                monoMonoSink.error(e);
            }
        });
        return response.writeAndFlushWith(resp);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
