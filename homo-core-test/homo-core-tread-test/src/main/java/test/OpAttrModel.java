package test;

import com.homo.core.facade.tread.processor.anotation.ResourceGetMethod;
import com.homo.core.facade.tread.processor.anotation.ResourceSetMethod;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpAttrModel {
    Integer id;
    Integer num;
    Long level;
    boolean lock;
    String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ResourceGetMethod(ResourceType.ATTR_MODEL_NUM)
    public Integer getNum() {
        return num;
    }

    @ResourceSetMethod(ResourceType.ATTR_MODEL_NUM)
    public void setNum(Integer num) {
        this.num = num;
    }

    @ResourceGetMethod(ResourceType.ATTR_MODEL_LEVEL)
    public Long getLevel() {
        return level;
    }

    @ResourceSetMethod(ResourceType.ATTR_MODEL_LEVEL)
    public void setLevel(Long level) {
        this.level = level;
    }

    @ResourceGetMethod(ResourceType.ATTR_MODEL_LOCK)
    public boolean isLock() {
        return lock;
    }

    @ResourceSetMethod(ResourceType.ATTR_MODEL_LOCK)
    public void setLock(boolean lock) {
        this.lock = lock;
    }

    @ResourceGetMethod(ResourceType.ATTR_MODEL_DESC)
    public String getDesc() {
        return desc;
    }

    @ResourceSetMethod(ResourceType.ATTR_MODEL_DESC)
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
