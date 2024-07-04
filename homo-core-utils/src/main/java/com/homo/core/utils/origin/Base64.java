package com.homo.core.utils.origin;

import org.apache.commons.lang3.StringUtils;


/**
 * 提供Base64编码解码
 *
 * @author 80036381
 *         2011-1-7
 */
public class Base64 {
    /**
     * base64编码
     *
     * @param data     要编码的字符
     * @param encoding 字符编码，传空则使用默认编码
     * @return
     */
    public static String base64Encode(String data, String encoding) {
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }

        try {
            return base64Encode(data.getBytes(encoding));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * base64编码
     *
     * @param data 要编码的字符
     * @return
     */
    public static String base64Encode(byte[] data) {
        try {
            return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * base64解码
     *
     * @param data 要解码的字符
     * @return
     */
    public static byte[] base64Decode(String data) {
        try {
            return org.apache.commons.codec.binary.Base64.decodeBase64(data);
        } catch (Exception e) {
            return null;
        }
    }
}
