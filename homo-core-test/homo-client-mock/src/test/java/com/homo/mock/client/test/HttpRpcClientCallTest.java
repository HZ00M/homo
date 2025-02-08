package com.homo.mock.client.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import com.homo.mock.client.ClientMockApplication;
import io.homo.demo.proto.HttpServerRequestPb;
import io.homo.demo.proto.HttpServerResponsePb;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashMap;

@Slf4j
@SpringBootTest(classes = ClientMockApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@AutoConfigureMockMvc
public class HttpRpcClientCallTest {
//    @Autowired
//    WebApplicationContext webApplicationContext;
//    @Autowired
//    private MockMvc mockMvc;  //mockMvc是web相关支持，webflux不能使用
//    @Before
//    public void setUp() throws Exception {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }
    public String targetServiceUrl = "http://http-rpc-demo-server:30300";

    @Autowired
    private WebTestClient webClient;
    @Autowired
    FastjsonSerializationProcessor processor;
    @Test
    public void jsonGetJson(){

        byte[] responseBody = webClient.get().uri(targetServiceUrl +"/jsonGetJson")
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).returnResult().getResponseBody();
        JSONObject jsonObject = processor.readValue(responseBody, JSONObject.class);
        log.info("jsonGetJson responseBody {}",jsonObject);
    }

    @Test
    public void jsonPost(){
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("name","1");
        params.add("name","2");
        params.add("age","2");
        byte[] responseBody = webClient.post().uri(targetServiceUrl +"/jsonPost")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(params))
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).returnResult().getResponseBody();
        String readValue = processor.readValue(responseBody, String.class);
        log.info("jsonPost responseBody {}",readValue);
    }

    @Test
    public void jsonPostArray(){
        HashMap map = new HashMap<>();
        map.put("key","value");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(new JSONObject(map));
        jsonArray.add(new JSONObject(map));
        byte[] responseBody = webClient.post().uri(targetServiceUrl +"/jsonPostArray")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonArray))
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).returnResult().getResponseBody();
        String readValue = processor.readValue(responseBody, String.class);
        log.info("jsonPostArray responseBody {}",readValue);
    }

    @Test
    public void valuePost(){

        byte[] responseBody = webClient.post().uri(targetServiceUrl +"/valuePost")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("value"))
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).returnResult().getResponseBody();
        String readValue = processor.readValue(responseBody, String.class);
        log.info("postValue responseBody {}",readValue);
    }

    @Test
    public void pbPost() throws InterruptedException {
        HttpServerRequestPb request = HttpServerRequestPb.newBuilder().setParam("123").build();
        HttpServerResponsePb responseBody = webClient.post().uri(targetServiceUrl +"/pbPost")
                .body(BodyInserters.fromValue(request))
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(HttpServerResponsePb.class).returnResult().getResponseBody();
        log.info("pbPost responseBody {}",responseBody);
    }


}
