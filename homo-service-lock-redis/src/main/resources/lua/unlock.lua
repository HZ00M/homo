--没有该锁对象，解锁失败
if (redis.call('exists', KEYS[1]) == 0) then
    return 0
end
--坑没有被占用，解锁失败
if (redis.call('hexists',KEYS[1],ARGV[1]) == 0) then
    return 0
end
--坑被占用，减去1，解锁成功
local counter = redis.call('hincrby',KEYS[1],ARGV[1],-1)
if (counter>0) then
    return 1
end
--counter为0，释放坑位
redis.call('del',KEYS[1])
return 1