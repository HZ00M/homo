package com.homo.core.redis.lua;

import com.homo.core.common.module.Module;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Getter
@Slf4j
public class LuaScriptHelper implements Module {

    private String lockScript;

    private String unLockScript;

    private String updateKeysExpireScript;

    private String incrScript;


    private String queryAllFieldsScript;

    private String queryFieldsScript;

    private String updateFieldsScript;

    private String asyncIncrScript;

    private String removeFieldsScript;

    private String hotFieldsScript;

    private String hotAllFieldScript;


    @Override
    public void init() {
        try {
            lockScript = StreamUtils.copyToString(new ClassPathResource("lua/lock.lua").getInputStream(), StandardCharsets.UTF_8);
            unLockScript = StreamUtils.copyToString(new ClassPathResource("lua/unlock.lua").getInputStream(), StandardCharsets.UTF_8);
            updateKeysExpireScript = StreamUtils.copyToString(new ClassPathResource("lua/updateKeysExpire.lua").getInputStream(), StandardCharsets.UTF_8);
            incrScript = StreamUtils.copyToString(new ClassPathResource("lua/incr.lua").getInputStream(), StandardCharsets.UTF_8);
            queryAllFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/queryAllField.lua").getInputStream(), StandardCharsets.UTF_8);
            queryFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/queryFields.lua").getInputStream(), StandardCharsets.UTF_8);
            updateFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/updateFields.lua").getInputStream(), StandardCharsets.UTF_8);
            asyncIncrScript = StreamUtils.copyToString(new ClassPathResource("lua/asyncIncr.lua").getInputStream(), StandardCharsets.UTF_8);
            removeFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/removeFieldsScript.lua").getInputStream(), StandardCharsets.UTF_8);
            hotFieldsScript = StreamUtils.copyToString(new ClassPathResource("lua/hotFields.lua").getInputStream(), StandardCharsets.UTF_8);
            hotAllFieldScript = StreamUtils.copyToString(new ClassPathResource("lua/hotAllField.lua").getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("LuaScriptHelper init load lua exception_", e);
        }
    }


}
