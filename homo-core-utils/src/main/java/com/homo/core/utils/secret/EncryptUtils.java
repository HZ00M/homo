package com.homo.core.utils.secret;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
public class EncryptUtils {
    public static String md5(String txt) {
        return encrypt(txt, "MD5");
    }

    public static String sha(String txt) {

        return encrypt(txt, "SHA");
    }

    public static String sha1(String txt) {

        return encrypt(txt, "SHA1");
    }

    private static String encrypt(String txt, String algorithmName) {
        if (txt == null || txt.trim().length() == 0) {
            return null;
        }
        if (algorithmName == null || algorithmName.trim().length() == 0) {
            algorithmName = "MD5";
        }
        String encryptTxt = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithmName);
            messageDigest.reset();
            messageDigest.update(txt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = messageDigest.digest();
            return hex(digest);
        } catch (Exception e) {
            log.error("encrypt error txt {} algorithmName {} e", txt, algorithmName, e);
        }
        return null;
    }

    private static String hex(byte[] bts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bts.length; i++) {
            sb.append(Integer.toHexString((bts[i] & 0xFF) | 0x100).substring(1, 3));
        }

        return sb.toString();
    }
}
