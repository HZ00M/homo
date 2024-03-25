package com.homo.service.dirty;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.utils.module.Module;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

;

@Slf4j
public class PersistentProcess implements Module {
    @Autowired(required = false)
    DirtyDriver dirtyDriver;
    @Autowired(required = false)
    DirtyProperties dirtyProperties;

    HomoTimerMgr homoTimerMgr = HomoTimerMgr.getInstance();

    @Override
    public void moduleInit() {
        log.info("landingTask start dirtyProperties {} ",dirtyProperties);
            homoTimerMgr.once("dirtyLanding",() -> {
                String dirtyName = dirtyDriver.chooseDirtyMap();
                try {
                        String dirtySaving = dirtyDriver.snapShot(dirtyName);
                        long startTime = System.currentTimeMillis();
                        log.info("landingTask landing start dirtyName {} dirtySaving {} startTime", dirtyName, dirtySaving);
                        boolean landingResult = dirtyDriver.landing(dirtyName, dirtySaving);
                        long endTimeTime = System.currentTimeMillis();
                        log.info("landingTask landing finish dirtyName {} dirtySaving {} result {} spentTime {}", dirtyName, dirtySaving, landingResult,endTimeTime - startTime);
                } catch (Exception e) {
                    log.error("landingTask landing  dirtyName {} error", dirtyName,e);
                } finally {
                    log.info("landingTask unlock dirtyName start{} ",dirtyName);
                    dirtyDriver.unlockDirtyMap(dirtyName);
                    log.info("landingTask unlock dirtyName end{} ",dirtyName);
                    moduleInit();
                }
            }, dirtyProperties.getDelayTimeMillSecond());
    }
}
