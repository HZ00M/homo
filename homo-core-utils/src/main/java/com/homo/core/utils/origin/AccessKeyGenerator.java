package com.homo.core.utils.origin;

import java.util.Random;

/**
 * Created by 80072454 on 2016-11-15.
 */
public class AccessKeyGenerator {

    private static final String prefix = "!@#$%^&AWVLKHGBGEND)(#JKLVD*KCVNOSKNX>JIA((#JK(A%#&(*)&UJNHASFDLMXLKNJIOHAF";

    public static AccessObject generate() {
        AccessObject obj = new AccessObject();
        String data = prefix + System.currentTimeMillis() + new Random().nextInt(999999);
        String key = Digests.bit16(data, "utf-8");
        String secrent = genSecret(key);
        obj.setKey(key);
        obj.setSecret(secrent);
        return obj;
    }

    public static AccessObject generate(String data) {
        AccessObject obj = new AccessObject();
        String key = Digests.bit16(prefix + data, "utf-8");
        String secrent = genSecret(key);
        obj.setKey(key);
        obj.setSecret(secrent);
        return obj;
    }

    public static String genSecret(String key) {
        return Digests.md5(prefix, key, "utf-8");
    }

    public static class AccessObject {

        private String key;

        private String secret;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

//    public static void main(String [] args) {
//        for (int i = 0; i < 10; i++) {
//            AccessObject object = AccessKeyGenerator.generate();
//            System.out.println(object.getKey() + " " + object.getSecret() + " " + AccessKeyGenerator.genSecret(object.getKey()).equals(object.getSecret()));
//        }
//    }
}
