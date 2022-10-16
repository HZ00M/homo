--[[
   设置对应服务连接pod
   input:
    KEYS: uidSvcKey连接状态key,uidKey连接状态key
    ARGV:  svcName,podNum,ttl (-1即为永久有效)
   output: 成功返回1
]]
local uidSvcKey = KEYS[1]
local uidKey = KEYS[2]

local svcName = ARGV[1]
local podIndex = ARGV[2]
local ttl = tonumber(ARGV[3])

redis.call("SET",uidSvcKey,podIndex) --设置用户与对应服务的连接pod

if ttl == -1 then   --如果设置永不过期，将 uidSvcKeys连接设置永不过期,uidKey Map里加入这个service的信息
    redis.call("PERSIST",uidSvcKey)
    redis.call("HSET",uidKey,svcName,podIndex)
else                --否则设置连接超时时间,uidKey Map里移除这个service的信息
    redis.call("EXPIRE",uidSvcKey,ttl)
    redis.call("HDEL",uidKey,svcName)
end
return {1}