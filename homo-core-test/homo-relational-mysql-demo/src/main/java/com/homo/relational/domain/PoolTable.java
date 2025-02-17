package com.homo.relational.domain;

import com.homo.core.facade.relational.mapping.HomoColumn;
import com.homo.core.facade.relational.mapping.HomoId;
import com.homo.core.facade.relational.mapping.HomoTable;
import lombok.Data;

@Data
@HomoTable(value = "pool_record")
public class PoolTable {
    @HomoId(autoGenerate = true)
    public Long id;
    public String name;
    public Long count;
}
