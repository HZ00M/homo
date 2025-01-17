package com.homo.core.facade.relational.schema;

import com.homo.core.utils.rector.Homo;

import java.util.Set;

public interface SchemaAccessDialect {
    Homo<Boolean> createTable(TableSchema table);

    Homo<Set<String>> fetchExistTables();

   default String driverName(){
       return "";
   }

}
