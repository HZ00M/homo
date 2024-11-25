/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.homo.core.utils.origin;

import com.homo.core.utils.origin.exceptions.UtilException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;


/**
 * 支持SHA-1/MD5消息摘要的工具类.
 * <p>
 * 返回ByteSource，可进一步被编码为Hex, Base64或UrlSafeBase64
 *
 */
public class Digests {

    private static final String SHA1 = "SHA-1";
    private static final String MD5 = "MD5";

    private static SecureRandom random = new SecureRandom();

    /**
     * 对输入字符串进行sha1散列.
     */
    public static byte[] sha1(byte[] input) {
        return digest(input, SHA1, null, 1);
    }

    public static byte[] sha1(byte[] input, byte[] salt) {
        return digest(input, SHA1, salt, 1);
    }

    public static byte[] sha1(byte[] input, byte[] salt, int iterations) {
        return digest(input, SHA1, salt, iterations);
    }

    /**
     * 对字符串进行散列, 支持md5与sha1算法.
     */
    private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new UtilException(e);
        }
    }

    /**
     * 生成随机的Byte[]作为salt.
     *
     * @param numBytes byte数组的大小
     */
    public static byte[] generateSalt(int numBytes) {
        Validate.isTrue(numBytes > 0, "numBytes argument must be a positive integer (1 or larger)", numBytes);

        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * 对文件进行md5散列.
     */
    public static byte[] md5(InputStream input) throws IOException {
        return digest(input, MD5);
    }

    /**
     * 对文件进行sha1散列.
     */
    public static byte[] sha1(InputStream input) throws IOException {
        return digest(input, SHA1);
    }

    private static byte[] digest(InputStream input, String algorithm) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            int bufferLength = 8 * 1024;
            byte[] buffer = new byte[bufferLength];
            int read = input.read(buffer, 0, bufferLength);

            while (read > -1) {
                messageDigest.update(buffer, 0, read);
                read = input.read(buffer, 0, bufferLength);
            }

            return messageDigest.digest();
        } catch (GeneralSecurityException e) {
            throw new UtilException(e);
        }
    }


    /**
     * md5散列
     *
     * @param text
     * @param key
     * @param encoding
     * @return
     */
    public static String md5(String text, String key, String encoding) {
        if ((key == null) || (key.equals(""))) {
            return md5(text, encoding);
        }
        return md5(md5(text, encoding) + key, encoding);
    }

    public static String md5(String text, String encoding) {
        if (text == null) {
            return null;
        }

        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }

        String s = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes(encoding));
            byte[] tmp = md.digest();
            s = toHex(tmp);
        } catch (Exception localException) {
        }
        return s;
    }

    public static String bit16(String text, String encoding) {
        try {
            String str = md5(text, encoding);
            return str.substring(8, 24);
        } catch (Exception e) {
        }
        return null;
    }

    public static String toHex(byte[] input) {
        if (input == null)
            return null;
        StringBuilder output = new StringBuilder(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xFF;
            if (current < 16)
                output.append("0");
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

}
