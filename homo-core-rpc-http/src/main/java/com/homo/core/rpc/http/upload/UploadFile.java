package com.homo.core.rpc.http.upload;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

public interface UploadFile extends Serializable {
    String filename();
    Map<String, String> headers();
    MultiValueMap<String, String> queryParams();
    Mono<MultiValueMap<String, String>> formData();
    Flux<DataBuffer> content();
}
