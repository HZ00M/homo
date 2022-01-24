package com.homo.core.redis.facade;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface HomoAsyncRedisPool extends HomoRedisPool {
    RedisFuture<Object> evalAsync(String script, String[] keys, String... args);

    Flux<Object> evalAsyncReactive(String script, String[] keys, String... args);

    RedisFuture<Object> evalAsync(String script, byte[][] keys, byte[]... args);

    Object eval(String script, String[] keys, String... args);

    RedisFuture<List<KeyValue<byte[], byte[]>>> hmgetAsync(byte[] key, byte[]... fields);

    RedisFuture<Long> hdelAsync(byte[] key, byte[]... args);

    RedisFuture<Long> hsetAsync(byte[] key, Map<byte[], byte[]> data);

    RedisFuture<Map<byte[], byte[]>> hgetallAsync(byte[] key);

    List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields);

    List<KeyValue<String, byte[]>> hsmget(String key, String... fields);

    RedisFuture<Object> evalAsync(String script, String[] keys, byte[]... args);

    RedisFuture<Long> hsetAsync(String key, Map<String, String> data);

    Mono<Long> hsetAsyncReactive(String key, Map<String, String> data);

    List<KeyValue<String, byte[]>> hmgetStringByte(String key, String... fields);

    String rename(String k1, String k2);

    Object eval(String script, String[] keys, byte[]... args);

    Flux<Object> evalAsyncReactive(String script, String[] keys, byte[]... args);

    StatefulRedisConnection<byte[], byte[]> getStatefulRedisConnection();

    StatefulRedisConnection<String, String> getStringStringConnection();
}
