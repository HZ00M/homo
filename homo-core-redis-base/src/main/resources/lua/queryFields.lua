local result = {}
local needLoadKey = {}
local persistentKey = redis.call("GET", KEYS[2])
if not persistentKey then
    return -1;
else
    for i = 2, #ARGV, 1 do
        local value = redis.call("HGET", KEYS[1], ARGV[i]) --先尝试从reids拿数据，如果有，做完结果返回；如果没有，记录缺失field
        if not value then
            table.insert(needLoadKey, ARGV[i])
        end
        if (value and value ~= "del") then
            table.insert(result, ARGVi)
            table.insert(result, value)
        end
    end
    table.insert(result, "needLoadFlag");  --设置一个缺失标志，当读到该标志，将从数据库捞取数据
    table.insert(result, needLoadKey);
    redis.call("EXPIRE", KEYS[1], ARGV[1])
    return result;
end