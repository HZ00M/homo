package com.homo.core.rpc.http.mapping;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.homo.core.rpc.base.serial.FileRpcContent;
import com.homo.core.rpc.http.HttpServer;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.serial.FSTSerializationProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import com.homo.core.utils.upload.DefaultUploadFile;
import io.homo.proto.client.ClientRouterHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
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
    public void moduleInit() {
        log.info("DeFaultHttpMapping init start");
        super.moduleInit();
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
        Map<String, String> formDataParams = request.getHeaders().toSingleValueMap();
        Span span = ZipkinUtil.currentSpan();
        JSONObject headerInfo = exportHeaderInfo(request);
        List<Object> list = new ArrayList<>();
        list.add(formDataParams);
        list.add(headerInfo);
        String msg = JSON.toJSONString(list);
        log.info("httpGet begin port {} msgId {} msg {}", port, msgId, msg);
        HttpServer httpServer = routerHttpServerMap.get(port);
        Mono<DataBuffer> respBuffer = httpServer.onJsonCall(msgId, msg, response);
        return response.writeAndFlushWith(Mono.just(respBuffer))
                .doFinally(ret -> {
                    if (span != null) {
                        span.annotate(ZipkinUtil.SERVER_SEND_TAG);
                        span.finish();
                    }
                });
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
        JSONObject headerInfo = exportHeaderInfo(request);
        Span span = ZipkinUtil.currentSpan();
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
                                        list.add(headerInfo);
                                        if (type == JSONValidator.Type.Array) {
                                            //参数是列表(headerInfo,JSONArray)
                                            JSONArray bodyArr = JSON.parseArray(reqStr);
                                            list.add(bodyArr);
                                            //参数是列表(headerInfo,json1,json2,...,)
//                                            for (Object item : bodyArr) {
//                                                list.add(item);
//                                            }
                                        } else if (type == JSONValidator.Type.Object) {
                                            //参数是单个json (headerInfo,json)
                                            JSONObject bodyJson = JSON.parseObject(reqStr);
                                            list.add(bodyJson);
                                        } else {
                                            //参数是字符串(headerInfo,reqStr)
                                            list.add(reqStr);
                                        }


                                        msg = JSON.toJSONString(list);
                                        log.info("httpJsonPost begin port {} msgId {} msg {}", port, msgId, msg);
                                        HttpServer httpServer = routerHttpServerMap.get(port);
                                        Mono<DataBuffer> bufferMono = httpServer.onJsonCall(msgId, msg, response);
                                        monoSink.success(bufferMono);
                                    } catch (Exception e) {
                                        monoSink.error(e);
                                    }
                                }
                        ));
        return response.writeAndFlushWith(resp)
                .doFinally(ret -> {
                    if (span != null) {
                        span.annotate(ZipkinUtil.SERVER_SEND_TAG);
                        span.finish();
                    }
                });
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
        Map<String, String> headerInfo = request.getHeaders().toSingleValueMap();
        Span span = ZipkinUtil.currentSpan();
        Mono<Mono<DataBuffer>> resp = DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(new DefaultDataBufferFactory().allocateBuffer(0)) // 创建一个空的 DataBuffer 作为默认值
                .flatMap(dataBuffer ->
                        Mono.create(monoSink -> {
                            try {
                                checkDataBufferSize(dataBuffer);
                                byte[] protoData = new byte[dataBuffer.readableByteCount()];

                                // ClientRouterHeader在反序列化时，可以被序列化成相同pb结构的HttpHeadInfo
                                /**
                                 * message ClientRouterHeader{
                                 *   map<string,string> headers = 1;
                                 * }
                                 * message HttpHeadInfo{
                                 *   map<string,string> headers = 1;
                                 * }
                                 */
                                //参数格式 (http头信息,pb协议)
                                ClientRouterHeader routerHeader = ClientRouterHeader.newBuilder()
                                        .putAllHeaders(headerInfo).build();
                                byte[][] msg = {routerHeader.toByteArray(), protoData};
                                log.info("httpProtoPost begin port {} msgId {} ", port, msgId);
                                dataBuffer.read(protoData);
                                HttpServer httpServer = routerHttpServerMap.get(port);
                                Mono<DataBuffer> bufferMono = httpServer.onBytesCall(msgId, msg, response);
                                monoSink.success(bufferMono);
                            } catch (Exception e) {
                                monoSink.error(e);
                            }
                        })
                );

        return response.writeAndFlushWith(resp)
                .doFinally(ret -> {
                    if (span != null) {
                        span.annotate(ZipkinUtil.SERVER_SEND_TAG);
                        span.finish();
                    }
                });
    }

    /**
     * 上传文件
     *
     * @param exchange
     * @param filePart 上传的文件内容 //todo 验证文件上传
     * @return
     */
    @PostMapping(value = "/**", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> httpFormBody(ServerWebExchange exchange, @RequestPart("file") FilePart filePart) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        int port = exportPort(request);
        String msgId = exportMsgId(request);
        return exchange.getMultipartData() // 解析multipart数据
                .flatMap(multiValueMap -> {
                    // 解析form-data中的字段
                    MultiValueMap<String, String> formDataMap = new LinkedMultiValueMap<>();
                    multiValueMap.forEach((key, parts) -> {
                        if (parts.get(0) instanceof FormFieldPart) {
                            List<String> values = parts.stream()
                                    .map(part -> ((FormFieldPart) part).value())
                                    .collect(Collectors.toList());
                            formDataMap.put(key, values);
                        }
                    });

                    // 准备文件信息
                    String filename = filePart.filename();
                    Flux<DataBuffer> content = filePart.content();
                    Map<String, String> headers = request.getHeaders().toSingleValueMap();

                    Span span = ZipkinUtil.nextOrCreateSRSpan(); // Zipkin追踪
                    DefaultUploadFile uploadFile = new DefaultUploadFile(filename, headers, request.getQueryParams(), formDataMap, content);

                    FileRpcContent fileRpcContent = new FileRpcContent();
                    fileRpcContent.setMsgId(msgId);
                    fileRpcContent.setSpan(span);
                    fileRpcContent.setParam(uploadFile);

                    // 调用路由服务处理文件上传
                    Mono<DataBuffer> uploadResult;
                    try {
                        uploadResult = routerHttpServerMap.get(port).onFileUpload(msgId, fileRpcContent, response);
                    } catch (Exception e) {
                        log.error("File upload failed, msgId: {}, error: {}", msgId, e.getMessage(), e);
                        return Mono.error(e);
                    }

                    // 返回响应
                    return response.writeAndFlushWith(Mono.just(uploadResult.flux()));
                })
                .doOnError(e -> log.error("Failed to handle file upload: {}", e.getMessage(), e))
                .then();
//        Mono<Mono<DataBuffer>> resp = Mono.create(monoMonoSink -> {
//            // 获取queryParams（来自URL）和formData（来自POST的数据）
//            MultiValueMap<String, String> params = request.getQueryParams();
//            Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
//            // 把form-data模式中的参数，除了文件类型之外取出来放到formData
//            // 解析multipart数据，提取form-data模式的字段
//            Mono<MultiValueMap<String, String>> multipartData = exchange.getMultipartData()
//                    .map(multiValueMap -> {
//                        MultiValueMap<String, String> mmMap = new LinkedMultiValueMap<>();
//                        multiValueMap.forEach((key, partList) -> {
//                            if (partList.get(0) instanceof FormFieldPart) {
//                                List<String> paramList = partList.stream().map(item -> ((FormFieldPart) item).value()).collect(Collectors.toList());
//                                mmMap.put(key, paramList);
//                            }
//                        });
//                        return mmMap;
//                    });
//            // 合并formData和multipartData
//            Mono<MultiValueMap<String, String>> resultData =
//                    Mono.just(new LinkedMultiValueMap<String, String>())
//                            .flatMap(linkMap ->
//                                    //flatMap从另一个publisher获取，（异步的转换发布的元素并返回一个新的Mono，被转换的元素和新Mono是动态绑定的。）
//                                    formData.map(map -> {
//                                        linkMap.putAll(map);
//                                        return linkMap;
//                                    })
//                            ).flatMap(linkMap ->
//                                    multipartData.map(map -> {
//                                                linkMap.putAll(map);
//                                                return linkMap;
//                                            }
//                                    )
//                            );
//            // 从请求头中获取文件相关信息
//            Map<String, String> headers = request.getHeaders().toSingleValueMap();
//            String filename = filePart.filename();
//            Flux<DataBuffer> content = filePart.content();
//            Span span = ZipkinUtil.nextOrCreateSRSpan();
//            DefaultUploadFile uploadFile = new DefaultUploadFile(filename, headers, params, resultData, content);
//            FileRpcContent fileRpcContent = new FileRpcContent();
//            fileRpcContent.setId(msgId);
//            fileRpcContent.setSpan(span);
//            fileRpcContent.setParam(uploadFile);
//            try {
//                Mono<DataBuffer> dataBufferMono = routerHttpServerMap.get(port).onFileUpload(msgId, fileRpcContent, response);
//                monoMonoSink.success(dataBufferMono);
//            } catch (Exception e) {
//                monoMonoSink.error(e);
//            }
//        });
//        return response.writeAndFlushWith(resp);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 分割‘/’，‘-’字符，首字符大写，拼接成msgId
     * @param request
     * @return
     */
    public static String exportMsgId(ServerHttpRequest request){
        String url = request.getURI().getPath();
        url = url.substring(1);
        url = url.replace("-", "/");
        String[] split = url.split("/");
        StringBuilder msgIdBuilder = new StringBuilder(split[0]);
        for(int i = 1; i < split.length; i++){
//            String tmp = StringUtils.toUpperCase4Index(split[i]);
//            msgIdBuilder.append(tmp);
            msgIdBuilder.append(split[i]);
        }
        return msgIdBuilder.toString();
    }
}
