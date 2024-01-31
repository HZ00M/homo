package test;

import com.homo.core.facade.tread.tread.annotation.GetMethod;
import com.homo.core.facade.tread.tread.annotation.SetMethod;

public class TalentInfoModel {
    public long id;
    public Integer num;
    public long level;
    public String quality;

    public TalentInfoModel(long id, Integer num, long level, String quality) {
        this.id = id;
        this.num = num;
        this.level = level;
        this.quality = quality;
    }

    @GetMethod({ResourceType.TELENT_NUM,ResourceType.TELENT_NUM_1,ResourceType.TELENT_NUM_2,ResourceType.TELENT_NUM_3})
    public Integer getNum() {
        return num;
    }

    @SetMethod({ResourceType.TELENT_NUM,ResourceType.TELENT_NUM_1,ResourceType.TELENT_NUM_2,ResourceType.TELENT_NUM_3})
    public void setNum(Integer num) {
        this.num = num;
    }

    @GetMethod({ResourceType.TELENT_LEVEL,ResourceType.TELENT_LEVEL_1,ResourceType.TELENT_LEVEL_2,ResourceType.TELENT_LEVEL_3})
    public long getLevel() {
        return level;
    }

    @SetMethod({ResourceType.TELENT_LEVEL,ResourceType.TELENT_LEVEL_1,ResourceType.TELENT_LEVEL_2,ResourceType.TELENT_LEVEL_3})
    public void setLevel(long level) {
        this.level = level;
    }

    @GetMethod({ResourceType.TELENT_QUALITY,ResourceType.TELENT_QUALITY_1,ResourceType.TELENT_QUALITY_2,ResourceType.TELENT_QUALITY_3})
    public String getQuality() {
        return quality;
    }

    @SetMethod({ResourceType.TELENT_QUALITY,ResourceType.TELENT_QUALITY_1,ResourceType.TELENT_QUALITY_2,ResourceType.TELENT_QUALITY_3})
    public void setQuality(String quality) {
        this.quality = quality;
    }
}
