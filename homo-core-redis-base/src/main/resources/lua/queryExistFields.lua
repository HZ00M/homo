local ret = {}
local delFlag = "delFlag";
for i = 1, #ARGV, 1 do
    local value = redis.call("HGET", KEYS[1], ARGV[i])
    if(value and value ~= delFlag) then
        table.insert(ret, ARGV[i])
        table.insert(ret, value)
    end
end
return ret