package test;

import com.homo.core.facade.tread.tread.annotation.GetMethod;
import com.homo.core.facade.tread.tread.annotation.SetMethod;

public class ItemInfoModel {
    public int id;
    public Integer num;
    public int level;
    public Integer quality;

    public ItemInfoModel(int id, int num, int level, int quality) {
        this.id = id;
        this.num = num;
        this.level = level;
        this.quality = quality;
    }


    @GetMethod({ResourceType.BAGINFO_NUM,ResourceType.INNER_BAGINFO_NUM})
    public Integer getNum() {
        return num;
    }

    @SetMethod({ResourceType.BAGINFO_NUM,ResourceType.INNER_BAGINFO_NUM})
    public void setNum(Integer num) {
        this.num = num;
    }

    @GetMethod({ResourceType.BAGINFO_LEVEL,ResourceType.INNER_BAGINFO_LEVEL})
    public int getLevel() {
        return level;
    }

    @SetMethod({ResourceType.BAGINFO_LEVEL,ResourceType.INNER_BAGINFO_LEVEL})
    public void setLevel(Integer level) {
        this.level = level;
    }

    @GetMethod({ResourceType.BAGINFO_QUALITY,ResourceType.INNER_BAGINFO_QUALITY})
    public int getQuality() {
        return quality;
    }

    @SetMethod({ResourceType.BAGINFO_QUALITY,ResourceType.INNER_BAGINFO_QUALITY})
    public void setQuality(int quality) {
        this.quality = quality;
    }
}
