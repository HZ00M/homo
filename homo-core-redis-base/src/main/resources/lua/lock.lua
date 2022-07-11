--不存在kye就占坑
if redis.call('exists',KEYS[1],ARGV[2])==0 then
    redis.call('hset',KEYS[1],ARGV[2],1)
    redis.call('expire',KEYS[1],ARGV[1])
    return 1
end;
--存在key就检查是否被占用
if redis.call('hexists',KEYS[1],ARGV[2])==0 then
    redis.call('hincrby',KEYS[1],ARGV[2],1)
    redis.call('expire',KEYS[1],ARGV[1])
    return 1
end;
--支持重入
if redis.call('hexists',KEYS[1],ARGV[2])==1 then
    redis.call('hincrby',KEYS[1],ARGV[2],1)
    redis.call('expire',KEYS[1],ARGV[1])
    return 1
end;
return 0

