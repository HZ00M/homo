//
//import com.homo.core.facade.tread.tread.enums.ExecRet;
//import com.homo.core.tread.tread.BoolTread;
//import com.homo.core.tread.tread.config.TreadProperties;
//import com.homo.core.tread.tread.intTread.IntTread;
//import com.homo.core.tread.tread.longTread.LongTread;
//import com.homo.core.tread.tread.objTread.ObjTread;
//import com.homo.core.tread.tread.stringTread.StringTread;
//import com.homo.core.utils.rector.Homo;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import reactor.core.publisher.Flux;
//import reactor.test.StepVerifier;
//import reactor.test.scheduler.VirtualTimeScheduler;
//import reactor.util.function.Tuples;
//import test.*;
//
//@Slf4j
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@SpringBootTest(classes = TreadTestApplication.class)
//public class TreadOpByObjCase extends AbstractEntityServiceBase {
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
//        log.info("setup userId {}", userId);
//        localHandler = entityMgr.createEntityPromise(userId, TreadHandler.class, userId)
//                .block();
//        treadHandler =
//                callSystemPorxyUtil.getEntityProxy(TreadService.class, ITreadHandler.class, userId);
//        VirtualTimeScheduler.getOrSet();
//    }
//
//
//    @Test
//    public void testUpLevelGunSuccess() {
//
//        treadHandler.addMaterial(new Material(1, 100));
//        treadHandler.addGun(new Gun(1, 1, 50, 1));
//        StepVerifier.create(treadHandler.upLevelGun(1, 2, 1, 20, 100))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        StepVerifier.create(treadHandler.getMaterCount(1))
//                .expectNext(80)
//                .verifyComplete();
//        StepVerifier.create(treadHandler.getGunLevel(1))
//                .expectNext(3)
//                .verifyComplete();
//        StepVerifier.create(treadHandler.getGunDamage(1))
//                .expectNext(150)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSuccess() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"D", 99)
//                .add(localHandler,"E", 99, (op, check) -> op < 100)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.d, localHandler.e);
//        StepVerifier.create(flux)
//                .expectNext(301)
//                .expectNext(599)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubLittle1Fail() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"A", -1)
//                .sub(localHandler,"B", 2)
//                .add(localHandler,"C", 3)
//                .add(localHandler,"D", 4)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.subCheckFail)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testAddNegativeFail() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"A", 1)
//                .sub(localHandler,"B", 2)
//                .add(localHandler,"C", -1)
//                .add(localHandler,"D", 4)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.addCheckFail)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d);
//        StepVerifier.create(flux)
//                .expectNext(99)
//                .expectNext(198)
//                .expectNext(300)
//                .expectNext(400)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testChangePredicateSubCheckFail() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"A", 1, (opValue, getValue) -> opValue % 2 == 0)
//                .sub(localHandler,"B", 2)
//                .add(localHandler,"C", 3)
//                .add(localHandler,"D", 4)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.subCheckFail)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testChangePredicateAddCheckFail() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"A", 1)
//                .sub(localHandler,"B", 2)
//                .add(localHandler,"C", 3, (opValue, getValue) -> opValue > 3)
//                .add(localHandler,"D", 4)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.addCheckFail)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d);
//        StepVerifier.create(flux)
//                .expectNext(99)
//                .expectNext(198)
//                .expectNext(300)
//                .expectNext(400)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubABAddCD() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(
//                IntTread.create(userId)
//                        .sub(localHandler,"A", 1)
//                        .sub(localHandler,"B", 2)
//                        .add(localHandler,"C", 3)
//                        .add(localHandler,"D", 4)
//                        .exec()
//                        .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d);
//        StepVerifier.create(flux)
//                .expectNext(99)
//                .expectNext(198)
//                .expectNext(303)
//                .expectNext(404)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testAddError() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"D", 99)
//                .sub(localHandler,"E", 20)
//                .add(localHandler,"F", 50)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.addError)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.d, localHandler.e, localHandler.f);
//        StepVerifier.create(flux)
//                .expectNext(301)
//                .expectNext(480)
//                .expectNext(600)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubError1() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .add(localHandler,"D", 99)
//                .sub(localHandler,"E", 20)
//                .sub(localHandler,"F", 50)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.subError)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.d, localHandler.e, localHandler.f);
//        StepVerifier.create(flux)
//                .expectNext(400)
//                .expectNext(480)
//                .expectNext(600)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubError2() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"F", 50)
//                .sub(localHandler,"D", 99)
//                .add(localHandler,"E", 20)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.subError)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.d, localHandler.e, localHandler.f);
//        StepVerifier.create(flux)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .verifyComplete();
//    }
//
//    @Test
//    public void addError() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"E", 99)
//                .add(localHandler,"F", 50)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.addError)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.e, localHandler.f, localHandler.g);
//        StepVerifier.create(flux)
//                .expectNext(401)
//                .expectNext(600)
//                .expectNext(700)
//                .verifyComplete();
//    }
//
//    @Test
//    public void subError() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700);
//        StepVerifier.create(IntTread.create(userId)
//                .sub(localHandler,"G", 20)
//                .sub(localHandler,"E", 99)
//                .sub(localHandler,"F", 50)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        Flux<Integer> flux = Flux.just(localHandler.e, localHandler.f, localHandler.g);
//        StepVerifier.create(flux)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testLong() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 1L, null, null);
//        StepVerifier.create(LongTread.create(userId)
//                .sub(localHandler,"x", 1L)
//                .add(localHandler,"x", 100L)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.x);
//        StepVerifier.create(flux)
//                .expectNext(100L)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testBool() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(BoolTread.create(userId)
//                .sub(localHandler,"y", true)
//                .add(localHandler,"y", true)
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.y);
//        StepVerifier.create(flux)
//                .expectNext(true)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testString() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, null, null, null);
//        StepVerifier.create(StringTread.create(userId)
//                .sub(localHandler,"z", "123")
//                .add(localHandler,"z", "123")
//                .exec()
//                .nextDo(ret -> Homo.result(ret.getT1())))
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testMuLti() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                Homo.all(
//                        LongTread.create(userId).sub(localHandler,"x", 9999L).add(localHandler,"x", 1L).exec().nextDo(ret -> Homo.result(ret.getT1())),
//                        BoolTread.create(userId).sub(localHandler,"y", false).add(localHandler,"y", false).exec().nextDo(ret -> Homo.result(ret.getT1())),
//                        StringTread.create(userId).sub(localHandler,"z", "旧数据").add(localHandler,"z", "新数据").exec().nextDo(ret -> Homo.result(ret.getT1()))
//                )
//        )
//                .expectNext(Tuples.of(ExecRet.ok, ExecRet.ok, ExecRet.ok))
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(2L)
//                .expectNext(false)
//                .expectNext("新数据")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testObjTread() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .add(localHandler,"A", 100)
//                        .add(localHandler,"B", 200)
//                        .add(localHandler,"C", 300)
//                        .sub(localHandler,"D", 400)
//                        .sub(localHandler,"x", 9999L)
//                        .sub(localHandler,"y", false)
//                        .sub(localHandler,"z", "字符串")
//                        .exec()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(200)
//                .expectNext(400)
//                .expectNext(600)
//                .expectNext(0)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(1L)
//                .expectNext(false)
//                .expectNext("字符串")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testObjTreadFail() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .add(localHandler,"A", -1)
//                        .add(localHandler,"B", 200)
//                        .add(localHandler,"C", 300)
//                        .sub(localHandler,"D", 400)
//                        .sub(localHandler,"x", 9999L)
//                        .sub(localHandler,"y", false)
//                        .sub(localHandler,"z", "456")
//                        .exec()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.addCheckFail)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(0)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(1L)
//                .expectNext(false)
//                .expectNext("456")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testAddOnly() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .add(localHandler,"A", 1)
//                        .add(localHandler,"B", 200)
//                        .add(localHandler,"C", 300)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(101)
//                .expectNext(400)
//                .expectNext(600)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testAddOnlyError() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .add(localHandler,"A", 1)
//                        .add(localHandler,"B", 200)
//                        .add(localHandler,"C", 300)
//                        .sub(localHandler,"D", 100)
//                        .execAddOnly()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubOnly() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .sub(localHandler,"A", 1)
//                        .sub(localHandler,"B", 200)
//                        .sub(localHandler,"C", 300)
//                        .execSubOnly()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(99)
//                .expectNext(0)
//                .expectNext(0)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testSubOnlyError() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .sub(localHandler,"A", 1)
//                        .sub(localHandler,"B", 200)
//                        .sub(localHandler,"C", 300)
//                        .add(localHandler,"D", 400)
//                        .execSubOnly()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.sysError)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testConsumer() {
//
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .sub(localHandler,"A", 1, newValue -> {
//                            localHandler.a += 1;
//                        })
//                        .sub(localHandler,"B", 200, newValue -> {
//                            localHandler.b += 200;
//                        })
//                        .sub(localHandler,"C", 300, newValue -> {
//                            localHandler.c += 300;
//                        })
//                        .add(localHandler,"D", 400, newValue -> {
//                            localHandler.d -= 400;
//                        })
//                        .exec()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//
//    @Test
//    public void testExeUnsafeSuccess() {
//        treadProperties.traceEnable = true;
//        localHandler.reset(100, 200, 300, 400, 500, 600, 700, 10000L, true, "123");
//
//        StepVerifier.create(
//                ObjTread.create(userId)
//                        .execUnSafe()
//                        .nextDo(ret -> Homo.result(ret.getT1()))
//        )
//                .expectNext(ExecRet.ok)
//                .verifyComplete();
//        Flux<Object> flux = Flux.just(localHandler.a, localHandler.b, localHandler.c, localHandler.d, localHandler.e, localHandler.f, localHandler.g, localHandler.x, localHandler.y, localHandler.z);
//        StepVerifier.create(flux)
//                .expectNext(100)
//                .expectNext(200)
//                .expectNext(300)
//                .expectNext(400)
//                .expectNext(500)
//                .expectNext(600)
//                .expectNext(700)
//                .expectNext(10000L)
//                .expectNext(true)
//                .expectNext("123")
//                .verifyComplete();
//    }
//}
