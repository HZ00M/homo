--如果查询persistenceKey不存在，说明数据库不存在该key数据，直接返回
local persistenceKey = redis.call("GET", KEYS[2])
if not persistenceKey then
    return -1   --数据库里没有数据
end

--如果存在cachedKey,说明在内存中这个key对应的hash表是最新的,否则内存中的数据不是最新的，需要从数据库中加载冷数据
--如果内存中有些数据的value值是del，说明这个数据被删除了

local cachedKey = redis.call("HGET", KEYS[1], ":cachedKey:")
if not cachedKey then
    return 0    --内存中的数据不是最新的
else
    local ret = {}
    local del = "del"
    local value = redis.call("HGETALL", KEYS[1])
    for i = 1, #value, 2 do
        if (value[i + 1] ~= del and value[i] ~= "cachedKey" and not string.find(value[i], del)) then
            table.insert(ret, value[i])
            table.insert(ret, value[i + 1])
        end
    end
    redis.call("EXPIRE",KEYS[1],ARGV[1])
    return ret;
end
