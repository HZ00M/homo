local result = {}
local delFlag = ":delFlag";
local persistentKey = redis.call("GET", KEYS[2])
local cachedAllKey = redis.call("HGET", KEYS[1], "cachedAllKey")
if persistentKey and not cachedAllKey then
    table.insert(result, "unCachedAllKey")
else
    for i = 2, #ARGV, 2 do
        local isDel = redis.call("HGET", KEYS[1], ARGV[i])
        if isDel == delFlag then
            redis.call("HDEL", KEYS[1], ARGV[i])
        end
        local value = redis.call("HINCRBY", KEYS[1], ARGV[i], ARGV[i + 1])
        table.insert(result, ARGV[i])
        table.insert(result, value)
    end
    redis.call("SET", KEYS[2], '1')
    redis.call("EXPIRE", KEYS[1], ARGV[1])
end
return result
