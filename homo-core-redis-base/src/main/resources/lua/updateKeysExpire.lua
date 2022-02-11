local result = {}
for i = 2, #ARGV, 2 do
    redis.call("hset",KEYS[1],ARGV[i],ARGV[i+1])
end
redis.call("expire",KEYS[1],ARGV[1])
return result