package com.homo.core.utils.origin;


import com.homo.core.utils.origin.exceptions.UtilException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 统一资源定位符相关工具类
 *
 *
 *
 */
public class URLUtil {
	private URLUtil(){

	}
	/** 针对ClassPath路径的伪协议前缀（兼容Spring）: "classpath:" */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";


	/**
	 * 通过一个字符串形式的URL地址创建URL对象
	 *
	 * @param url URL
	 * @return URL对象
	 */
	public static URL url(String url) {
		return url(url, null);
	}

	/**
	 * 通过一个字符串形式的URL地址创建URL对象
	 *
	 * @param url URL
	 * @param handler {@link URLStreamHandler}
	 * @return URL对象
	 *
	 */
	public static URL url(String url, URLStreamHandler handler) {
		Assert.notNull(url, "URL must not be null");

		// 兼容Spring的ClassPath路径
		if (url.startsWith(CLASSPATH_URL_PREFIX)) {
			url = url.substring(CLASSPATH_URL_PREFIX.length());
			return ClassLoaderUtil.getClassLoader().getResource(url);
		}

		try {
			return new URL(null, url, handler);
		} catch (MalformedURLException e) {
			// 尝试文件路径
			try {
				return new File(url).toURI().toURL();
			} catch (MalformedURLException ex2) {
				throw new UtilException(e);
			}
		}
	}

	/**
	 * 将URL字符串转换为URL对象，并做必要验证
	 *
	 * @param urlStr URL字符串
	 * @return URL
	 *
	 */
	public static URL toUrlForHttp(String urlStr) {
		return toUrlForHttp(urlStr, null);
	}

	/**
	 * 将URL字符串转换为URL对象，并做必要验证
	 *
	 * @param urlStr URL字符串
	 * @param handler {@link URLStreamHandler}
	 * @return URL
	 *
	 */
	public static URL toUrlForHttp(String urlStr, URLStreamHandler handler) {
		Assert.notBlank(urlStr, "Url is blank !");
		// 编码空白符，防止空格引起的请求异常
		urlStr = encodeBlank(urlStr);
		try {
			return new URL(null, urlStr, handler);
		} catch (MalformedURLException e) {
			throw new UtilException(e);
		}
	}
	
