local result = {}
for i = 2, #ARGV, 2 do
    redis.call("HSET",KEYS[1],ARGV[i],ARGV[i+1])
end
redis.call("SET",KEYS[2],"1")
redis.call("EXPIRE",KEYS[1],ARGV[1])
return result