package com.homo.core.persisten.handler;

import com.homo.core.common.pojo.DataObject;
import com.homo.core.utils.callback.CallBack;

import java.util.List;

/**
 * 缓存mysql数据到redis处理器
 */
public class LoadDataHandler {


    public void hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey, CallBack<Boolean> callBack){

    }

    public void hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields, CallBack<List<DataObject>> callBack){

    }

    private void cache(byte[] script, String appId, String regionId, Integer logicType, String ownerId, String redisKey, List<DataObject> list) {

    }
}
