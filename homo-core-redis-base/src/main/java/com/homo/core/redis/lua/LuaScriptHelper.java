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


    @Override
    public void init() {
        try {
            lockScript = StreamUtils.copyToString(new ClassPathResource("lua/lock.lua").getInputStream(), StandardCharsets.UTF_8);
            unLockScript = StreamUtils.copyToString(new ClassPathResource("lua/unlock.lua").getInputStream(), StandardCharsets.UTF_8);
            updateKeysExpireScript = StreamUtils.copyToString(new ClassPathResource("lua/updateKeysExpire.lua").getInputStream(), StandardCharsets.UTF_8);
            incrScript = StreamUtils.copyToString(new ClassPathResource("lua/incr.lua").getInputStream(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("LuaScriptHelper init load lua exception: ", e);
        }
    }
}
