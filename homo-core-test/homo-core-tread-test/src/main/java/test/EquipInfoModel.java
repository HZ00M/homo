package test;

import com.homo.core.facade.tread.tread.annotation.GetMethod;
import com.homo.core.facade.tread.tread.annotation.SetMethod;

public class EquipInfoModel {
    public int id;
    public Integer num;
    public int level;
    public Integer quality;

    public EquipInfoModel(int id, int num, int level, int quality) {
        this.id = id;
        this.num = num;
        this.level = level;
        this.quality = quality;
    }


    @GetMethod(ResourceType.EQUIP_NUM)
    public Integer getNum() {
        return num;
    }

    @SetMethod(ResourceType.EQUIP_NUM)
    public void setNum(Integer num) {
        this.num = num;
    }

    @GetMethod(ResourceType.EQUIP_LEVEL)
    public int getLevel() {
        return level;
    }

    @SetMethod(ResourceType.EQUIP_LEVEL)
    public void setLevel(Integer level) {
        this.level = level;
    }

    @GetMethod(ResourceType.EQUIP_QUALITY)
    public int getQuality() {
        return quality;
    }

    @SetMethod(ResourceType.EQUIP_QUALITY)
    public void setQuality(int quality) {
        this.quality = quality;
    }
}
