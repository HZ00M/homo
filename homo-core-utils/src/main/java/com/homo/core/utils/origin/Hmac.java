package com.homo.core.utils.origin;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <p>Project: oppo-framework-parent</p>
 * <p>Title: Hmac</p>
 * <p>Description: Hmac算法</p>
 * <p>Copyright (c) 2015 www.oppo.com Inc. All rights reserved.</p>
 * <p>Company: OPPO</p>
 *
 * @author Yibowen
 * @time 16:06
 * @since 2015-06-02
 */
public class Hmac {

    public final static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 对报文进行hmac-sha1签名
     *
     * @param aValue - 字符串
     * @param aKey   - 密钥
     * @return - 签名结果，hex字符串
     */
    public static String signSHA1(String aValue, String aKey) {
        return signSHA1(aValue, aKey, DEFAULT_ENCODING);
    }

    /**
     * 对报文进行hmac-sha1签名
     *
     * @param aValue   - 字符串
     * @param aKey     - 密钥
     * @param encoding - 字符串编码方式
     * @return - 签名结果，hex字符串
     */
    public static String signSHA1(String aValue, String aKey, String encoding) {
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }

        byte[] value;
        byte[] key;
        try {
            value = aValue.getBytes(encoding);
            key = aKey.getBytes(encoding);
        } catch (UnsupportedEncodingException e1) {
            value = aValue.getBytes();
            key = aKey.getBytes();
        }

        SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
        } catch (Exception e) {
            return null;
        }

        byte[] rawHmac = mac.doFinal(value);
        return toHex(rawHmac);
    }

    /**
     * 对报文进行hmac-md5签名
     *
     * @param aValue - 字符串
     * @param aKey   - 密钥
     * @return - 签名结果，hex字符串
     */
    public static String hmacSign(String aValue, String aKey) {
        return signMD5(aValue, aKey, "UTF-8");
    }

    /**
     * 对报文进行hmac-md5签名
     *
     * @param aValue - 字符串
     * @param aKey   - 密钥
     * @return - 签名结果，hex字符串
     */
    public static String signMD5(String aValue, String aKey) {
        return signMD5(aValue, aKey, "UTF-8");
    }

    /**
     * 对报文进行hmac-md5签名
     *
     * @param aValue   - 字符串
     * @param aKey     - 密钥
     * @param encoding - 字符串编码方式
     * @return - 签名结果，hex字符串
     */
    public static String signMD5(String aValue, String aKey, String encoding) {
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }

        byte k_ipad[] = new byte[64];
        byte k_opad[] = new byte[64];
        byte keyb[];
        byte value[];
        try {
            keyb = aKey.getBytes(encoding);
            value = aValue.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            keyb = aKey.getBytes();
            value = aValue.getBytes();
        }

        Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
        Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
        for (int i = 0; i < keyb.length; i++) {
            k_ipad[i] = (byte) (keyb[i] ^ 0x36);
            k_opad[i] = (byte) (keyb[i] ^ 0x5c);
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(k_ipad);
        md.update(value);
        byte dg[] = md.digest();
        md.reset();
        md.update(k_opad);
        md.update(dg, 0, 16);
        dg = md.digest();
        return toHex(dg);
    }

    private static String toHex(byte[] input) {
        if (input == null)
            return null;
        StringBuilder output = new StringBuilder(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            //小于16的需要补充一位(共2位)
            if (current < 16) {
                output.append('0');
            }

            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

}
