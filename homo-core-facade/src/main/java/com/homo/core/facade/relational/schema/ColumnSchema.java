package com.homo.core.facade.relational.schema;

import com.homo.core.facade.relational.mapping.HomoColumn;
import com.homo.core.facade.relational.mapping.HomoId;
import com.homo.core.facade.relational.mapping.HomoJsonColumn;
import javafx.print.Collation;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Data
public class ColumnSchema {
    public static final int DEFAULT_LENGTH = 255;
    public static final int DEFAULT_PRECISION = 19;
    public static final int DEFAULT_SCALE = 2;

    private int length = DEFAULT_LENGTH;
    private int precision = DEFAULT_PRECISION;
    private int scale = DEFAULT_SCALE;
    private String name;
    private String rawName;
    private boolean nullable = true;
    private String comment;
    private String defaultValue;

    private String sqlType;
    private Integer sqlTypeCode;

    private String typeName;
    private Class<?> typeClazz;
    private Method readMethod;
    private Method writeMethod;
    private Field field;
    private boolean isCollection;
    private Method proxyKeyWriter;
    private boolean isJson;
    private boolean isPrimaryKey;
    private boolean autoGenerate;

    public ColumnSchema(Field field,Method setMethod,Method getMethod) {
        name = field.getName();
        rawName = field.getName();
        this.field = field;
        typeClazz = field.getType();
        typeName = field.getType().getTypeName();
        HomoColumn homoColumn = field.getAnnotation(HomoColumn.class);
        if (homoColumn != null) {
            if (StringUtils.hasText(homoColumn.value())){
                name = homoColumn.value();
            }
            name = homoColumn.value();
            length = homoColumn.length();
            nullable = homoColumn.nullable();
            scale = homoColumn.scale();
            precision = homoColumn.precision();
        }
        isJson = HomoJsonColumn.class.isAssignableFrom(field.getType());
        isCollection = Collation.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType());
        this.writeMethod = setMethod;
        this.readMethod = getMethod;
        HomoId homoId = field.getAnnotation(HomoId.class);
        if (homoId != null) {
            isPrimaryKey = true;
            autoGenerate = homoId.autoGenerate();
        }
    }

    public String getQuotedName() {
        return IdentifierSchema.toIdentifier(name).getText();
    }
}
