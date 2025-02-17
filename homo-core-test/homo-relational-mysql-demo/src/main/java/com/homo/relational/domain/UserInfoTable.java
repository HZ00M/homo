package com.homo.relational.domain;

import com.homo.core.facade.relational.mapping.HomoColumn;
import com.homo.core.facade.relational.mapping.HomoId;
import com.homo.core.facade.relational.mapping.HomoTable;
import lombok.Data;

@Data
@HomoTable(value = "user_record")
public class UserInfoTable {
    @HomoId(autoGenerate = true)
    public String id;
    @HomoColumn("user_name")
    public String userName;
}
