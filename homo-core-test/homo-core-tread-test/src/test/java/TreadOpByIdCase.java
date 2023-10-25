//import com.homo.core.facade.tread.tread.enums.ExecRet;
//import com.homo.core.tread.tread.config.TreadProperties;
//import com.homo.core.tread.tread.objTread.ObjTread;
//import com.homo.core.utils.rector.Homo;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.Assert;
//import reactor.core.publisher.Flux;
//import reactor.test.StepVerifier;
//import reactor.test.scheduler.VirtualTimeScheduler;
//import reactor.util.function.Tuple2;
//import test.*;
//
//@Slf4j
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@SpringBootTest(classes = TreadTestApplication.class)
//public class TreadOpByIdCase extends AbstractEntityServiceBase {
//    @Autowired
//    CallSystemImpl callSystem;
//    @Autowired
//    EntityMgrImpl entityMgr;
//    ITreadHandler treadHandler;
//    TreadHandler localHandler;
//    @Autowired
//    CallSystemPorxyUtil callSystemPorxyUtil;
//    @Autowired
//    TreadProperties treadProperties;
//    String userId = "";
//
//    @BeforeAll
//    void setup() throws Exception {
//        treadProperties.traceEnable = true;
//        userId = "123456";
//        log.info("setup userId_{}", userId);
//        localHandler = entityMgr.createEntityPromise(userId, TreadHandler.class, userId)
//                .block();
//        treadHandler = callSystemPorxyUtil.getEntityProxy(TreadService.class, ITreadHandler.class, userId);
//        VirtualTimeScheduler.getOrSet();
//
//    }
//
//
//    @Test
//    public void testCreateObj3SourceTypeSuccess() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testCreateAndInnerHandlerSuccess2() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler.innerHandler)
//                        .addById(1, ResourceType.INNER_BAGINFO_NUM, 3)
//                        .addById(1, ResourceType.INNER_BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.INNER_BAGINFO_QUALITY, 1)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel innerModel = localHandler.innerHandler.innerMap.get(1);
//        Assert.isTrue(innerModel != null, "itemInfoModel is null");
//        Flux<Integer> innerFlux = Flux.just(innerModel.id, innerModel.num, innerModel.level, innerModel.quality);
//        StepVerifier.create(innerFlux)
//                .expectNext(1)
//                .expectNext(3)
//                .expectNext(2)
//                .expectNext(1)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testCreateAndInnerHandlerSuccess3() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler, localHandler.innerHandler)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .addById(1, ResourceType.INNER_BAGINFO_NUM, 3)
//                        .addById(1, ResourceType.INNER_BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.INNER_BAGINFO_QUALITY, 1)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//        ItemInfoModel innerModel = localHandler.innerHandler.innerMap.get(1);
//        Assert.isTrue(innerModel != null, "itemInfoModel is null");
//        Flux<Integer> innerFlux = Flux.just(innerModel.id, innerModel.num, innerModel.level, innerModel.quality);
//        StepVerifier.create(innerFlux)
//                .expectNext(1)
//                .expectNext(3)
//                .expectNext(2)
//                .expectNext(1)
//                .verifyComplete();
//
//    }
//
//    @Test
//    public void testCreateAndInnerAndDirectSuccess1() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler, localHandler.innerHandler)
//                        .addById(1, ResourceType.INNER_BAGINFO_NUM, 3)
//                        .addById(1, ResourceType.INNER_BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.INNER_BAGINFO_QUALITY, 1)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .add(localHandler, "A", 1)
//                        .add(localHandler, "B", 1)
//                        .add(localHandler, "C", 1)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//        ItemInfoModel innerModel = localHandler.innerHandler.innerMap.get(1);
//        Assert.isTrue(innerModel != null, "itemInfoModel is null");
//        Flux<Integer> innerFlux = Flux.just(innerModel.id, innerModel.num, innerModel.level, innerModel.quality);
//        StepVerifier.create(innerFlux)
//                .expectNext(1)
//                .expectNext(3)
//                .expectNext(2)
//                .expectNext(1)
//                .verifyComplete();
//        Flux<Integer> flux2 = Flux.just(localHandler.a, localHandler.b, localHandler.c);
//        StepVerifier.create(flux2)
//                .expectNext(101)
//                .expectNext(201)
//                .expectNext(301)
//                .verifyComplete();
//
//    }
//
//    @Test
//    public void testRegisterMgrObjFail() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel == null, "itemInfoModel is null");
//    }
//
//    @Test
//    public void testRegisterMgrById() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .registerMgrObj(1, localHandler, ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterMgrById2() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1, localHandler)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2, localHandler)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3, localHandler)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterCreateObj() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterCreateObj1() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.BAGINFO_NUM, 1, () -> new ItemInfoModel(1, 1, 1, 1))
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 2, () -> new ItemInfoModel(1, 2, 2, 2))
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 3, () -> new ItemInfoModel(1, 3, 3, 3))
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .expectNext(4)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterCreateObj2() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .registerCreateFun(1, (id) -> {
//                            return new ItemInfoModel((Integer) id, 1, 1, 1);
//                        }, ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 1)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(1)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterCreateObj3() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .registerCreateFun(1, (id) -> new ItemInfoModel((Integer) id, 1, 1, 1), ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY)
//                        .addById(1, ResourceType.BAGINFO_LEVEL, 1, () -> new ItemInfoModel(1, 2, 2, 2))
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(3)
//                .expectNext(2)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterCreateObj4() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .registerCreateFun(1, (id) -> new ItemInfoModel((Integer) id, 1, 1, 1), ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY)
//                        .addById(1, ResourceType.BAGINFO_QUALITY, 1, () -> new ItemInfoModel(1, 2, 2, 2))
//                        .addById(1, ResourceType.BAGINFO_NUM, 2)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(itemInfoModel.id, itemInfoModel.num, itemInfoModel.level, itemInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(4)
//                .expectNext(2)
//                .expectNext(3)
//                .verifyComplete();
//    }
//
////    @Test
////    public void testRegisterCreateObj5() {
////        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
////        StepVerifier.create(
////                ObjTread.create(userId,localHandler,localHandler)
////                        .addById(1, ResourceType.BAGINFO_QUALITY, 1)
////                        .addById(1,ResourceType.BAGINFO_NUM,2)
////                        .execAddOnly()
////                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println))
////                        .nextValue(Tuple2::getT1)
////                        .onErrorContinue(throwable -> {
////                            throw  new RuntimeException("error");
////                        })
////        )
////                .expectError(RuntimeException.class)
////                .verify();
////    }
//
////    @Test
////    public void testRegisterCreateObj6() {
////        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
////        StepVerifier.create(
////                ObjTread.create(userId)
////                        .registerMgrObj(1,localHandler,ResourceType.BAGINFO_NUM)
////                        .registerMgrObj(1,localHandler,ResourceType.BAGINFO_NUM)
////                        .addById(1, ResourceType.BAGINFO_QUALITY, 1)
////                        .addById(1,ResourceType.BAGINFO_NUM,2)
////                        .execAddOnly()
////                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
////                        .onErrorContinue(throwable -> {
////                            throw  new RuntimeException("error");
////                        })
////        )
////                .expectError(RuntimeException.class)
////                .verify();
////    }
//
//    @Test
//    public void testSubByIdOnlyError() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .registerCreateFun(1, (id) -> new ItemInfoModel((Integer) id, 1, 1, 1), ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY)
//                        .subById(1, ResourceType.BAGINFO_QUALITY, 1)
//                        .subById(1, ResourceType.BAGINFO_NUM, 2)
//                        .execSubOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.getObjError)
//                .verifyComplete();
//        ItemInfoModel itemInfoModel = localHandler.bagMap.get(1);
//        Assert.isTrue(itemInfoModel == null, "itemInfoModel is null");
//
//    }
//
//    @Test
//    public void testRegisterMethod() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.EQUIP_NUM, 1)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        EquipInfoModel equipInfoModel = localHandler.equipMap.get(1);
//        Assert.isTrue(equipInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(equipInfoModel.id, equipInfoModel.num, equipInfoModel.level, equipInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(1)
//                .expectNext(1)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterMethodError1() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.EQUIP_NUM, 1)
//                        .addById(1, ResourceType.EQUIP_LEVEL, 2)
//                        .addById(1, ResourceType.EQUIP_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        EquipInfoModel equipInfoModel = localHandler.equipMap.get(1);
//        Assert.isTrue(equipInfoModel != null, "itemInfoModel is null");
//        Flux<Integer> flux = Flux.just(equipInfoModel.id, equipInfoModel.num, equipInfoModel.level, equipInfoModel.quality);
//        StepVerifier.create(flux)
//                .expectNext(1)
//                .expectNext(2)
//                .expectNext(1)
//                .expectNext(1)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testRegisterMethodError2() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1, ResourceType.EQUIP_LEVEL, 2)
//                        .addById(1, ResourceType.EQUIP_QUALITY, 3)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        EquipInfoModel equipInfoModel = localHandler.equipMap.get(1);
//        Assert.isTrue(equipInfoModel == null, "itemInfoModel is null");
//    }
//
//    @Test
//    public void testMultiTypeSuccess() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        localHandler.talentMap.put(2L, new TalentInfoModel(2L, 100, 100L, "100"));
//        localHandler.talentMap.put(3L, new TalentInfoModel(3L, 100, 100L, "100"));
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(2L, ResourceType.TELENT_NUM, 100)
//                        .addById(2L, ResourceType.TELENT_LEVEL, 200L)
//                        .addById(2L, ResourceType.TELENT_QUALITY, "300")
//                        .subById(2L, ResourceType.TELENT_NUM, 50)
//                        .subById(2L, ResourceType.TELENT_LEVEL, 100L)
//
//                        .addById(3L, ResourceType.TELENT_NUM, 200)
//                        .addById(3L, ResourceType.TELENT_LEVEL, 400L)
//                        .addById(3L, ResourceType.TELENT_QUALITY, "600")
//                        .subById(3L, ResourceType.TELENT_NUM, 50)
//                        .subById(3L, ResourceType.TELENT_LEVEL, 100L)
//                        .subById(3L, ResourceType.TELENT_QUALITY, "500")
//                        .exec()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        TalentInfoModel talentInfoModel1 = localHandler.talentMap.get(2L);
//        TalentInfoModel talentInfoModel2 = localHandler.talentMap.get(3L);
//        Assert.isTrue(talentInfoModel1 != null, "talentInfoModel1 is null");
//        Assert.isTrue(talentInfoModel2 != null, "talentInfoModel2 is null");
//        Assert.isTrue(talentInfoModel1.num == 150 && "300".equals(talentInfoModel1.quality) && talentInfoModel1.level == 200L, "talentInfoModel1 is false");
//        Assert.isTrue(talentInfoModel2.num == 250 && "600".equals(talentInfoModel2.quality) && talentInfoModel2.level == 400L, "talentInfoModel2 is false");
//    }
//
//    @Test
//    public void testCreateObjMethodError() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1L, ResourceType.TELENT_LEVEL_2, 2L)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.createObjError)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testGetObjMethodError() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1L, ResourceType.TELENT_QUALITY_2, "test")
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.getObjError)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSetObjMethodError() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1L, ResourceType.TELENT_NUM_2, 2)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.setObjError)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSetObjMethodParamError1() {
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(
//                ObjTread.create(userId, localHandler)
//                        .addById(1L, ResourceType.TELENT_NUM_3, 2)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//    }
//
////    @Test  //参数不匹配测试
////    public void testSetObjMethodParamError2() {
////        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
////        StepVerifier.create(
////                ObjTread.create(userId, localHandler)
////                        .addById(1L, ResourceType.TELENT_LEVEL_3, 2)
////                        .execAddOnly()
////                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
////        )
////                .expectNext(ExecRet.sysError)
////                .verifyComplete();
////    }
////
////    @Test
////    public void testSetObjMethodParamError3() {
////        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
////        StepVerifier.create(
////                ObjTread.create(userId, localHandler)
////                        .addById(1L, ResourceType.TELENT_QUALITY_3, "2")
////                        .execAddOnly()
////                        .nextDo(ret -> Homo.result(ret).consumerValue(System.out::println)).nextValue(Tuple2::getT1)
////        )
////                .expectNext(ExecRet.sysError)
////                .verifyComplete();
////    }
//}