	/**
	 * 单独编码URL中的空白符，空白符编码为%20
	 * 
	 * @param urlStr URL字符串
	 * @return 编码后的字符串
	 *
	 */
	public static String encodeBlank(CharSequence urlStr) {
		if (urlStr == null) {
			return null;
		}

		int len = urlStr.length();
		final StringBuilder sb = new StringBuilder(len);
		char c;
		for (int i = 0; i < len; i++) {
			c = urlStr.charAt(i);
			if (CharUtil.isBlankChar(c)) {
				sb.append("%20");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}



	/**
	 * 获得URL，常用于使用绝对路径时的情况
	 *
	 * @param file URL对应的文件对象
	 * @return URL
	 * @exception UtilException MalformedURLException
	 */
	public static URL getURL(File file) {
		Assert.notNull(file, "File is null !");
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new UtilException("Error occured when get URL!",e);
		}
	}

	/**
	 * 获得URL，常用于使用绝对路径时的情况
	 *
	 * @param files URL对应的文件对象
	 * @return URL
	 * @exception UtilException MalformedURLException
	 */
	public static URL[] getURLs(File... files) {
		final URL[] urls = new URL[files.length];
		try {
			for (int i = 0; i < files.length; i++) {
				urls[i] = files[i].toURI().toURL();
			}
		} catch (MalformedURLException e) {
			throw new UtilException("Error occured when get URL!",e);
		}

		return urls;
	}

	/**
	 * 补全相对路径
	 *
	 * @param baseUrl 基准URL
	 * @param relativePath 相对URL
	 * @return 相对路径
	 * @exception UtilException MalformedURLException
	 */
	public static String complateUrl(String baseUrl, String relativePath) {
		baseUrl = normalize(baseUrl, false);
		if (StringUtil.isBlank(baseUrl)) {
			return null;
		}

		try {
			final URL absoluteUrl = new URL(baseUrl);
			final URL parseUrl = new URL(absoluteUrl, relativePath);
			return parseUrl.toString();
		} catch (MalformedURLException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
	 * 
	 * @param url URL
	 * @return 编码后的URL
	 * @exception UtilException UnsupportedEncodingException
	 */
	public static String encodeAll(String url) {
		return encodeAll(url, StandardCharsets.UTF_8);
	}

	/**
	 * 编码URL<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
	 * 
	 * @param url URL
	 * @param charset 编码
	 * @return 编码后的URL
	 * @exception UtilException UnsupportedEncodingException
	 */
	public static String encodeAll(String url, Charset charset) throws UtilException {
		try {
			return URLEncoder.encode(url, charset.toString());
		} catch (UnsupportedEncodingException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于URL自动编码，类似于浏览器中键入地址自动编码，对于像类似于“/”的字符不再编码
	 * 
	 * @param url URL
	 * @return 编码后的URL
	 * @exception  UnsupportedEncodingException
	 *
	 */
	public static String encode(String url) throws UtilException {
		return encode(url, "UTF-8");
	}



	/**
	 * 编码字符为 application/x-www-form-urlencoded
	 *
	 * @param url 被编码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 *
	 */
	public static String encode(String url, String charset) throws UtilException {
		if (StringUtil.isEmpty(url)) {
			return url;
		}
		if (null == charset) {
			charset = "UTF-8";
		}

		try {
			return URLEncoder.encode(url, charset);
		} catch (UnsupportedEncodingException e) {
			throw new UtilException(e);
		}

	}

	/**
	 * 解码URL<br>
	 * 将%开头的16进制表示的内容解码。
	 *
	 * @param url URL
	 * @return 解码后的URL
	 * @exception UtilException UnsupportedEncodingException
	 *
	 */
	public static String decode(String url) throws UtilException {
		return decode(url, "UTF-8");
	}

	/**
	 * 解码application/x-www-form-urlencoded字符
	 *
	 * @param content 被解码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 *
	 */
	public static String decode(String content, Charset charset) {
		if (null == charset) {
			charset = StandardCharsets.UTF_8;
		}
		return decode(content, charset.name());
	}

	/**
	 * 解码URL<br>
	 * 将%开头的16进制表示的内容解码。
	 *
	 * @param url URL
	 * @param charset 编码
	 * @return 解码后的URL
	 * @exception UtilException UnsupportedEncodingException
	 */
	public static String decode(String url, String charset) throws UtilException {
		if (StringUtil.isEmpty(url)) {
			return url;
		}
		try {
			return URLDecoder.decode(url, charset);
		} catch (UnsupportedEncodingException e) {
			throw new UtilException( "Unsupported encoding: ["+charset+"]",e);
		}
	}

	/**
	 * 获得path部分<br>
	 *
	 * @param uriStr URI路径
	 * @return path
	 * @exception UtilException 包装URISyntaxException
	 */
	public static String getPath(String uriStr) {
		URI uri = null;
		try {
			uri = new URI(uriStr);
		} catch (URISyntaxException e) {
			throw new UtilException(e);
		}
		return uri.getPath();
	}

	/**
	 * 从URL对象中获取不被编码的路径Path<br>
	 * 对于本地路径，URL对象的getPath方法对于包含中文或空格时会被编码，导致本读路径读取错误。<br>
	 * 此方法将URL转为URI后获取路径用于解决路径被编码的问题
	 *
	 * @param url {@link URL}
	 * @return 路径
	 *
	 */
	public static String getDecodedPath(URL url) {
		if (null == url) {
			return null;
		}

		String path = null;
		try {
			// URL对象的getPath方法对于包含中文或空格的问题
			path = URLUtil.toURI(url).getPath();
		} catch (UtilException e) {
			// ignore
		}
		return (null != path) ? path : url.getPath();
	}

	/**
	 * 转URL为URI
	 *
	 * @param url URL
	 * @return URI
	 * @exception UtilException 包装URISyntaxException
	 */
	public static URI toURI(URL url) throws UtilException {
		return toURI(url, false);
	}

	/**
	 * 转URL为URI
	 *
	 * @param url URL
	 * @param isEncode 是否编码参数中的特殊字符（默认UTF-8编码）
	 * @return URI
	 * @exception UtilException 包装URISyntaxException
	 */
	public static URI toURI(URL url, boolean isEncode) throws UtilException {
		if (null == url) {
			return null;
		}

		return toURI(url.toString(), isEncode);
	}
	/**
	 * 转字符串为URI
	 *
	 * @param location 字符串路径
	 * @return URI
	 * @exception UtilException 包装URISyntaxException
	 */
	public static URI toURI(String location) throws UtilException {
		return toURI(location, false);
	}
	/**
	 * 转字符串为URI
	 *
	 * @param location 字符串路径
	 * @param isEncode 是否编码参数中的特殊字符（默认UTF-8编码）
	 * @return URI
	 * @exception UtilException 包装URISyntaxException
	 */
	public static URI toURI(String location, boolean isEncode) throws UtilException {
		try {
			if(isEncode){
				location = encode(location);
			}
			return new URI(location);
		} catch (Exception e) {
			throw new UtilException(e);
		}
	}


	/**
	 * 标准化URL字符串，包括：
	 * <pre>
	 * 1. 多个/替换为一个
	 * </pre>
	 *
	 * @param url URL字符串
	 * @return 标准化后的URL字符串
	 */
	public static String normalize(String url) {
		return normalize(url, false);
	}

	/**
	 * 标准化URL字符串，包括：
	 * <pre>
	 * 1. 多个/替换为一个
	 * </pre>
	 *
	 * @param url URL字符串
	 * @param isEncodePath 是否对URL中path部分的中文和特殊字符做转义（不包括http:和/）
	 * @return 标准化后的URL字符串
	 *
	 */
	public static String normalize(String url, boolean isEncodePath) {
		if (StringUtil.isBlank(url)) {
			return url;
		}
		final int sepIndex = url.indexOf("://");
		String protocol;
		String body;
		if (sepIndex > 0) {
			protocol = StringUtil.subPre(url, sepIndex + 3);
			body = StringUtil.subSuf(url, sepIndex + 3);
		} else {
			protocol = "http://";
			body = url;
		}

		final int paramsSepIndex = StringUtil.indexOf(body, '?');
		String params = null;
		if (paramsSepIndex > 0) {
			params = StringUtil.subSuf(body, paramsSepIndex);
			body = StringUtil.subPre(body, paramsSepIndex);
		}

		if(StringUtil.isNotEmpty(body)){
			// 去除开头的\或者/
			//noinspection ConstantConditions
			body = body.replaceAll("^[\\\\/]+", StringUtil.EMPTY);
			// 替换多个\或/为单个/
			body = body.replace("\\", "/").replaceAll("//+", "/");
		}

		final int pathSepIndex = StringUtil.indexOf(body, '/');
		String domain = body;
		String path = null;
		if (pathSepIndex > 0) {
			domain = StringUtil.subPre(body, pathSepIndex);
			path = StringUtil.subSuf(body, pathSepIndex);
		}
		if (isEncodePath) {
			path = encode(path);
		}
		return protocol + domain + StringUtil.nullToEmpty(path) + StringUtil.nullToEmpty(params);
	}
}
