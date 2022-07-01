package com.homo.service.dirty;

import com.homo.concurrent.schedule.HomoTimeMgr;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.core.common.module.Module;
import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
public class PersistentProcess implements Module {
    @Autowired(required = false)
    DirtyDriver dirtyDriver;
    @Autowired(required = false)
    DirtyProperties dirtyProperties;

    HomoTimeMgr<TaskFun0> homoTimeMgr = HomoTimeMgr.getInstance();

    @Override
    public void init() {
        log.info("landingTask start dirtyProperties {} ",dirtyProperties);
        try {
            String dirtyName = dirtyDriver.chooseDirtyMap();
            homoTimeMgr.once(() -> {
                try {
                    Boolean lock = dirtyDriver.lockDirtyMap(dirtyName);
                    if (lock) {
                        String dirtySaving = dirtyDriver.snapShot(dirtyName);
                        long startTime = System.currentTimeMillis();
                        log.info("landingTask landing start dirtyName {} dirtySaving {} startTime", dirtyName, dirtySaving);
                        boolean landingResult = dirtyDriver.landing(dirtyName, dirtySaving);
                        long endTimeTime = System.currentTimeMillis();
                        log.info("landingTask landing finish dirtyName {} dirtySaving {} result {} spentTime {}", dirtyName, dirtySaving, landingResult,endTimeTime - startTime);
                    }
                } catch (Exception e) {
                    log.error("landingTask error", e);
                } finally {
                    init();
                }
            }, dirtyProperties.getDelayTime());
        } catch (InterruptedException e) {
            log.error("landingTask error",e);
         }
    }
}
