package test;

import com.homo.core.facade.tread.processor.anotation.ResourceCheckMethod;
import com.homo.core.facade.tread.processor.anotation.ResourceGetMethod;
import com.homo.core.facade.tread.processor.anotation.ResourceSetMethod;
import com.homo.core.utils.rector.Homo;

import java.util.HashMap;
import java.util.Map;

public class OpModel {
    Integer attr;
    Map<Integer, OpSubModel> modelMap = new HashMap<>();

    @ResourceSetMethod(ResourceType.OP_ATTR)
    public void setAttr(Integer attr){
        this.attr = attr;
    }
    @ResourceGetMethod(ResourceType.OP_ATTR)
    public Integer getAttr(){
        return this.attr;
    }

    @ResourceSetMethod(ResourceType.OP_SUB_MODEL_NUM)
    public void setNum(Integer num, Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel == null) {
            opSubModel = new OpSubModel(id, num, 0L, false, "desc");
            modelMap.put(id, opSubModel);
        }
        if (num <= 0) {
            modelMap.remove(id);
        }
        opSubModel.num = num;
    }

    @ResourceGetMethod(ResourceType.OP_SUB_MODEL_NUM)
    public Integer getNum(Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel==null || opSubModel.num==null){
            return null;
        }
        return opSubModel.num;
    }

    @ResourceCheckMethod(value = ResourceType.OP_SUB_MODEL_NUM,checkInfo = "check beforeSetValue must <= 500")
    public Homo<Boolean> checkNum(Integer opValue, Integer beforeSetValue, Integer id){
        return Homo.result(beforeSetValue <= 500);
    }

    @ResourceSetMethod(ResourceType.OP_SUB_MODEL_LEVEL)
    public void setLevel(Long level, Integer id) {

        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel == null) {
            opSubModel = new OpSubModel(id, 1, level, false, "desc");
            modelMap.put(id, opSubModel);
        }
        if (level <= 0) {
            modelMap.remove(id);
        }
        opSubModel.level = level;
    }

    @ResourceGetMethod(ResourceType.OP_SUB_MODEL_LEVEL)
    public Long getLevel(Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel==null || opSubModel.level==null){
            return null;
        }
        return opSubModel.level;
    }

    @ResourceSetMethod(ResourceType.OP_SUB_MODEL_LOCK)
    public void setLock(Boolean lock, Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel == null) {
            opSubModel = new OpSubModel(id, 0, 0L, lock, "desc");
            modelMap.put(id, opSubModel);
        }
        opSubModel.lock = lock;
    }

    @ResourceGetMethod(ResourceType.OP_SUB_MODEL_LOCK)
    public Boolean getLock(Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel==null ){
            return null;
        }
        return opSubModel.lock;
    }

    @ResourceSetMethod(ResourceType.OP_SUB_MODEL_DESC)
    public void setDesc(String desc, Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel == null) {
            opSubModel = new OpSubModel(id, 0, 0L, true, desc);
            modelMap.put(id, opSubModel);
        }
        opSubModel.desc = desc;
    }

    @ResourceGetMethod(ResourceType.OP_SUB_MODEL_DESC)
    public String getDesc(Integer id) {
        OpSubModel opSubModel = modelMap.get(id);
        return opSubModel.desc;
    }

    public void addSubModel(Integer id, Integer num, Long level, boolean lock, String desc) {
        OpSubModel opSubModel = modelMap.get(id);
        if (opSubModel == null) {
            OpSubModel opSubModel1 = new OpSubModel(id, num, level, lock, desc);
            modelMap.put(id, opSubModel1);
        }
    }

    public OpSubModel getSubModelById(Integer id){
        return modelMap.get(id);
    }
}
