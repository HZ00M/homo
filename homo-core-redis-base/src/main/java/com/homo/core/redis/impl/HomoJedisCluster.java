package com.homo.core.redis.impl;

import com.homo.core.redis.facade.HomoRedisPool;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomoJedisCluster implements HomoRedisPool {

    private JedisCluster jedisCluster;

    public HomoJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Long del(String key) {
        return jedisCluster.del(key);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return jedisCluster.eval(script, keys, args);
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return jedisCluster.eval(script, keys, args);
    }

    @Override
    public String select(int index) {
        return jedisCluster.select(index);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return jedisCluster.hget(key, field);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return jedisCluster.hset(key, field, value);
    }

    @Override
    public Long decr(byte[] key) {
        return jedisCluster.decr(key);
    }

    @Override
    public Long incr(byte[] key) {
        return jedisCluster.incr(key);
    }

    @Override
    public byte[] get(byte[] key) {
        return jedisCluster.get(key);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return jedisCluster.set(key, value);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return jedisCluster.setex(key, seconds, value);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        return jedisCluster.zrevrange(key, start, end);
    }

    @Override
    public String set(String key, String value) {
        return jedisCluster.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return jedisCluster.set(key, value, nxxx, expx, time);
    }

    @Override
    public String set(String key, String value, String nxxx) {
        return jedisCluster.set(key, value, nxxx);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public Boolean exists(String key) {
        return jedisCluster.exists(key);
    }

    @Override
    public Boolean persist(String key) {
        return  jedisCluster.persist(key)>0?Boolean.TRUE:Boolean.FALSE;
    }

    @Override
    public Long expire(String key, int seconds) {
        return jedisCluster.expire(key,  seconds);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return jedisCluster.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return jedisCluster.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return jedisCluster.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long setRange(String key, long offset, String value) {
        return jedisCluster.setrange(key, offset, value);
    }

    @Override
    public String getRange(String key, long startOffset, long endOffset) {
        return jedisCluster.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        return jedisCluster.getSet(key, value);
    }

    @Override
    public Long setNx(String key, String value) {
        return jedisCluster.setnx(key, value);
    }

    @Override
    public String setEx(String key, int seconds, String value) {
        return jedisCluster.setex(key, seconds, value);
    }

    @Override
    public String psetEx(String key, long milliseconds, String value) {
        return jedisCluster.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(String key, long integer) {
        return jedisCluster.decrBy(key, integer);
    }

    @Override
    public Double decrByFloat(String key, double value) {
        throw new RuntimeException("decrByFloat not support !!!");
    }

    @Override
    public Long decr(String key) {
        return jedisCluster.decr(key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return jedisCluster.incrBy(key, integer);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return jedisCluster.incrByFloat(key, value);
    }

    @Override
    public Long incr(String key) {
        return jedisCluster.incr(key);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return jedisCluster.hset(key, field, value);
    }

    @Override
    public String hget(String key, String field) {
        return jedisCluster.hget(key, field);
    }

    @Override
    public Long hsetNx(String key, String field, String value) {
        return jedisCluster.hsetnx(key, field, value);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return jedisCluster.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return jedisCluster.hmget(key, fields);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return jedisCluster.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return jedisCluster.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return jedisCluster.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        return jedisCluster.hdel(key,  field);
    }

    @Override
    public Long hlen(String key) {
        return jedisCluster.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return jedisCluster.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return jedisCluster.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return jedisCluster.hgetAll(key);
    }

    @Override
    public Long rpush(String key, String... string) {
        return jedisCluster.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String... string) {
        return jedisCluster.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        return jedisCluster.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return jedisCluster.lrange(key, start, end);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return jedisCluster.ltrim(key, start, end);
    }

    @Override
    public String lindex(String key, long index) {
        return jedisCluster.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        return jedisCluster.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return jedisCluster.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        return jedisCluster.lpop(key);
    }

    @Override
    public String rpop(String key) {
        return jedisCluster.rpop(key);
    }

    @Override
    public Long sadd(String key, String... member) {
        return jedisCluster.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        return jedisCluster.smembers(key);
    }

    @Override
    public Long srem(String key, String... member) {
        return jedisCluster.srem(key, member);
    }

    @Override
    public Long scard(String key) {
        return jedisCluster.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return jedisCluster.sismember(key, member);
    }

    @Override
    public String srandmember(String key) {
        return jedisCluster.srandmember(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return jedisCluster.srandmember(key, count);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return jedisCluster.zadd(key, score, member);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return jedisCluster.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return jedisCluster.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return jedisCluster.zadd(key, scoreMembers, params);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return jedisCluster.zrange(key, start, end);
    }

    @Override
    public Long zrem(String key, String... member) {
        return jedisCluster.zrem(key, member);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return jedisCluster.zincrby(key, score, member);
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
        return jedisCluster.zincrby(key, score, member, params);
    }

    @Override
    public Long zrank(String key, String member) {
        return jedisCluster.zrank(key, member);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return jedisCluster.zrangeWithScores(key, start, end);
    }

    @Override
    public Long zcard(String key) {
        return jedisCluster.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return jedisCluster.zscore(key, member);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return jedisCluster.zcount(key, min, max);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return jedisCluster.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return jedisCluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return jedisCluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return jedisCluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return jedisCluster.zrangeByScoreWithScores(key, min,  max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return jedisCluster.zrangeByScoreWithScores(key, min,  max, offset, count);
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return jedisCluster.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        return jedisCluster.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return jedisCluster.zlexcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return jedisCluster.zrangeByLex(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return jedisCluster.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return jedisCluster.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return jedisCluster.sdiff(keys);
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long end) {
        return jedisCluster.zrange(key, start, end);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return jedisCluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        return jedisCluster.zrangeWithScores(key, start, end);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return jedisCluster.zadd(key, score, member);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return jedisCluster.hgetAll(key);
    }
}
