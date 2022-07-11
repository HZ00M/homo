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
        if (value and value ~= ":delFlag") then
            table.insert(result, ARGV[i])
            table.insert(result, value)
        end
    end
    table.insert(result, "missNum");  --设置一个缺失数量key，将从数据库捞取数据
    table.insert(result, needLoadKey);  --缺少key列表
    redis.call("EXPIRE", KEYS[1], ARGV[1])
    return result;
end