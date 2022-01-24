package com.homo.core.redis.impl;

import com.homo.core.redis.facade.HomoRedisPool;
import io.codis.jodis.RoundRobinJedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class HomoJodisPool  implements HomoRedisPool{
    private RoundRobinJedisPool robinJedisPool;

    public HomoJodisPool(RoundRobinJedisPool robinJedisPool){
        this.robinJedisPool = robinJedisPool;
    }

    protected Jedis getResource() {
        return robinJedisPool.getResource();
    }

    @Override
    public String set(String key, String value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.set(key, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when set key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.set(key, value, nxxx, expx, time);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when set key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String set(String key, String value, String nxxx) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.set(key, value, nxxx);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when set key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String get(String key) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.get(key);
        } catch (JedisConnectionException e) {
            log.error("connect jedis failed.");
        } catch (Exception e) {
            log.error("get failed. key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Boolean exists(String key) {
        Boolean result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.exists(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server", e);
        } catch (Exception e) {
            log.error("exists error! ", e);
        } finally {
            return result;
        }
    }

    @Override
    public Boolean persist(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.persist(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server  persist when key=" + key, e);
        } catch (Exception e) {
            log.error("persist key error! key=" + key, e);
        } finally {
            return result > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.expire(key, seconds);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when key=" + key, e);
        } catch (Exception e) {
            log.error("expire key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.pexpire(key, milliseconds);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when pexpire key" + key, e);
        } catch (Exception e) {
            log.error("pexpire error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.expireAt(key, unixTime);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when expireAt key" + key, e);
        } catch (Exception e) {
            log.error("expireAt error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.pexpireAt(key, millisecondsTimestamp);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when pexpireAt key" + key, e);
        } catch (Exception e) {
            log.error("pexpireAt error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long setRange(String key, long offset, String value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.setrange(key, offset, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when setrange key" + key, e);
        } catch (Exception e) {
            log.error("setrange error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String getRange(String key, long startOffset, long endOffset) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.getrange(key, startOffset, endOffset);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when getrange key" + key, e);
        } catch (Exception e) {
            log.error("getrange error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String getSet(String key, String value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.getSet(key, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when getSet key" + key, e);
        } catch (Exception e) {
            log.error("getSet error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long setNx(String key, String value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.setnx(key, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when setnx key" + key, e);
        } catch (Exception e) {
            log.error("setnx error! key=" + key);
        } finally {
            return result;
        }
    }


    @Override
    public String setEx(String key, int seconds, String value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.setex(key, seconds, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when setex key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String psetEx(String key, long millisSeconds, String value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.psetex(key, millisSeconds, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when psetex key" + key, e);
        } catch (Exception e) {
            log.error("psetex error! key=" + key);
        } finally {
            return result;
        }
    }


    @Override
    public Long decrBy(String key, long integer) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.decrBy(key, integer);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when decrBy key=" + key, e);
        } catch (Exception e) {
            log.error("decrByString key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Double decrByFloat(String key, double value) {
        return null;
    }

    @Override
    public Long decr(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.decr(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when decr key=" + key, e);
        } catch (Exception e) {
            log.error("decr key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long incrBy(String key, long val) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.incrBy(key, val);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when incrby key=" + key + "-" + val, e);
        } catch (Exception e) {
            log.error("incrby key error! key=" + key + "-" + val, e);
        } finally {
            return result;
        }
    }

    @Override
    public Double incrByFloat(String key, double value) {
        Double result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.incrByFloat(key, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when incrByFloat key" + key, e);
        } catch (Exception e) {
            log.error("incrByFloat error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long incr(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.incr(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when incr key=" + key, e);
        } catch (Exception e) {
            log.error("incr key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hset(String key, String field, String value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hset(key, field, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hset key-field=" + key + "-" + field, e);
        } catch (Exception e) {
            log.error("hset key error! key=" + key + "-" + field, e);
        } finally {
            return result;
        }
    }

    @Override
    public String hget(String key, String field) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hget(key, field);
        } catch (JedisConnectionException e) {
            log.error("connect jedis failed.");
        } catch (Exception e) {
            log.error("hget failed. key=" + key + "-" + field, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hsetNx(String key, String field, String value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hsetnx(key, field, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hsetnx key" + key, e);
        } catch (Exception e) {
            log.error("hsetnx error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hmset(key, hash);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hmsetString key-field=" + key, e);
        } catch (Exception e) {
            log.error("hmsetString key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        List<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hmget(key, fields);
        } catch (JedisConnectionException e) {
            log.error("connect jedis failed.");
        } catch (Exception e) {
            log.error("hmget failed. key=" + key + "-" + fields, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hincrBy(key, field, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hgetAll key=" + key, e);
        } catch (Exception e) {
            log.error("hincrBy key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        Double result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hincrByFloat(key, field, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hincrByFloat key" + key, e);
        } catch (Exception e) {
            log.error("hincrByFloat error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Boolean hexists(String key, String field) {
        Boolean result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hexists(key, field);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hexists key=" + key, e);
        } catch (Exception e) {
            log.error("hexists key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hdel(String key, String... field) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hdel(key, field);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hgetAll key=" + key, e);
        } catch (Exception e) {
            log.error("hdel key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hlen(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hlen(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hlen key" + key, e);
        } catch (Exception e) {
            log.error("hlen error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> hkeys(String key) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hkeys(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hkeys key" + key, e);
        } catch (Exception e) {
            log.error("hkeys error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public List<String> hvals(String key) {
        List<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hvals(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hvals key" + key, e);
        } catch (Exception e) {
            log.error("hvals error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Map<String, String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hgetAll(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hgetAll key=" + key, e);
        } catch (Exception e) {
            log.error("hgetAll key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long rpush(String key, String... string) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.rpush(key, string);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lpush key-field=" + key, e);
        } catch (Exception e) {
            log.error("rpush key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long lpush(String key, String... string) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lpush(key, string);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lpush key-field=" + key, e);
        } catch (Exception e) {
            log.error("listGetAll key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long llen(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.llen(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when llen key" + key, e);
        } catch (Exception e) {
            log.error("llen error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        List<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lrange(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lrange key-field=" + key, e);
        } catch (Exception e) {
            log.error("lrange key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String ltrim(String key, long start, long end) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.ltrim(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when ltrim key" + key, e);
        } catch (Exception e) {
            log.error("ltrim error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String lindex(String key, long index) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lindex(key, index);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lindex key" + key, e);
        } catch (Exception e) {
            log.error("lindex error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String lset(String key, long index, String value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lset(key, index, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lset key" + key, e);
        } catch (Exception e) {
            log.error("lset error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long lrem(String key, long count, String value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lrem(key, count, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lrem key" + key, e);
        } catch (Exception e) {
            log.error("lrem key error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String lpop(String key) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.lpop(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when lpop key" + key, e);
        } catch (Exception e) {
            log.error("lpop key error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public String rpop(String key) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.rpop(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when rpop key" + key, e);
        } catch (Exception e) {
            log.error("rpop key error! key=" + key);
        } finally {
            return result;
        }
    }

    @Override
    public Long sadd(String key, String... member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.sadd(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when sadd key-member=" + key + "-" + member, e);
        } catch (Exception e) {
            log.error("sadd key error! key=" + key + "-" + member, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> smembers(String key) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.smembers(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when smembers key=" + key, e);
        } catch (Exception e) {
            log.error("smembers key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long srem(String key, String... member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.srem(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when srem key=" + key, e);
        } catch (Exception e) {
            log.error("srem key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long scard(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.scard(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when scard key=" + key, e);
        } catch (Exception e) {
            log.error("scard key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Boolean sismember(String key, String member) {
        Boolean result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.sismember(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when sismember key-member=" + key + "-" + member, e);
        } catch (Exception e) {
            log.error("sismember key error! key=" + key + "-" + member, e);
        } finally {
            return result;
        }
    }

    @Override
    public String srandmember(String key) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.srandmember(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when srandmember key=" + key, e);
        } catch (Exception e) {
            log.error("srandmember key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public List<String> srandmember(String key, int count) {
        List<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.srandmember(key, count);
        }
        return result;
    }

    @Override
    public Long zadd(String key, double score, String member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zadd(key, score, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zadd key-score-member=" + key + "-" + score + "-" + member, e);
        } catch (Exception e) {
            log.error("zadd key error! key=" + key + "-" + score + "-" + member, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zadd(key, score, member, params);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zadd key-score-member=" + key + "-" + score + "-" + member, e);
        } catch (Exception e) {
            log.error("zadd key error! key=" + key + "-" + score + "-" + member, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zadd(key, scoreMembers);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zadd key-score-member=" + key, e);
        } catch (Exception e) {
            log.error("zadd key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zadd(key, scoreMembers, params);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zadd key-score-member=" + key, e);
        } catch (Exception e) {
            log.error("zadd key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrange(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrange key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zrange method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zrem(String key, String... member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrem(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrem key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zrem method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        Double result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zincrby(key, score, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zincrby key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zincrby method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
        Double result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zincrby(key, score, member, params);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zincrby key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zincrby method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zrank(String key, String member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrank(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrank key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zrank method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeWithScores(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeWithScores key=" + key, e);
            return null;
        } catch (Exception e) {
            log.error("JedisUtils zrangeWithScores method error:", e);
            return null;
        } finally {
            return result;
        }
    }

    @Override
    public Long zcard(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zcard(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zcard key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zcard method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Double zscore(String key, String member) {
        Double result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zscore(key, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zscore key=" + key, e);
        } catch (Exception e) {
            log.error("JedisUtils zscore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zcount(String key, double min, double max) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zcount(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zcount key=" + key, e);
        } catch (Exception e) {
            log.error("zcount method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zcount(String key, String min, String max) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zcount(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zcount key=" + key, e);
        } catch (Exception e) {
            log.error("zcount method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScore(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScore(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zremrangeByScore(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zremrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zremrangeByScore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zremrangeByScore(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zremrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zremrangeByScore method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zlexcount(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zlexcount key=" + key, e);
        } catch (Exception e) {
            log.error("zlexcount method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByLex(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByLex key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByLex method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByLex(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByLex key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByLex method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrevrangeByLex(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrevrangeByLex key=" + key, e);
        } catch (Exception e) {
            log.error("zrevrangeByLex method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<String> sdiff(String... keys) {
        Set<String> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.sdiff(keys);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when sdiff key=" + keys, e);
        } catch (Exception e) {
            log.error("sdiff key error! keys=" + keys, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long del(String key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.del(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when key=" + key, e);
        } catch (Exception e) {
            log.error("del key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        Object result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.eval(script, keys, args);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server", e);
        } catch (Exception e) {
            log.error("eval error! ", e);
        } finally {
            return result;
        }
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        Object result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.eval(script, keys, args);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server", e);
        } catch (Exception e) {
            log.error("eval error! ", e);
        } finally {
            return result;
        }
    }

    @Override
    public String select(int index) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.select(index);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server", e);
        } catch (Exception e) {
            log.error("select error! ", e);
        } finally {
            return result;
        }
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        byte[] result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hget(key, field);
        } catch (JedisConnectionException e) {
            log.error("connect jedis failed.");
        } catch (Exception e) {
            log.error("hget failed. key=" + key + "-" + field, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hset(key, field, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hset key-field=" + key + "-" + field, e);
        } catch (Exception e) {
            log.error("hset key error! key=" + key + "-" + field, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long decr(byte[] key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.decr(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when decr key=" + key, e);
        } catch (Exception e) {
            log.error("decr key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long incr(byte[] key) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.incr(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when incr key=" + key, e);
        } catch (Exception e) {
            log.error("incr method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public byte[] get(byte[] key) {
        byte[] result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.get(key);
        } catch (JedisConnectionException e) {
            log.error("connect jedis failed.");
        } catch (Exception e) {
            log.error("get failed. key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String set(byte[] key, byte[] value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.set(key, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when set key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        String result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.setex(key, seconds, value);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when setex key=" + key, e);
        } catch (Exception e) {
            log.error("set key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        Set<byte[]> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrevrange(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrevrange key=" + key, e);
        } catch (Exception e) {
            log.error("zrevrange method error:", e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long end) {
        Set<byte[]> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrange(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrange key=" + key, e);
        } catch (Exception e) {
            log.error("zrange key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        Set<byte[]> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScore key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScore key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeByScoreWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeByScoreWithScores key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        Set<Tuple> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zrangeWithScores(key, start, end);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zrangeWithScores key=" + key, e);
        } catch (Exception e) {
            log.error("zrangeWithScores key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        Long result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.zadd(key, score, member);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when zadd key=" + key, e);
        } catch (Exception e) {
            log.error("zadd key error! key=" + key, e);
        } finally {
            return result;
        }
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        Map<byte[], byte[]> result = null;
        try (Jedis jedis = getResource()) {
            result = jedis.hgetAll(key);
        } catch (JedisConnectionException e) {
            log.error("can't connect to redis server when hgetAll key=" + key, e);
        } catch (Exception e) {
            log.error("hgetAll key error! key=" + key, e);
        } finally {
            return result;
        }
    }
}
