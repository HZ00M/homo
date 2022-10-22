package com.homo.core.rpc.http.upload;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

public class DefaultUploadFile implements UploadFile, Serializable {

    String filename  ;
    Map<String,String> headers;
    MultiValueMap<String,String> queryParams;
    Mono<MultiValueMap<String, String>> formData;
    Flux<DataBuffer> content;

    public DefaultUploadFile(String filename, Map<String, String> headers, MultiValueMap<String, String> queryParams, Mono<MultiValueMap<String, String>> formData, Flux<DataBuffer> content) {
        this.filename = filename;
        this.headers = headers;
        this.queryParams = queryParams;
        this.formData = formData;
        this.content = content;
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public MultiValueMap<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public Mono<MultiValueMap<String, String>> formData() {
        return formData;
    }

    @Override
    public Flux<DataBuffer> content() {
        return content;
    }
}
