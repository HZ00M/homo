package com.homo.core.entity.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PlayRecord {
    public String playId;
    public String playType;
    public String chapterId;
    public Integer score;
    public Integer isWin;
}
