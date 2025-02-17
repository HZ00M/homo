package com.homo.relational.domain;

import com.homo.core.facade.relational.mapping.HomoColumn;
import com.homo.core.facade.relational.mapping.HomoId;
import com.homo.core.facade.relational.mapping.HomoIndex;
import com.homo.core.facade.relational.mapping.HomoTable;
import io.homo.proto.relational.test.DrawCardPb;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
@HomoTable(value = "draw_card_record",
        indices = {@HomoIndex(columns = {"user_id","pool_id"},indexType = HomoIndex.IndexType.NORMAL)})
public class DrawCardTable {
    @HomoId(autoGenerate = true)
    public long id;
    /**
     * 玩家UID
     */
    @HomoColumn(value = "user_id")
    public String userId;

    /**
     * 卡池ID
     */
    @HomoColumn(value = "pool_id")
    public int poolId;

    public DrawCardPb covertToPb(){
        DrawCardPb.Builder builder = DrawCardPb.newBuilder();
        builder.setId(id);
        builder.setUserId(userId);
        builder.setPoolId(poolId);
        return builder.build();
    }

    public static DrawCardTable coverPbTo(DrawCardPb drawCardPb){
        DrawCardTable drawCardTable = new DrawCardTable();
        drawCardTable.id = drawCardPb.getId();
        drawCardTable.userId = drawCardPb.getUserId();
        drawCardTable.poolId = drawCardPb.getPoolId();
        return drawCardTable;  //返回this，方便链式调用
    }
}
