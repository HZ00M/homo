package com.homo.core.utils.origin.exceptions;


import com.homo.core.utils.origin.ExceptionUtil;

/**
 * 日期异常
 *
 */
public class DateException extends RuntimeException{
	private static final long serialVersionUID = 8247610319171014183L;

	public DateException(Throwable e) {
		super(ExceptionUtil.getMessage(e), e);
	}
	
	public DateException(String message) {
		super(message);
	}

	public DateException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
