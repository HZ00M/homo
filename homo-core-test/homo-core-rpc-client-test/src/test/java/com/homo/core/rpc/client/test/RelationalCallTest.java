package com.homo.core.rpc.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.relational.facade.DrawCardFacade;
import io.homo.proto.relational.test.*;
import io.homo.proto.rpc.HttpHeadInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(classes = TestRpcClientApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RelationalCallTest {
    @Autowired(required = false)
    DrawCardFacade cardFacade;
    @Autowired
    private WebTestClient webClient;

    @Test
    public void saveTest() throws InterruptedException {
        DrawCardPb drawCardPb = DrawCardPb.newBuilder().setPoolId(1).setUserId("123").build();
        SaveDrawCardReq req = SaveDrawCardReq.newBuilder().setDrawCard(drawCardPb).build();
        HttpHeadInfo headInfo = HttpHeadInfo.newBuilder().build();
        StepVerifier.create(cardFacade.save(req, headInfo))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void insertTest() {
        DrawCardPb drawCardPb = DrawCardPb.newBuilder().setPoolId(2).setUserId("444").build();
        InsertDrawCardReq req = InsertDrawCardReq.newBuilder().setDrawCard(drawCardPb).build();
        StepVerifier.create(cardFacade.insert(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void insertBatchTest() {
        List<DrawCardPb> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DrawCardPb drawCardPb = DrawCardPb.newBuilder().setPoolId(i).setUserId("123" + i).build();
            list.add(drawCardPb);
        }
        InsertsDrawCardReq req = InsertsDrawCardReq.newBuilder().addAllDrawCard(list).build();
        StepVerifier.create(cardFacade.inserts(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void findAllTest() {
        QueryDrawCardReq req = QueryDrawCardReq.newBuilder().addIds(1).addIds(2).build();
        StepVerifier.create(cardFacade.queryFindAll(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void findOneTest() {
        QueryDrawCardReq req = QueryDrawCardReq.newBuilder().addIds(1).addIds(2).build();
        StepVerifier.create(cardFacade.queryFindOne(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void findExistTest() {
        QueryDrawCardReq req = QueryDrawCardReq.newBuilder().addIds(1).addIds(2).build();
        StepVerifier.create(cardFacade.queryFindExists(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void deleteTest() {
        DeleteDrawCardReq req = DeleteDrawCardReq.newBuilder().addIds(1).addIds(2).build();
        StepVerifier.create(cardFacade.delete(req) )
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void updateEntityTest() {
        DrawCardPb drawCardPb = DrawCardPb.newBuilder().setId(1).setPoolId(33).setUserId("1232222").build();
        UpdateDrawCardReq req = UpdateDrawCardReq.newBuilder().setDrawCard(drawCardPb).build();
        StepVerifier.create(cardFacade.updateEntity(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void updateTest() {
        DrawCardPb drawCardPb = DrawCardPb.newBuilder().setId(1).setPoolId(33).setUserId("1232222").build();
        UpdateDrawCardReq req = UpdateDrawCardReq.newBuilder().setDrawCard(drawCardPb).build();
        StepVerifier.create(cardFacade.update(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void aggregateTest() {
        DrawCardPb drawCardPb = DrawCardPb.newBuilder().setId(1).setPoolId(33).setUserId("1232222").build();
        AggregateReq req = AggregateReq.newBuilder().build();
        StepVerifier.create(cardFacade.aggregate(req))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return resp.getCode() == 0;
                })
                .verifyComplete();
    }

    @Test
    public void updateRecordJson() throws InterruptedException {
        JSONObject req = new JSONObject();
        req.put("3", "444");
        StepVerifier.create(cardFacade.updateRecord(req, null))
                .expectNextMatches(resp -> {
                    // 验证返回的 drawCard 是否符合预期
                    log.info(" resp {}", resp);
                    return true;
                })
                .verifyComplete();
    }


    @Test
    public void uploadFile() throws InterruptedException {
        File file = new File("src/main/resources/file/test.txt");  // 文件路径
        log.info("uploadFile start ");
        webClient.post()
                .uri("http://relational-service:30022/uploadFile")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new FileSystemResource(file)))  // 上传文件
                .headers(httpHeaders -> {
                    httpHeaders.add("token", "123");
                })
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    // 提取响应数据并转成字符串
                    byte[] responseBody = response.getResponseBody();
                    if (responseBody != null) {
                        String responseString = new String(responseBody, StandardCharsets.UTF_8);
                        System.out.println("Response Body: " + responseString); // 打印响应内容
                    } else {
                        System.out.println("Response Body is null.");
                    }
                });
    }
}
