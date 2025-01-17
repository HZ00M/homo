package com.homo.relational.base;

import com.homo.core.configurable.relational.RelationalProperties;
import com.homo.core.facade.relational.mapping.HomoTable;
import com.homo.core.facade.relational.mapping.HomoTransient;
import com.homo.core.facade.relational.schema.ColumnSchema;
import com.homo.core.facade.relational.schema.IdentifierSchema;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SchemaInfoCoordinator implements ServiceModule {

    @Autowired
    RelationalProperties relationalProperties;

    @Autowired
    ScanningCoordinator scanningCoordinator;
    private final static Map<String, TableSchema> tableByName = new HashMap<>();
    private final static Map<Class<?>, TableSchema> tablesByClazz = new HashMap<>();

    @Override
    public void moduleInit() {
        try {
            scanningCoordinator.addIncludeFilter(new AnnotationTypeFilter(com.homo.core.facade.relational.mapping.HomoTable.class));//
            Set<Class<?>> candidateClasses = scanningCoordinator.findCandidateClasses(relationalProperties.getBasePackage());// Initialize schema
            for (Class<?> tableClass : candidateClasses) {
                addTable(tableClass);
            }
            log.info("SchemaCoordinator init success");
        }catch (Exception e){
            log.error("SchemaCoordinator init error: ", e);
            throw  new RuntimeException(e);
        }
    }

    private static void addTable(Class<?> tableClass) throws InstantiationException, IllegalAccessException {
        HomoTable homoTable = tableClass.getAnnotation(HomoTable.class);
        String tableName = homoTable.value();
        log.info("addTable {}", tableName);
        if (!StringUtils.hasText(tableName)){
            tableName = tableClass.getSimpleName();
        }
        List<ColumnSchema> columnList = getColumnSchemaList(tableClass);
        IdentifierSchema identifierSchema = IdentifierSchema.toIdentifier(tableName);
        String driverName = homoTable.driverName();
        TableSchema tableSchema = new TableSchema(tableName,driverName,identifierSchema,tableClass,homoTable.indices(),
                homoTable.generate(),homoTable.nameStrategy(),columnList);
        tableByName.put(tableName,tableSchema);
        tablesByClazz.put(tableClass,tableSchema);
    }

    public static TableSchema getTable(Class<?> clazz){
        if (!tablesByClazz.containsKey(clazz)){
            throw new RuntimeException(String.format("unknown table: %s", clazz.getName()));
        }
        return tablesByClazz.get(clazz);
    }

    public static TableSchema getTable(String tableName){
        if (!tableByName.containsKey(tableName)){
            throw new RuntimeException(String.format("unknown tableName: %s", tableName));
        }
        return tableByName.get(tableName);
    }

    public static List<TableSchema> getTableList() {
        return new ArrayList<>(tableByName.values());
    }

    private static List<ColumnSchema> getColumnSchemaList(Class<?> tableClass) {
        List<ColumnSchema> list = new ArrayList<>();
        List<Field> columnFields = new ArrayList<>();
        List<Field> candidateFields = new ArrayList<>(Arrays.asList(tableClass.getFields()));
        for (Class<?> superClass = tableClass.getSuperclass();superClass != null && !superClass.equals(Object.class);superClass = superClass.getSuperclass()){
            candidateFields.addAll(Arrays.asList(superClass.getDeclaredFields()));
        }
        for (Field field : candidateFields) {
            if (isProperty(field)){
                columnFields.add(field);
            }
        }
        List<Method> methods = getClassMethods(tableClass);
        for (Field field : columnFields) {
            Method getmethod = getFieldGetMethod(field,methods);
            Method setmethod = setFieldSetMethod(field,methods);
            ColumnSchema columnSchema = new ColumnSchema(field,setmethod,getmethod);
            list.add(columnSchema);
        }
        return list;
    }

    private static Method setFieldSetMethod(Field field, List<Method> methods) {
        String methodName = String.format("set%s%s", field.getName().substring(0, 1).toUpperCase(), field.getName().substring(1));
        return methods.stream().filter(method -> method.getName().equals(methodName)).findFirst().orElse(null);
    }

    private static Method getFieldGetMethod(Field field,List<Method> methods) {
        String methodName = String.format("get%s%s", field.getName().substring(0, 1).toUpperCase(), field.getName().substring(1));
        return methods.stream().filter(method -> method.getName().equals(methodName)).findFirst().orElse(null);
    }


    private static List<Method> getClassMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
        Class<?> superClazz = clazz.getSuperclass();
        while (!superClazz.equals(Object.class)) {
            methods.addAll(Arrays.asList(superClazz.getDeclaredMethods()));
            superClazz = superClazz.getSuperclass();
        }
        return methods;
    }
    private static boolean isProperty(Field field) {
        //1、非静态字段
        //2、没有HomoTransient注解
        //3、非合成字段
        // field.isSynthetic() 返回 true 的情况通常与 Java 编译器生成的特殊字段有关，主要出现在以下几种情境中：
        //匿名内部类：编译器为匿名内部类生成字段来引用外部类的实例。
        //Lambda 表达式：编译器为实现 Lambda 表达式的接口生成字段。
        //泛型类型擦除：编译器为处理泛型类型擦除生成合成字段。
        //内嵌类的字段：编译器为内嵌类生成字段，以引用外部类的实例。
        //编译器优化：编译器为了优化代码而生成的字段。
        //4、没有transient关键字
        return !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(HomoTransient.class) &&
                !field.isSynthetic() && !Modifier.isTransient(field.getModifiers());
    }

}
