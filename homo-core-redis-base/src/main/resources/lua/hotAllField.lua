for i = 2, #ARGV, 2 do
    local value = redis.call("HGET", KEYS[1], ARGV[i])
    if not value then
        redis.call("HSET", KEYS[1], ARGV[i], ARGV[i + 1])
    end
end
redis.call("HSET", KEYS[1], ':cachedAllKey:', 'cachedAllKey')
redis.call("EXPIRE", KEYS[1], ARGV[1])
return 1