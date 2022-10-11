--[[
  设置对应服务连接pod(如果之前没有设置的话)
  input:
  KEYS: uidSvcKey连接状态key,uidKey连接状态key
  ARGV:  svcName,podNum,ttl (-1即为永久有效)
  output: 实际的podNumber
]]
-- 返回服务的pod和状态值  服务状态格式: podId-podState
local uidSvcKey = KEYS[1]
local uidKey = KEYS[2]

local svcName = ARGV[1]
local podIndex = ARGV[2]
local setTtl = tonumber(ARGV[3])

local existPodNum = redis.call("GET",uidSvcKey)
if existPodNum then
    local ttl = redis.call("TTL",uidSvcKey)
    if ttl ~= -1 then   --如果存在连接信息且连接有过期时间时，更新过期时间
        redis.call("EXPIRE",uidSvcKey,setTtl)
    end

    return {existPodNum}
else
    --没有与该服务的连接 ，则建立指定连接
    redis.call("SET",uidSvcKey,podIndex)
    if setTtl == -1 then    --如果设置为持久化，则存入用户uidKey map中
        redis.call("HSET",uidKey,svcName,podIndex)
    else
        redis.call("EXPIRE",uidSvcKey,setTtl)
    end
    return {podIndex}
end