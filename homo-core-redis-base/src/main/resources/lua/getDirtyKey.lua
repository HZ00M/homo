local ret = {}
local value = redis.call("EXISTS", KEYS[1])
if value == 1 then
    ret = redis.call("HSCAN", KEYS[1], ARGV[1], 'COUNT', ARGV[2])
end
return ret