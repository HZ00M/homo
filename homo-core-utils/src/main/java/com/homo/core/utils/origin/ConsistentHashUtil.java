package com.homo.core.utils.origin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 一致性hash
 * zhishui
 */
public class ConsistentHashUtil {

    private final static ConcurrentMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    public static String doSelect(String tag, List<String> nodeList, String key) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String node: nodeList) {
            stringBuffer.append(node);
        }

        String identityHashCode = ConsistentHashUtil.hashCode(stringBuffer.toString());

        ConsistentHashSelector selector = (ConsistentHashSelector) selectors.get(tag);
        if (selector == null || selector.identityHashCode.equals(identityHashCode)) {
            selectors.put(key, new ConsistentHashSelector(nodeList, identityHashCode));
            selector = (ConsistentHashSelector) selectors.get(key);
        }
        return selector.select(key);
    }

    private static final class ConsistentHashSelector {

        private final TreeMap<Long, String> virtualNodes;

        private final int replicaNumber = 160;

        private final String identityHashCode;

        ConsistentHashSelector(List<String> nodeList, String identityHashCode) {
            this.virtualNodes = new TreeMap<Long, String>();
            this.identityHashCode = identityHashCode;

            for (String node: nodeList) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(node + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualNodes.put(m, node);
                    }
                }
            }
        }

        public String select(String key) {
            byte[] digest = md5(key);
            return selectForKey(hash(digest, 0));
        }

        private String selectForKey(long hash) {
            Map.Entry<Long, String> entry = virtualNodes.ceilingEntry(hash);
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        }

        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        private byte[] md5(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.reset();
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            md5.update(bytes);
            return md5.digest();
        }
    }

    private static String hashCode(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        md5.reset();
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        md5.update(bytes);
        return md5.toString();
    }
}
