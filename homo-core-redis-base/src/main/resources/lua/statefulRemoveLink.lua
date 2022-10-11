--[[
   去除对应服务连接pod
   input:
    KEYS: uidSvcKey连接状态key,uidKey连接状态key
    ARGV:  服务名,ttl (-1即为立即删除)
   output: 成功返回1
]]
local uidSvcKey = KEYS[1]
local uidKey = KEYS[2]

local svcName = ARGV[1]
local setTtl = tonumber(ARGV[2])

--如果setTtl -1 则立刻删除
if setTtl == -1 then
    redis.call("DEL",uidSvcKey)
else
    redis.call("EXPIRE",uidSvcKey,setTtl)
end
redis.call("HDEL",uidKey,svcName)
local keyNum = redis.call("HLEN",uidKey)
--如果hash表中没字段删除hash表
if keyNum == 0 then
    redis.call("DEL",uidKey)
end
return {1}