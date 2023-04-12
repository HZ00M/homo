
import com.homo.core.tread.processor.Processes;
import com.homo.core.tread.processor.ResourceMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;
import test.OpAttrModel;
import test.ResourceType;
import test.TreadHandler;
import test.TreadTestApplication;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TreadTestApplication.class)
public class OpTestCase {

    static class TestItem {
        //        String s;
        Integer i;
        Boolean b;
    }

    static class TestOwner {
        Map<Integer, TestItem> itemMap = new HashMap<>();
        //        String s;
        Integer i;
        Boolean b;
    }

    @BeforeAll
    static void init() throws Exception {


        ResourceMgr.registerResourceCheckFun("i", TestOwner.class, ((opValue, newValue, owner, resourceInfos) -> (Integer) newValue <= 100), "must low 100");

        ResourceMgr.registerResourceGetFun("i", Integer.class, TestOwner.class, (owner, resourceInfos) -> Homo.result(((TestOwner) (owner)).i));

        ResourceMgr.registerResourceSetFun("i", Integer.class, TestOwner.class, (opValue, owner, param) -> {
            ((TestOwner) (owner)).i = (Integer) opValue;
            return Homo.result(true);
        });

        ResourceMgr.registerResourceGetFun("item_i", Integer.class, TestOwner.class, (owner, resourceInfos) -> {
            TestItem testItem = ((TestOwner) (owner)).itemMap.get(resourceInfos[0]);
            if (testItem == null) {
                return Homo.result(null);
            }
            return Homo.result(testItem.i);
        });
        ResourceMgr.registerResourceSetFun("item_i", Integer.class, TestOwner.class, (opValue, owner, resourceInfos) -> {
            if (opValue == null || (Integer) opValue <= 0) {
                ((TestOwner) (owner)).itemMap.remove((Integer) resourceInfos[0]);
                return Homo.result(true);
            }

            TestItem testItem = ((TestOwner) (owner)).itemMap.get(resourceInfos[0]);
            return Homo.result(testItem).nextValue(item -> {
                if (item == null) {
                    TestItem newItem = new TestItem();
                    ((TestOwner) (owner)).itemMap.put((Integer) resourceInfos[0], newItem);
                    return newItem;
                } else {
                    return item;
                }
            }).nextValue(item -> {
                item.i = (Integer) opValue;
                return true;
            });
        });
    }

    @Test
    void test1() throws Exception {
        TestOwner owner = new TestOwner();
        Processes processes = Processes.create(owner).setTraceInfo("userId_123");
        Assertions.assertEquals(Boolean.FALSE, processes
                .add(10, "i", 1)
                .add(5L, "i")
                .sub(20, "item_i", 1)
                .add(10, "item_i", 20)
                .exec()
                .consumerValue(rel -> {
                    log.info("{}", rel);
                }).block());
        owner.i = 5;
        owner.itemMap.put(10, new TestItem());
        owner.itemMap.get(10).i = 20;
        processes = Processes.create(owner)
                .setSubOwner(owner)
                .setAddOwner(owner);
        Assertions.assertEquals(Boolean.TRUE, processes
                .add(10, "i")
                .add(5, "i")
                .sub(20, "item_i", 10)
                .add(10, "item_i", 20)
                .exec()
                .consumerValue(rel -> {
                    log.info("{}", rel);
                }).block());
        Assertions.assertEquals(20, owner.i);
        Assertions.assertNull(owner.itemMap.get(10));
        Assertions.assertEquals(10, owner.itemMap.get(20).i);

        processes = Processes.create(owner);
        Assertions.assertEquals(Boolean.FALSE, processes
                .add(100, "i", 1)
                .exec()
                .consumerValue(rel -> {
                    log.info("{}", rel);
                }).block());
        Assertions.assertEquals(20, owner.i);
        Assertions.assertNull(owner.itemMap.get(10));
        Assertions.assertEquals(10, owner.itemMap.get(20).i);

    }


