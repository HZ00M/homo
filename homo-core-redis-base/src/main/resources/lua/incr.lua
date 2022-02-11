local result = {}
for i = 1, #ARGV, 2 do
    local newValue = redis.call("hincrby", KEYS[1], ARGV[i], ARGV[i + 1])
    table.insert(result, ARGV[i])
    table.insert(result, newValue)
end
return result