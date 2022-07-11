package com.homo.service.dirty;

import com.homo.concurrent.schedule.HomoTimeMgr;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.core.common.module.Module;
import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

;

@Log4j2
public class PersistentProcess implements Module {
    @Autowired(required = false)
    DirtyDriver dirtyDriver;
    @Autowired(required = false)
    DirtyProperties dirtyProperties;

    HomoTimeMgr<TaskFun0> homoTimeMgr = HomoTimeMgr.getInstance();

    @Override
    public void init() {
        log.info("landingTask start dirtyProperties {} ",dirtyProperties);
            homoTimeMgr.once(() -> {
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
                    init();
                }
            }, dirtyProperties.getDelayTime());
    }
}
