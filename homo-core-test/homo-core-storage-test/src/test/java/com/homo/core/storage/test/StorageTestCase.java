package com.homo.core.storage.test;

import com.homo.core.storage.ByteStorage;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = ByteStorageTessApplication.class)
public class StorageTestCase {
    @Autowired
    ByteStorage byteStorage;
    @Autowired
    HomoSerializationProcessor serializationProcessor;
    @Autowired
    RootModule rootModule;
    public String ownerId = "default";
    public String logicType = "logicType";
    String key = "test";
    TestSaveObj saveObj;
    @BeforeAll
    public void init(){
        ownerId = UUID.randomUUID().toString();
        saveObj = new TestSaveObj(ownerId,logicType);
    }

    @Test
    @Order(1)
    public void testUpdate() throws InterruptedException {
        byte[] bytes = serializationProcessor.writeByte(saveObj);
        Map<String,byte[]> map = new HashMap<>();
        map.put(key,bytes);
        StepVerifier.create(
                byteStorage.update(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(), saveObj.getLogicType(),
                        saveObj.getOwnerId(),map)
                .nextDo(ret-> {
                    return Homo.result(ret.getKey());
                })
        )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @Order(2)
    public void testGet(){
        StepVerifier.create(
                byteStorage.get(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,key)
                .nextValue(ret->{
                    return serializationProcessor.readValue(ret, TestSaveObj.class);
                })
                .nextValue(ret->{
                    return ret.equals(saveObj);
                })
        )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @Order(3)
    public void testRemove(){
        List<String> list = new ArrayList<>();
        list.add(key);
        StepVerifier.create(
                byteStorage.removeKeys(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,list)
                        .nextValue(ret->{
                            return ret.get(0);
                        })
                        .nextValue(ret->{
                            return ret.equals(key);
                        })
                        .nextDo(ret->{
                            return byteStorage.get(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,key);
                        })
                        .nextDo(ret->{
                            return Homo.result(ret==null);
                        })
        )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @Order(4)
    public void testIncr() throws InterruptedException {
        String incrKey1 = "incrKey1";
        String incrKey2 = "incrKey2";
        String incrKey3 = "incrKey3";
        Map<String,Long> map = new HashMap();
        map.put(incrKey1,2L);
        map.put(incrKey2,1L);
        map.put(incrKey3,1L);
        StepVerifier.create(
                byteStorage.incr(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,incrKey1)
        )
                .expectNext(1L)
                .verifyComplete();
        StepVerifier.create(
                byteStorage.get(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,incrKey1)
                .nextDo(ret->{
                    return  Homo.result(Long.valueOf(new String(ret, StandardCharsets.UTF_8)));
                })
        )
                .expectNext(1L)
                .verifyComplete();
        StepVerifier.create(
                byteStorage.incr(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,ownerId,map)
                        .nextDo(ret->{
                            return  Homo.result(Tuples.of(map.get(incrKey1),map.get(incrKey2),map.get(incrKey3)));
                        })
        )
                .expectNext(Tuples.of(2L,1L,1L))
                .verifyComplete();
    }

    @Test
    @Order(5)
    public void testUpdateBatch() throws InterruptedException {

        TestSaveObj saveObj1 = new TestSaveObj("user_1",logicType);
        TestSaveObj saveObj2 = new TestSaveObj("user_2",logicType);
        TestSaveObj saveObj3 = new TestSaveObj("user_3",logicType);
        byte[] bytes1 = serializationProcessor.writeByte(saveObj1);
        byte[] bytes2 = serializationProcessor.writeByte(saveObj2);
        byte[] bytes3 = serializationProcessor.writeByte(saveObj3);
        Map<String,byte[]> map = new HashMap<>();
        map.put(key,bytes1);
        StepVerifier.create(
                byteStorage.update(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(), saveObj1.getLogicType(),
                        saveObj1.getOwnerId(),map)
                        .nextDo(ret-> {
                            return Homo.result(ret.getKey());
                        })
        )
                .expectNext(true)
                .verifyComplete();
        map.put(key,bytes2);
        StepVerifier.create(
                byteStorage.update(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(), saveObj2.getLogicType(),
                        saveObj2.getOwnerId(),map)
                        .nextDo(ret-> {
                            return Homo.result(ret.getKey());
                        })
        )
                .expectNext(true)
                .verifyComplete();
        map.put(key,bytes3);
        StepVerifier.create(
                byteStorage.update(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(), saveObj3.getLogicType(),
                        saveObj3.getOwnerId(),map)
                        .nextDo(ret-> {
                            return Homo.result(ret.getKey());
                        })
        )
                .expectNext(true)
                .verifyComplete();
    }

//    @Test
    @Order(6)
    public void testIncrLoading() {
        String incrKey1 = "incrKey1";
        String incrKey2 = "incrKey2";
        String incrKey3 = "incrKey3";
        List<String> keys = new ArrayList<>();
        keys.add(incrKey1);
        keys.add(incrKey2);
        keys.add(incrKey3);
        StepVerifier.create(
                byteStorage.get(rootModule.getServerInfo().getAppId(),rootModule.getServerInfo().getRegionId(),logicType,"78b1b220-7df1-4271-9916-3a34df90f8bb",keys)
                        .nextDo(ret->{
                            return  Homo.result(Tuples.of(Long.valueOf(new String(ret.get(incrKey1), StandardCharsets.UTF_8)),
                                    Long.valueOf(new String(ret.get(incrKey2), StandardCharsets.UTF_8)),
                                    Long.valueOf(new String(ret.get(incrKey3), StandardCharsets.UTF_8))));
                        })
        )
                .expectNext(Tuples.of(2L,1L,1L))
                .verifyComplete();
    }
}
