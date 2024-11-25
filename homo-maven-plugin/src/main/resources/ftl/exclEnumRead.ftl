<#ftl encoding="utf-8">
package ${packagename};

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class Table_${className}{
	<#list fields as f1>
		<#if f1.TypeName??>
			public enum ${f1.TypeName?trim}{
			<#list fields as f2>
				<#if f2?counter gte f1?counter>
					<#if f2.TypeName?? && f2.TypeName != f1.TypeName><#break></#if>

                    //${f2.Annotation?trim}
					${f2.Name?trim}(${f2.Value?keep_before(".")})<#sep>,
				</#if>
			</#list>;
				private Integer value;
				${f1.TypeName}(Integer value){
					this.value = value;
				}
				public Integer getValue(){
					return this.value;
				}
				@Override
				public String toString() {
					return value.toString();
				}
			};
		</#if>
	</#list>

	private static Map<Class,Object> map = new ConcurrentHashMap<>();

	/**
     * 根据条件获取枚举对象
     * @param className 枚举类
     * @param predicate 筛选条件
     * @param <T>
     * @return
     */
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> getEnumObject(Class<T> className, Predicate<T> predicate) {
		if(!className.isEnum()){
//            logger.info("Class 不是枚举类");
			return null;
		}
		Object obj = map.get(className);
		T[] ts = null;
		if(obj == null){
			ts = className.getEnumConstants();
			map.put(className,ts);
		}else{
			ts = (T[])obj;
		}
		return Arrays.stream(ts).filter(predicate).findAny();
	}
}