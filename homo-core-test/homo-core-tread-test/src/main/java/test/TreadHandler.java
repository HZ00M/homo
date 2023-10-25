package test;

import com.homo.core.facade.tread.tread.annotation.*;
import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.tread.tread.intTread.IntTread;
import com.homo.core.tread.tread.objTread.ObjTread;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TreadHandler  implements  ITreadHandler {
    public Integer a;
    public Integer b;
    public Integer c;
    public Integer d;
    public Long x;
    public Boolean y;
    public String z;
    public Map<Integer, ItemInfoModel> bagMap = new HashMap<>();
    public OpModel opModel = new OpModel();
    public OpAttrModel opAttrModel = new OpAttrModel(1,0,0L,true,"defaultDesc");
    public OpAttrModel opAttrModel2 = new OpAttrModel(2,0,0L,true,"defaultDesc");
    @CreateObjMethod(value = {ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY},type = ItemInfoModel.class)
    public ItemInfoModel newItem(Integer id) {
        return new ItemInfoModel(id, 0, 0, 0);
    }

    @GetObjMethod(value = {ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY},type = ItemInfoModel.class)
    public ItemInfoModel getItemInfoById(Integer id) {
        return bagMap.get(id);
    }

    @SetObjMethod(value = {ResourceType.BAGINFO_NUM, ResourceType.BAGINFO_LEVEL, ResourceType.BAGINFO_QUALITY},type = ItemInfoModel.class)
    public void setItemInfoModel(Integer id, ItemInfoModel itemInfoModel) {
        if (itemInfoModel == null || itemInfoModel.num == 0) {
            bagMap.remove(id);
            return;
        }
        bagMap.put(id, itemInfoModel);
    }



    public Integer e;
    public Integer f;
    public Integer g;
    public boolean h;
    public boolean i;
    public Equip equip = new Equip();
    public Pack pack = new Pack();

    public Map<Integer, EquipInfoModel> equipMap = new HashMap<>();
    public Map<Long, TalentInfoModel> talentMap = new HashMap<>();
    public Map<Long, TalentInfoModel> talentMap1 = new HashMap<>();
    public Map<Long, TalentInfoModel> talentMap2 = new HashMap<>();
    public Map<Long, TalentInfoModel> talentMap3 = new HashMap<>();

    String ownerId;
    public InnerHandler innerHandler = new InnerHandler();

    public class InnerHandler {
        @CreateObjMethod(value = {ResourceType.INNER_BAGINFO_NUM, ResourceType.INNER_BAGINFO_LEVEL, ResourceType.INNER_BAGINFO_QUALITY},type = ItemInfoModel.class)
        public ItemInfoModel newItem(Integer id) {
            return new ItemInfoModel(id, 0, 0, 0);
        }

        @GetObjMethod(value = {ResourceType.INNER_BAGINFO_NUM, ResourceType.INNER_BAGINFO_LEVEL, ResourceType.INNER_BAGINFO_QUALITY},type = ItemInfoModel.class)
        public ItemInfoModel getItemInfoById(Integer id) {
            return innerMap.get(id);
        }

        @SetObjMethod(value = {ResourceType.INNER_BAGINFO_NUM, ResourceType.INNER_BAGINFO_LEVEL, ResourceType.INNER_BAGINFO_QUALITY},type = ItemInfoModel.class)
        public void setItemInfoModel(Integer id, ItemInfoModel itemInfoModel) {
            if (itemInfoModel == null || itemInfoModel.num == 0) {
                innerMap.remove(id);
                return;
            }
            innerMap.put(id, itemInfoModel);
        }
        public Map<Integer, ItemInfoModel> innerMap = new HashMap<>();
    }



    /////////////////////////////////
    @CreateObjMethod(value = {ResourceType.TELENT_NUM, ResourceType.TELENT_QUALITY, ResourceType.TELENT_LEVEL})
    public TalentInfoModel createTalent(Long id) {
        return new TalentInfoModel(id, 0, 0L, "0");
    }

    @GetObjMethod(value = {ResourceType.TELENT_NUM, ResourceType.TELENT_QUALITY, ResourceType.TELENT_LEVEL})
    public TalentInfoModel getTalent(Long id) {
        return talentMap.get(id);
    }

    @SetObjMethod(value = {ResourceType.TELENT_NUM, ResourceType.TELENT_QUALITY, ResourceType.TELENT_LEVEL})
    public void setTalent(Long id, TalentInfoModel talentInfoModel) {
        if (talentInfoModel == null || talentInfoModel.num == 0) {
            talentMap.remove(id);
            return;
        }
        talentMap.put(id, talentInfoModel);
    }
    /////////////////////////////////
    @CreateObjMethod(value = {ResourceType.TELENT_NUM_1, ResourceType.TELENT_QUALITY_1})
    public TalentInfoModel createTalent1(Long id) {
        return new TalentInfoModel(id, 0, 0L, "0");
    }

    @CreateObjMethod(value = {ResourceType.TELENT_LEVEL_1})
    public TalentInfoModel createTalent1_2(Long id) {
        return new TalentInfoModel(id, 10, 10, "10");
    }

    @GetObjMethod(value = {ResourceType.TELENT_NUM_1, ResourceType.TELENT_QUALITY_1, ResourceType.TELENT_LEVEL_1})
    public TalentInfoModel getTalent1(Long id) {
        return talentMap1.get(id);
    }

    @SetObjMethod(value = {ResourceType.TELENT_NUM_1, ResourceType.TELENT_QUALITY_1, ResourceType.TELENT_LEVEL_1})
    public void setTalent1(Long id, TalentInfoModel talentInfoModel) {
        if (talentInfoModel == null || talentInfoModel.num == 0) {
            talentMap2.remove(id);
            return;
        }
        talentMap2.put(id, talentInfoModel);
    }

    /////////////////////////////////
    @CreateObjMethod(value = {ResourceType.TELENT_NUM_2,ResourceType.TELENT_QUALITY_2})
    public TalentInfoModel createTalent2(Long id) {
        return new TalentInfoModel(id, 0, 0L, "0");
    }

    @CreateObjMethod(value = { ResourceType.TELENT_LEVEL_2})
    public TalentInfoModel createTalent2Error(Long id) throws Exception {
        throw  new RuntimeException("createTalent2Error");
    }

    @GetObjMethod(value = {ResourceType.TELENT_NUM_2,ResourceType.TELENT_LEVEL_2})
    public TalentInfoModel getTalent2(Long id) {
        return talentMap2.get(id);
    }

    @GetObjMethod(value = {ResourceType.TELENT_QUALITY_2})
    public TalentInfoModel getTalent2Error(Long id) throws Exception{
        int i = 2/0;
        return null;
    }

    @SetObjMethod(value = {ResourceType.TELENT_LEVEL_2,ResourceType.TELENT_QUALITY_2})
    public void setTalent2(Long id, TalentInfoModel talentInfoModel) {
        if (talentInfoModel == null || talentInfoModel.num == 0) {
            talentMap2.remove(id);
            return;
        }
        talentMap2.put(id, talentInfoModel);
    }

    @SetObjMethod(value = {ResourceType.TELENT_NUM_2})
    public void setTalent2Error(Long id, TalentInfoModel talentInfoModel) throws Exception{
        throw new RuntimeException("setTalent2Error");
    }

    /////////////////////////////////
    @CreateObjMethod(value = {ResourceType.TELENT_NUM_3, ResourceType.TELENT_QUALITY_3,ResourceType.TELENT_LEVEL_3})
    public TalentInfoModel createTalent3(Long id) {
        return new TalentInfoModel(id, 0, 0L, "0");
    }

    @GetObjMethod(value = {ResourceType.TELENT_NUM_3, ResourceType.TELENT_QUALITY_3, ResourceType.TELENT_LEVEL_3})
    public TalentInfoModel getTalent3(Long id) {
        return talentMap3.get(id);
    }
//    //参数不匹配测试
//    @SetObjMethod(value = {ResourceType.TELENT_NUM_3},type = TalentInfoModel.class)
//    public void setTalent3_1(Long id, EquipInfoModel equipInfoModel) {
//        equipMap.put(1, equipInfoModel);
//    }

//    @SetObjMethod(value = {ResourceType.TELENT_QUALITY_3})
//    public void setTalent3_2(Long id, TalentInfoModel talentInfoModel,String extra) {
//        if (talentInfoModel == null || talentInfoModel.num == 0) {
//            talentMap3.remove(id);
//            return;
//        }
//        talentMap3.put(id, talentInfoModel);
//    }

//    @SetObjMethod(value = {ResourceType.TELENT_LEVEL_3})
//    public void setTalent3_3() {
//    }

    /////////////////////////////////
    public void example() {
        ObjTread.create(ownerId)
//                .setSupplier("1", ()->new ItemInfoModel("1", 1,1,1))
//                .setSupplier("2", ()->new ItemInfoModel("2", 1,1,1))
//                .setSupplier("3", ()->new ItemInfoModel("3", 1,1,1))
                .sub("1", ResourceType.BAGINFO_NUM, 1)
                .sub("1", ResourceType.BAGINFO_LEVEL, 1)
                .add("2", ResourceType.BAGINFO_NUM, 1)
                .add("2", ResourceType.BAGINFO_QUALITY, 1, () -> new ItemInfoModel(1, 1, 1, 1))
                .exec().start();
    }

    public TreadHandler(String userId) {
        log.info("ActivityHandler Init for user: {}", userId);
        this.ownerId = userId;
    }

    public void reset(Integer a, Integer b, Integer c, Integer d) {
        reset(a,b,c,d,null,null,null,null,null,null);
    }

    public void reset(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g) {
        reset(a,b,c,d,e,f,g,null,null,null);
    }

    public void reset(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Long x, Boolean y, String z) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.x = x;
        this.y = y;
        this.z = z;
        bagMap = new HashMap<>();
        equipMap = new HashMap<>();
        talentMap = new HashMap<>();
        innerHandler = new InnerHandler();
    }

    public void addGun(Gun gun) {
        equip.addGun(gun);
    }

    public void addMaterial(Material material) {
        pack.addMater(material);
    }

    public Homo<Integer> getGunLevel(Integer gunId) {
        return Homo.result(equip.getGun(gunId).getLevel());
    }

    public Homo<Integer> getGunDamage(Integer gunId) {
        return Homo.result(equip.getGun(gunId).getDamage());
    }

    public Homo<Integer> getMaterCount(Integer materId) {
        return Homo.result(pack.getMater(materId).getCount());
    }

    public Homo<ExecRet> upLevelGun(Integer gunId, Integer level, Integer materialId, Integer cost, Integer damage) {
        Gun gun = equip.getGun(gunId);
        Material material = pack.getMater(materialId);
        return IntTread.create(ownerId)
                .sub(material,"material.count", cost)
                .add(gun,"gun.level", level)
                .add(gun,"gun.damage", damage)
                .exec()
                .nextValue(retStringTuple2 -> retStringTuple2.getT1());
    }

    @GetMethod("A")
    public Integer getA() {
        return a;
    }

    @SetMethod("A")
    public Homo<String> setA(Integer a) {
        this.a = a;
        return Homo.result(this.a + "");
    }

    @GetMethod("B")
    public Homo<Integer> getB() {
        return Homo.result(b);
    }

    @SetMethod("B")
    public Integer setB(Integer b) {
        this.b = b;
        return this.b;
    }

    @GetMethod("C")
    public Homo<Integer> getC() {
        return Homo.result(this.c);
    }

    @SetMethod("C")
    public Homo<Integer> setC(Integer c) {
        this.c = c;
        return Homo.result(this.c);
    }

    @GetMethod("D")
    public Integer getD() {
        return d;
    }

    @SetMethod("D")
    public String setD(Integer d) {
        this.d = d;
        return this.d + "";
    }


    public Integer getE() {
        return e;
    }

    public Homo<String> setE(Integer e) {
        this.e = e;
        return Homo.result(this.e + "");
    }

    public Integer getF() {
        return f;
    }

    public void setF(Integer f) {
        this.f = f;
    }

    public Integer getG() {
        return g;
    }

    public void setG(Integer g) {
        this.g = g;
    }

    @GetMethod("H")
    public Boolean getH() {
        return h;
    }

    @SetMethod("H")
    public Boolean setH(boolean h) {
        this.h = h;
        return this.h;
    }

    @GetMethod("i")
    public Boolean getI() {
        return i;
    }

    @SetMethod("i")
    public Homo<Boolean> setI(boolean i) {
        this.i = i;
        return Homo.result(this.i);
    }

    @GetMethod("x")
    public Long getX() {
        return x;
    }

    @SetMethod("x")
    public void setX(Long x) {
        this.x = x;
    }

    @GetMethod("y")
    public Boolean getY() {
        return y;
    }

    @SetMethod("y")
    public void setY(Boolean y) {
        this.y = y;
    }

    @GetMethod("z")
    public String getZ() {
        return z;
    }

    @SetMethod("z")
    public void setZ(String z) {
        this.z = z;
    }
}
