--[[
   获取对应服务连接pod
   input:
    KEYS: uidSvcKey连接状态key
   output: 对应服务的pod,其他情况返回-1
]]
local uidSvcKey = KEYS[1]
local ret = {}
--查找连接
local podIndex = redis.call("GET",uidSvcKey)
if podIndex then
    table.insert(ret,podIndex)
else
    --查找不到连接返回-1
    table.insert(ret,"-1")
end
return ret