    @Test
    public void test1ModelSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("userId_123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(99, ResourceType.OP_SUB_MODEL_NUM, 2)
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(99, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals(99, treadHandler.opModel.getSubModelById(2).getNum());
    }

    @Test
    public void test1ModelSubZero() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(99, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertNull(treadHandler.opModel.getSubModelById(1));

    }

    @Test
    public void test1ModelLittleFail() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opModel.addSubModel(2, 200, 200L, false, "测试道具2");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.FALSE,
                processes
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(100, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(100, ResourceType.OP_SUB_MODEL_NUM, 2)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(99, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals(200, treadHandler.opModel.getSubModelById(2).getNum());
    }


    @Test
    public void test1ModelCreate() {
        TreadHandler treadHandler = new TreadHandler("123");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .add("测试字符串", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .add(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .add(100, ResourceType.OP_SUB_MODEL_NUM, 2)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("onErrorContinue error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(1, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(10L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());
        Assertions.assertEquals(100, treadHandler.opModel.getSubModelById(2).getNum());
    }

    @Test
    public void test1ModelChangeSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .add("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .add(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(101, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(110L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());
    }

    @Test
    public void test1ModelChangeSuccessLocalAttr() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(111, ResourceType.OP_ATTR)
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .add("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .add(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(111, treadHandler.opModel.getAttr());
        Assertions.assertEquals(101, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(110L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());
    }

    @Test
    public void test2ModelAddSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "defaultDesc");
        Processes processes = Processes.create(treadHandler.opModel, treadHandler.opAttrModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(111, ResourceType.OP_ATTR)
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .add("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .add(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .add(1, ResourceType.ATTR_MODEL_NUM)
                        .add(1L, ResourceType.ATTR_MODEL_LEVEL)
                        .add(false, ResourceType.ATTR_MODEL_LOCK)
                        .add("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(111, treadHandler.opModel.getAttr());
        Assertions.assertEquals(101, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(110L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());

        Assertions.assertEquals(301, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(301L, treadHandler.opAttrModel.getLevel());
        Assertions.assertFalse(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("attr", treadHandler.opAttrModel.getDesc());
    }

    @Test
    public void test2ModelSubSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.setAttr(100);
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "defaultDesc");
        Processes processes = Processes.create(treadHandler.opModel, treadHandler.opAttrModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .sub(99, ResourceType.OP_ATTR)
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .sub(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .sub(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .sub(1, ResourceType.ATTR_MODEL_NUM)
                        .sub(1L, ResourceType.ATTR_MODEL_LEVEL)
                        .sub(false, ResourceType.ATTR_MODEL_LOCK)
                        .sub("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(1, treadHandler.opModel.getAttr());
        Assertions.assertEquals(99, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(90L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());

        Assertions.assertEquals(299, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(299L, treadHandler.opAttrModel.getLevel());
        Assertions.assertFalse(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("attr", treadHandler.opAttrModel.getDesc());
    }

    @Test
    public void test2ModelSubAndAddSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.setAttr(100);
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "defaultDesc");
        Processes processes = Processes.create(treadHandler.opModel, treadHandler.opAttrModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .sub(99, ResourceType.OP_ATTR)
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .sub(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .add(1, ResourceType.ATTR_MODEL_NUM)
                        .sub(1L, ResourceType.ATTR_MODEL_LEVEL)
                        .add(false, ResourceType.ATTR_MODEL_LOCK)
                        .sub("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(1, treadHandler.opModel.getAttr());
        Assertions.assertEquals(101, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(110L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());

        Assertions.assertEquals(301, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(299L, treadHandler.opAttrModel.getLevel());
        Assertions.assertFalse(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("attr", treadHandler.opAttrModel.getDesc());
    }


    @Test
    public void test2ModelSubFail1() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.setAttr(100);
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "defaultDesc");
        Processes processes = Processes.create(treadHandler.opModel, treadHandler.opAttrModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.FALSE,
                processes
                        .sub(101, ResourceType.OP_ATTR)
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .sub(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .add(1, ResourceType.ATTR_MODEL_NUM)
                        .sub(1L, ResourceType.ATTR_MODEL_LEVEL)
                        .add(false, ResourceType.ATTR_MODEL_LOCK)
                        .sub("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(100, treadHandler.opModel.getAttr());
        Assertions.assertEquals(100, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试道具", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(100L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertFalse(treadHandler.opModel.getSubModelById(1).isLock());

        Assertions.assertEquals(300, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(300L, treadHandler.opAttrModel.getLevel());
        Assertions.assertTrue(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("defaultDesc", treadHandler.opAttrModel.getDesc());
    }

    @Test
    public void test2ModelSubFail2() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.setAttr(100);
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "defaultDesc");
        Processes processes = Processes.create(treadHandler.opModel, treadHandler.opAttrModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.FALSE,
                processes
                        .sub(99, ResourceType.OP_ATTR)
                        .add(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub("测试字符串2", ResourceType.OP_SUB_MODEL_DESC, 1)
                        .add(10L, ResourceType.OP_SUB_MODEL_LEVEL, 1)
                        .sub(true, ResourceType.OP_SUB_MODEL_LOCK, 1)
                        .add(1, ResourceType.ATTR_MODEL_NUM)
                        .sub(301L, ResourceType.ATTR_MODEL_LEVEL)
                        .add(false, ResourceType.ATTR_MODEL_LOCK)
                        .sub("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(1, treadHandler.opModel.getAttr());
        Assertions.assertEquals(100, treadHandler.opModel.getSubModelById(1).getNum());
        Assertions.assertEquals("测试字符串2", treadHandler.opModel.getSubModelById(1).getDesc());
        Assertions.assertEquals(100L, treadHandler.opModel.getSubModelById(1).getLevel());
        Assertions.assertTrue(treadHandler.opModel.getSubModelById(1).isLock());

        Assertions.assertEquals(300, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(300L, treadHandler.opAttrModel.getLevel());
        Assertions.assertTrue(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("defaultDesc", treadHandler.opAttrModel.getDesc());
    }

    @Test
    public void test2SameModelError() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "opAttrModel");
        treadHandler.opAttrModel2 = new OpAttrModel(2, 200, 200L, true, "opAttrModel2");
        Assertions.assertThrows(RuntimeException.class, new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        Processes processes = Processes.create(treadHandler.opAttrModel, treadHandler.opAttrModel2).setTraceInfo("123");
                        processes
                                .add(1, ResourceType.ATTR_MODEL_NUM)
                                .sub(301L, ResourceType.ATTR_MODEL_LEVEL)
                                .add(false, ResourceType.ATTR_MODEL_LOCK)
                                .sub("attr", ResourceType.ATTR_MODEL_DESC)
                                .exec()
                                .onErrorContinue(throwable -> {
                                    log.info("error ", throwable);
                                    return Homo.result(false);
                                }).block();
                    }
                }
        );
        Assertions.assertEquals(300, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(300L, treadHandler.opAttrModel.getLevel());
        Assertions.assertTrue(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("opAttrModel", treadHandler.opAttrModel.getDesc());

        Assertions.assertEquals(200, treadHandler.opAttrModel2.getNum());
        Assertions.assertEquals(200L, treadHandler.opAttrModel2.getLevel());
        Assertions.assertTrue(treadHandler.opAttrModel2.isLock());
        Assertions.assertEquals("opAttrModel2", treadHandler.opAttrModel2.getDesc());
    }

    @Test
    public void test2SameModelSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opAttrModel = new OpAttrModel(1, 300, 300L, true, "opAttrModel");
        treadHandler.opAttrModel2 = new OpAttrModel(2, 200, 200L, true, "opAttrModel2");
        Processes processes = Processes.create().setTraceInfo("123")
                .setAddOwner(treadHandler.opAttrModel)
                .setSubOwner(treadHandler.opAttrModel2);
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(1, ResourceType.ATTR_MODEL_NUM)
                        .sub(100L, ResourceType.ATTR_MODEL_LEVEL)
                        .add(false, ResourceType.ATTR_MODEL_LOCK)
                        .sub("attr", ResourceType.ATTR_MODEL_DESC)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(301, treadHandler.opAttrModel.getNum());
        Assertions.assertEquals(300L, treadHandler.opAttrModel.getLevel());
        Assertions.assertFalse(treadHandler.opAttrModel.isLock());
        Assertions.assertEquals("opAttrModel", treadHandler.opAttrModel.getDesc());

        Assertions.assertEquals(200, treadHandler.opAttrModel2.getNum());
        Assertions.assertEquals(100L, treadHandler.opAttrModel2.getLevel());
        Assertions.assertTrue(treadHandler.opAttrModel2.isLock());
        Assertions.assertEquals("attr", treadHandler.opAttrModel2.getDesc());
    }


    @Test
    public void test1ModelCheckSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(401, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(500, treadHandler.opModel.getSubModelById(1).getNum());
    }

    @Test
    public void test1ModelCheckFail() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.FALSE,
                processes
                        .add(402, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(1, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(99, treadHandler.opModel.getSubModelById(1).getNum());
    }

    @Test
    public void test1ModelZeroFail() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.FALSE,
                processes
                        .add(402, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .sub(0, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(100, treadHandler.opModel.getSubModelById(1).getNum());
    }

    @Test
    public void test1ModelZeroSuccess() {
        TreadHandler treadHandler = new TreadHandler("123");
        treadHandler.opModel.addSubModel(1, 100, 100L, false, "测试道具");
        Processes processes = Processes.create(treadHandler.opModel).setTraceInfo("123");
        Assertions.assertEquals(Boolean.TRUE,
                processes
                        .add(302, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .subAllowZero(0, ResourceType.OP_SUB_MODEL_NUM, 1)
                        .exec()
                        .onErrorContinue(throwable -> {
                            log.info("error ", throwable);
                            return Homo.result(false);
                        }).block());
        Assertions.assertEquals(402, treadHandler.opModel.getSubModelById(1).getNum());
    }
}
