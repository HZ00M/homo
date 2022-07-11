local result = {}
local delFlag = ":delFlag";
local cacheAllKey = redis.call("HGET", KEYS[1], "cachedAllKey")
if not cacheAllKey then
    table.insert(result,"unCachedAllKey")
else
    for i = 2, #ARGV, 1 do
        local value = redis.call("HGET", KEYS[1], ARGV[i])
        if (value and value ~= delFlag) then
            local delField = table.concat({ ARGV[i], delFlag })
            redis.call("HSET", KEYS[1], delField, value) --将数据迁移至另一个field
            redis.call("HSET", KEYS[1], ARGV[i], delFlag)  --将field置为删除标识
        end
    end
    redis.call("EXPIRE", KEYS[1], ARGV[1])
    table.insert(result,"ok")
end
return result
