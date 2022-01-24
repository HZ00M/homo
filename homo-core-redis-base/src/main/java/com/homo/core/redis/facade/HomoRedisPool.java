package com.homo.core.redis.facade;

import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HomoRedisPool {
    String set(String key,String value);

    String set(String key,String value,String nxxx);

    /**
     * @param  nxxx – NX|XX, NX -- Only set the key if it does not already exist. XX -- Only set the key if it already exist.
     * @param  expx – EX|PX, expire time units: EX = seconds; PX = milliseconds
     * @return
     */
    String set(String key,String value,String nxxx,String expx, long time);

    String get(String key);

    Boolean exists(String key);

    Boolean persist(String key);

    Long expire(String key,int seconds);

    Long pexpire(String key, long milliseconds);

    Long expireAt(String key, long unixTime);

    Long pexpireAt(String key, long millisecondsTimestamp);

    Long setRange(String key,long offset,String value);

    String getRange(String key,long startOffset,long endOffset);

    String getSet(String key,String value);

    Long setNx(String key,String value);

    String setEx(String key,int seconds,String value);

    String psetEx(String key,long millisSeconds,String value);

    Long incr(String key);

    Long incrBy(String key,long value);

    Double incrByFloat(String key, double value);

    Long decr(String key);

    Long decrBy(String key, long integer);

    Double decrByFloat(String key, double value);

    Long hset(String key, String field, String value);

    Long hsetNx(String key, String field, String value);

    String hmset(String key, Map<String, String> hash);

    String hget(String key, String field);

    List<String> hmget(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Double hincrByFloat(String key, String field, double value);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... field);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Map<String, String> hgetAll(String key);

    Map<byte[], byte[]> hgetAll(byte[] key);

    Long rpush(String key, String... string);

    Long lpush(String key, String... string);

    Long llen(String key);

    List<String> lrange(String key, long start, long end);

    /**
     * 对列表进行修剪，只保留start 到end 之间的元素
     * @param key
     * @param start
     * @param end
     * @return
     */
    String ltrim(String key, long start, long end);

    /**
     * 获取列表在某个键上偏移量为index的值
     * @param key
     * @param index
     * @return
     */
    String lindex(String key, long index);

    String lset(String key, long index, String value);

    /**
     * Redis Lrem 根据参数 COUNT 的值，移除列表中与参数 VALUE 相等的元素。
     *
     * COUNT 的值可以是以下几种：
     *
     * count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为 COUNT 。
     * count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值。
     * count = 0 : 移除表中所有与 VALUE 相等的值。
     * @param key
     * @param count
     * @param value
     * @return
     */
    Long lrem(String key, long count, String value);

    String lpop(String key);

    String rpop(String key);

    Long sadd(String key, String... member);

    Set<String> smembers(String key);

    Long srem(String key, String... member);

    Long scard(String key);

    Boolean sismember(String key, String member);

    String srandmember(String key);

    List<String> srandmember(String key, int count);

    public Long zadd(String key, double score, String member);

    Long zadd(String key, double score, String member, ZAddParams params);

    Long zadd(String key, Map<String, Double> scoreMembers);

    Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    Set<String> zrange(String key, long start, long end);

    Long zrem(String key, String... member);

    Double zincrby(String key, double score, String member);

    Double zincrby(String key, double score, String member, ZIncrByParams params);

    Long zrank(String key, String member);

    Set<Tuple> zrangeWithScores(String key, long start, long end);

    Long zcard(String key);

    Double zscore(String key, String member);

    Long zcount(String key, double min, double max);

    Long zcount(String key, String min, String max);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    Long zremrangeByScore(String key, double start, double end);

    Long zremrangeByScore(String key, String start, String end);

    Long zlexcount(String key, String min, String max);

    Set<String> zrangeByLex(String key, String min, String max);

    /**
     * 返回指定成员区间内的成员 LEX结尾的指令是要求分数必须相同
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return
     */
    Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByLex(String key, String max, String min);

    Set<String> sdiff(String... keys);

    Long del(String key);

    Object eval(String script, List<String> keys, List<String> args);

    Object eval(final byte[] script, final List<byte[]> keys, final List<byte[]> args);

    String select(int index);
    byte[] hget(byte[] key, byte[] field);

    Long hset(byte[] key, byte[] field, byte[] value);

    Long decr(byte[] key);

    Long incr(byte[] key);

    byte[] get(byte[] key);

    String set(byte[] key, byte[] value);

    String setex(byte[] key, int seconds, byte[] value);

    Set<byte[]> zrevrange(byte[] key, long start, long end);

    Set<byte[]> zrange(byte[] key, long start, long end);

    Set<byte[]> zrangeByScore(byte[] key, double min, double max);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count);

    Set<Tuple> zrangeWithScores(byte[] key, long start, long end);

    Long zadd(byte[] key, double score, byte[] member);


}
