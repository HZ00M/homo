package com.homo.core.utils.origin;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * 3DES加解密工具类
 *
 * @author 80058500
 */
public class EncryptUtil {

    public static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * 3DES密钥长度
     */
    public static final int KEY_LENGTH = 24;
    private static final Logger logger = LoggerFactory.getLogger(EncryptUtil.class);
    /**
     * 3DES的算法名称为DESede
     */
    private static final String ALGORITHM_NAME = "DESede";

    /**
     * 加密
     *
     * @param key 加密密钥
     * @param src 加密数据
     * @return
     */
    public static byte[] encrypt(byte[] key, byte[] src) throws Exception {
        SecretKey deskey = new SecretKeySpec(key, ALGORITHM_NAME);
        Cipher c = Cipher.getInstance(ALGORITHM_NAME);
        c.init(Cipher.ENCRYPT_MODE, deskey);
        return c.doFinal(src);
    }

    /**
     * 解密
     *
     * @param key 解密密钥
     * @param src 解密数据
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] key, byte[] src) throws Exception {
        SecretKey deskey = new SecretKeySpec(key, ALGORITHM_NAME);
        Cipher c = Cipher.getInstance(ALGORITHM_NAME);
        c.init(Cipher.DECRYPT_MODE, deskey);
        return c.doFinal(src);
    }

    public static byte[] getKey(String keyStr, String charsetName) throws Exception {
        if (StringUtils.isBlank(charsetName)) {
            charsetName = DEFAULT_CHARSET;
        }
        byte[] key = keyStr.getBytes(charsetName);
        return getBytesByLength(key, KEY_LENGTH, (byte) '0');
    }

    /**
     * 获取指定长度的byte数组 如果过长，则截取前面指定长度的元素；如果长度不够，则在前面补充指定的byte值
     *
     * @param key         原数组
     * @param length      指定长度
     * @param defaultByte 需要填充的byte值
     * @return
     */
    private static byte[] getBytesByLength(byte[] key, int length, byte defaultByte) {
        if (key.length == length) {
            return key;
        } else {
            byte[] result = new byte[length];
            if (key.length > length) {
                //截取
                for (int i = 0; i < length; i++) {
                    result[i] = key[i];
                }
            } else {
                //填充
                int defaultCount = length - key.length;
                for (int i = 0; i < length; i++) {
                    if (i < defaultCount) {
                        result[i] = defaultByte;
                    } else {
                        result[i] = key[i - defaultCount];
                    }
                }
            }
            return result;
        }
    }

    /**
     * 使用密钥对字符串加密
     *
     * @param key     密钥
     * @param content 要加密的字符串
     * @return 经过Base64编码的字符串
     * @throws Exception
     */
    public static String encryptStr(String key, String content, String charsetName) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        if (StringUtils.isBlank(charsetName)) {
            charsetName = DEFAULT_CHARSET;
        }
        byte[] result;
        try {
            result = encrypt(getKey(key, charsetName), content.getBytes(charsetName));
            return Base64.base64Encode(result);
        } catch (Exception e) {
            StringBuilder error = new StringBuilder();
            error.append("key:").append(key).append(",")
                    .append("content:").append(content).append(",")
                    .append("charsetName:").append(charsetName);
            logger.warn("加密失败, " + error.toString(), e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * 使用密钥解密字符串
     *
     * @param key         密钥
     * @param result      经过Base64编码过的加密结果
     * @param charsetName
     * @return 原文字符串
     * @throws Exception
     */
    public static String decryptStr(String key, String result, String charsetName) {
        if (StringUtils.isBlank(result)) {
            return null;
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = DEFAULT_CHARSET;
        }
        try {
            byte[] content = decrypt(getKey(key, charsetName), Base64.base64Decode(result));
            return new String(content, charsetName);
        } catch (Exception e) {
            StringBuilder error = new StringBuilder();
            error.append("key:").append(key).append(",")
                    .append("result:").append(result).append(",")
                    .append("charsetName:").append(charsetName);
            logger.warn("解密失败, " + error.toString(), e);
        }
        return StringUtils.EMPTY;
    }
}
