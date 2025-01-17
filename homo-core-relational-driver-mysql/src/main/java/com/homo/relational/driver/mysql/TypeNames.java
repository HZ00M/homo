package com.homo.relational.driver.mysql;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TypeNames {

    private final Map<Integer, String> defaults = new HashMap<Integer, String>();

    private final Map<String, Integer> reverses = new HashMap<>();

    private final Map<Integer, Map<Long, String>> weighted = new HashMap<Integer, Map<Long, String>>();

    protected void registerColumnType(int code, String name) {
        put( code, name );
    }

    protected void registerColumnType(int code, String name, long size) {
        put(code, size, name);
    }

    protected void registerColumnTypeJava(String typeName, int code) {
        put(typeName, code);
    }

    public String getTypeName(int code, long length, int precision, int scale) {
        final String result = get(code, length, precision, scale);
        if (result == null) {
            throw new RuntimeException(
                    String.format( "No type mapping for java.sql.Types code: %s, length: %s", code, length )
            );
        }
        return result;
    }

    public int get(final String clazz) {
        return reverses.get(clazz);
    }

    public String get(final int typeCode) {
        final String result = defaults.get( typeCode );
        if ( result == null ) {
            throw new RuntimeException( "No MysqlDialect mapping for JDBC type: " + typeCode );
        }
        return result;
    }

    public String get(int typeCode, long size, int precision, int scale) {
        final Map<Long, String> map = weighted.get( typeCode );
        if ( map != null && map.size() > 0 ) {
            // iterate entries ordered by capacity to find first fit
            for ( Map.Entry<Long, String> entry: map.entrySet() ) {
                if ( size <= entry.getKey() ) {
                    return replace( entry.getValue(), size, precision, scale );
                }
            }
        }

        return replace( get( typeCode ), size, precision, scale );
    }

    private static String replace(String type, long size, int precision, int scale) {
        type = StringUtils.replaceOnce( type, "$s", Integer.toString( scale ) );
        type = StringUtils.replaceOnce( type, "$l", Long.toString( size ) );
        return StringUtils.replaceOnce( type, "$p", Integer.toString( precision ) );
    }

    private void put(int typeCode, long capacity, String value) {
        final Integer integer = typeCode;
        Map<Long, String> map = weighted.computeIfAbsent(integer, k -> new TreeMap<>());
        // add new ordered map
        map.put( capacity, value );
    }

    private void put(int typeCode, String value) {
        final Integer integer = typeCode;
        defaults.put( integer, value );
    }

    private void put(String typeName, int typeCode) {
        reverses.put(typeName, typeCode);
    }

    private boolean containsTypeName(final String typeName) {
        return this.defaults.containsValue( typeName );
    }
}
