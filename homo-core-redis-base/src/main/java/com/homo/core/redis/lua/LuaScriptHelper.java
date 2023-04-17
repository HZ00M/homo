package com.homo.core.redis.lua;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Log4j2
public class LuaScriptHelper  {

    public static String lockScript;

    public static String unLockScript;

    public static String updateKeysExpireScript;

    public static String incrScript; 

    public static String queryAllFieldsScript;

    public static String queryFieldsScript;

    public static String updateFieldsScript;

    public static String asyncIncrScript;

    public static String removeFieldsScript;

    public static String hotFieldsScript;

    public static String hotAllFieldScript;

    public static String dirtyDataScript;

    public static String queryExistFieldsScript;

    public static String getDirtyKeyScript;

    public static String statefulSetLink;

    public static String statefulSetLinkIfAbsent;

    public static String statefulGetLink;

    public static String statefulRemoveLink;

    public static String getAllLinkService;

    public static String getServiceState;


    static  {
        try {
            lockScript = StreamUtils.copyToString(new ClassPathResource("lua/lock.lua").getInputStream(), StandardCharsets.UTF_8);
            unLockScript = StreamUtils.copyToString(new ClassPathResource("lua/unlock.lua").getInputStream(), StandardCharsets.UTF_8);
            updateKeysExpireScript = StreamUtils.copyToString(new ClassPathResource("lua/updateKeysExpire.lua").getInputStream(), StandardCharsets.UTF_8);
            incrScript = StreamUtils.copyToString(new ClassPathResource("lua/incr.lua").getInputStream(), StandardCharsets.UTF_8);
            queryAllFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/queryAllFields.lua").getInputStream(), StandardCharsets.UTF_8);
            queryFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/queryFields.lua").getInputStream(), StandardCharsets.UTF_8);
            updateFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/updateFields.lua").getInputStream(), StandardCharsets.UTF_8);
            asyncIncrScript = StreamUtils.copyToString(new ClassPathResource("lua/asyncIncr.lua").getInputStream(), StandardCharsets.UTF_8);
            removeFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/removeFields.lua").getInputStream(), StandardCharsets.UTF_8);
            hotFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/hotFields.lua").getInputStream(), StandardCharsets.UTF_8);
            hotAllFieldScript = StreamUtils.copyToString(new ClassPathResource("lua/hotAllField.lua").getInputStream(), StandardCharsets.UTF_8);
            dirtyDataScript = StreamUtils.copyToString(new ClassPathResource("lua/dirtyData.lua").getInputStream(), StandardCharsets.UTF_8);
            queryExistFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/queryExistFields.lua").getInputStream(), StandardCharsets.UTF_8);
            getDirtyKeyScript = StreamUtils.copyToString(new ClassPathResource("lua/getDirtyKey.lua").getInputStream(), StandardCharsets.UTF_8);
            statefulSetLink = StreamUtils.copyToString(new ClassPathResource("lua/statefulSetLink.lua").getInputStream(), StandardCharsets.UTF_8);
            statefulSetLinkIfAbsent = StreamUtils.copyToString(new ClassPathResource("lua/statefulSetLinkIfAbsent.lua").getInputStream(), StandardCharsets.UTF_8);
            statefulGetLink = StreamUtils.copyToString(new ClassPathResource("lua/statefulGetLink.lua").getInputStream(), StandardCharsets.UTF_8);
            statefulRemoveLink = StreamUtils.copyToString(new ClassPathResource("lua/statefulRemoveLink.lua").getInputStream(), StandardCharsets.UTF_8);
            getAllLinkService = StreamUtils.copyToString(new ClassPathResource("lua/getAllLinkService.lua").getInputStream(), StandardCharsets.UTF_8);
            getServiceState = StreamUtils.copyToString(new ClassPathResource("lua/getServiceState.lua").getInputStream(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("LuaScriptHelper init load lua exception ", e);
        }
    }

}
