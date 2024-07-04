package com.homo.core.utils.origin;
import java.io.Serializable;

/**
 * 建造者模式接口定义
 * 
 * @param <T> 建造对象类型
 *
 *
 */
public interface Builder<T> extends Serializable{
	/**
	 * 构建
	 * 
	 * @return 被构建的对象
	 */
	T build();
}