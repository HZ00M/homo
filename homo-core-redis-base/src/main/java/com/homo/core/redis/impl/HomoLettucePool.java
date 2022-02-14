package com.homo.core.redis.impl;

import com.homo.core.redis.facade.HomoAsyncRedisPool;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HomoLettucePool implements HomoAsyncRedisPool {
    RedisClient redisClient;

    StatefulRedisConnection<String,String> connection;
    StatefulRedisConnection<byte[],byte[]> byteConnection;
    StatefulRedisConnection<String,byte[]> stringByteConnection;

    public HomoLettucePool(RedisClient redisClient){
        this.redisClient =redisClient;
        this.connection = redisClient.connect();
        this.byteConnection = this.redisClient.connect(ByteArrayCodec.INSTANCE);
        this.stringByteConnection = redisClient.connect(new RedisCodec<String, byte[]>() {
            @Override
            public String decodeKey(ByteBuffer bytes) {
                return StringCodec.UTF8.decodeKey(bytes);
            }

            @Override
            public byte[] decodeValue(ByteBuffer bytes) {
                return ByteArrayCodec.INSTANCE.decodeValue(bytes);
            }

            @Override
            public ByteBuffer encodeKey(String key) {
                return StringCodec.UTF8.encodeKey(key);
            }

            @Override
            public ByteBuffer encodeValue(byte[] value) {
                return ByteArrayCodec.INSTANCE.encodeValue(value);
            }
        });
    }

    @Override
    public StatefulRedisConnection<String, String> getStringStringConnection(){
        return connection;
    }

    @Override
    public StatefulRedisConnection<byte[], byte[]> getStatefulRedisConnection(){
        return byteConnection;
    }

    @Override
    public Object eval(String script, String[] keys, byte[]... args){
        RedisCommands<String, byte[]> commands = stringByteConnection.sync();
        return commands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public String rename(String k1, String k2){
        RedisCommands<String, String> commands = connection.sync();
        return commands.rename(k1, k2);
    }

    @Override
    public List<KeyValue<String, byte[]>> hmgetStringByte(String key, String...fields){
        RedisCommands<String, byte[]> commands = stringByteConnection.sync();
        return commands.hmget(key, fields);
    }

    @Override
    public RedisFuture<Long> hsetAsync(String key, Map<String, String> data ){
        RedisAsyncCommands<String, String> commands = connection.async();
        return commands.hset(key, data);
    }

    @Override
    public Mono<Long> hsetAsyncReactive(String key, Map<String, String> data ){
        RedisReactiveCommands<String, String> commands = connection.reactive();
        return commands.hset(key, data);
    }

    @Override
    public RedisFuture<Object> evalAsync(String script, String[] keys, byte[]... args){
        RedisAsyncCommands<String, byte[]> commands = stringByteConnection.async();
        return commands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public Flux<Object> evalAsyncReactive(String script, String[] keys, byte[]... args){
        RedisReactiveCommands<String, byte[]> commands = stringByteConnection.reactive();
        return commands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]...fields){
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.hmget(key, fields);
    }

    @Override
    public List<KeyValue<String, byte[]>> hsmget(String key, String... fields) {
        RedisCommands<String, byte[]> commands = stringByteConnection.sync();
        return commands.hmget(key, fields);
    }

    @Override
    public RedisFuture<Map<byte[], byte[]>> hgetallAsync(byte[] key){
        RedisAsyncCommands<byte[], byte[]> commands = byteConnection.async();
        return commands.hgetall(key);
    }

    @Override
    public RedisFuture<Long> hsetAsync(byte[] key, Map<byte[], byte[]> data ){
        RedisAsyncCommands<byte[], byte[]> commands = byteConnection.async();
        return commands.hset(key, data);
    }

    @Override
    public RedisFuture<Long> hdelAsync(byte[] key, byte[]...fields){
        RedisAsyncCommands<byte[], byte[]> commands = byteConnection.async();
        return commands.hdel(key, fields);
    }

    public RedisFuture<List<KeyValue<byte[], byte[]>>> hmgetAsync(byte[] key, byte[]...fields){
        RedisAsyncCommands<byte[], byte[]> commands = byteConnection.async();
        return commands.hmget(key, fields);
    }
    @Override
    public RedisFuture<Object> evalAsync(String script, String[] keys, String... args) {
        RedisScriptingAsyncCommands<String, String> asyncCommands = connection.async();
        return asyncCommands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public Flux<Object> evalAsyncReactive(String script, String[] keys, String... args){
        RedisReactiveCommands<String, String> commands = connection.reactive();
        return commands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public RedisFuture<Object> evalAsync(String script, byte[][] keys, byte[]... args) {
        RedisScriptingAsyncCommands<byte[], byte[]> asyncCommands = byteConnection.async();
        return asyncCommands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public Object eval(String script, String[] keys, String... args) {
        RedisScriptingCommands<String, String> scriptingCommands = connection.sync();
        return scriptingCommands.eval(script, ScriptOutputType.MULTI, keys, args);
    }

    @Override
    public String set(String key, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        RedisCommands<String, String> commands = connection.sync();
//        SetArgs.Builder setArgsBuilder = SetArgs.Builder;
        return commands.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.set(key, value);
    }

    @Override
    public String get(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.get(key);
    }

    @Override
    public Boolean exists(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.exists(key) == 1;
    }

    @Override
    public Boolean persist(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.persist(key) ;
    }

    @Override
    public Long expire(String key, int seconds) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.expire(key, seconds) ? 1L : 0;
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.pexpire(key, milliseconds) ? 1L : 0;
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.expireat(key, unixTime) ? 1L : 0;
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.pexpireat(key, millisecondsTimestamp) ? 1L : 0;
    }

    @Override
    public Long setRange(String key, long offset, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.setrange(key, offset, value);
    }

    @Override
    public String getRange(String key, long startOffset, long endOffset) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.getset(key, value);
    }

    @Override
    public Long setNx(String key, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.setnx(key, value) ? 1L : 0;
    }

    @Override
    public String setEx(String key, int seconds, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.setex(key, seconds, value);
    }

    @Override
    public String psetEx(String key, long milliseconds, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(String key, long integer) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.decrby(key, integer);
    }

    @Override
    public Double decrByFloat(String key, double value) {
        return null;
    }

    @Override
    public Long decr(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.decr(key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.incrby(key, integer);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.incrbyfloat(key, value);
    }

    @Override
    public Long incr(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.incr(key);
    }

    @Override
    public Long hset(String key, String field, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hset(key, field, value) ? 1L : 0;
    }

    @Override
    public String hget(String key, String field) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hget(key, field);
    }

    @Override
    public Long hsetNx(String key, String field, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hsetnx(key, field, value) ? 1L : 0;
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hmget(key, fields).parallelStream().map(KeyValue::getValue).collect(Collectors.toList());
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hincrby(key, field, value);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hincrbyfloat(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hkeys(key).parallelStream().collect(Collectors.toSet());
    }

    @Override
    public List<String> hvals(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.hgetall(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.hgetall(key);
    }

    @Override
    public Long rpush(String key, String... string) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String... string) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lrange(key, start, end);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.ltrim(key, start, end);
    }

    @Override
    public String lindex(String key, long index) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.lpop(key);
    }

    @Override
    public String rpop(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.rpop(key);
    }

    @Override
    public Long sadd(String key, String... member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.smembers(key);
    }

    @Override
    public Long srem(String key, String... member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.srem(key, member);
    }

    @Override
    public Long scard(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.sismember(key, member);
    }

    @Override
    public String srandmember(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.srandmember(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.srandmember(key, count);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zadd(key, score, member);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
//        RedisCommands<String, String> commands = connection.sync();
//        return commands.zadd(key, scoreMembers, params);
        return null;
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrange(key, start, end).parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Long zrem(String key, String... member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrem(key, member);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zincrby(key, score, member);
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
//        RedisCommands<String, String> commands = connection.sync();
//        return commands.zincrby();
        return null;
    }


    @Override
    public Long zrank(String key, String member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrank(key, member);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
//        RedisCommands<String, String> commands = connection.sync();
//        return commands.zrangeWithScores();
        return null;
    }

    @Override
    public Long zcard(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zscore(key, member);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zcount(key, min, max);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrangebyscore(key, min, max).parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrangebyscore(key, min, max, offset, count).parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.zrangebyscore(key, min, max, offset, count).parallelStream().collect(Collectors.toSet());
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
//        RedisCommands<String, String> commands = connection.sync();
//        return commands.zrangebyscoreWithScores(key, min, max).parallelStream().;
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return null;
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        return null;
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return null;
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return null;
    }

    @Override
    public Long del(String key) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.del(key);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        RedisScriptingCommands<String, String> commands = connection.sync();
        String[] keyArray = new String[keys.size()];
        String[] argsArray = new String[args.size()];
        keyArray = keys.toArray(keyArray);
        argsArray = args.toArray(argsArray);
        return commands.eval(script, ScriptOutputType.MULTI, keyArray, argsArray);
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        RedisScriptingCommands<byte[], byte[]> commands = byteConnection.sync();
        byte[][] keyArray = new byte[keys.size()][];
        byte[][] argsArray = new byte[args.size()][];
        keyArray = keys.toArray(keyArray);
        argsArray = args.toArray(argsArray);
        return commands.eval(script, ScriptOutputType.MULTI, keyArray, argsArray);
    }

    @Override
    public String select(int index) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.select(index);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.hget(key, field);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.hset(key, field, value) ? 1L : 0;
    }

    @Override
    public Long decr(byte[] key) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.decr(key);
    }

    @Override
    public Long incr(byte[] key) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.incr(key);
    }

    @Override
    public byte[] get(byte[] key) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.get(key);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.set(key, value);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        RedisCommands<byte[], byte[]> commands = byteConnection.sync();
        return commands.setex(key, seconds, value);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        return null;
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long end) {
        return null;
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        return null;
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return null;
    }
}
