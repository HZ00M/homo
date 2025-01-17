package com.homo.core.facade.relational.query;

import com.homo.core.utils.rector.Homo;

import java.util.HashMap;
import java.util.Map;

public class HomoUpdate {
    private final Map<String, Object> columnsToUpdate;

    private HomoUpdate(Map<String,Object> columnsToUpdate){
        this.columnsToUpdate = columnsToUpdate;
    }

    public Map<String, Object> getAssignments() {
        return this.columnsToUpdate;
    }

    public static class UpdateBuilder{
        private final Map<String, Object> columnsToUpdate = new HashMap<>();

        public UpdateBuilder set(String column, Object value){
            columnsToUpdate.put(column, value);
            return this;
        }

        public HomoUpdate build(){
            return new HomoUpdate(columnsToUpdate);
        }
    }

    public static UpdateBuilder builder(){
        return new UpdateBuilder();
    }
}
