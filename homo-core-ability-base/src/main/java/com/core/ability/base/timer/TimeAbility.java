package com.core.ability.base.timer;

import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerTask;
import org.apache.logging.log4j.core.util.UuidUtil;

import java.util.HashMap;
import java.util.Map;

public class TimeAbility extends AbstractAbility {
    Map<String, HomoTimerTask> timeTaskMap = new HashMap<>();
    public TimeAbility(AbilityEntity abilityEntity) {
        attach(abilityEntity);
    }

    private void clearAll(){
        for (HomoTimerTask timer : timeTaskMap.values()) {
            log.info("unAttach cancel timer type_{}  id_{}  result_{}",  getOwner().getType(), getOwner().getId(), timer.justCancel());
        }
        timeTaskMap.clear();
    }


    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        clearAll();
    }

    public String newTimer(Runnable task, long time, long period, int runCount){
        return addTimer(HomoTimerMgr.getInstance().schedule(task, time, period, runCount));
    }

    private String addTimer(HomoTimerTask task){
        String uuid = UuidUtil.getTimeBasedUuid().toString();
        timeTaskMap.put(uuid, task);
        task.setOnCancelConsumer(tpfTimer1 -> timeTaskMap.remove(uuid));
        return uuid;
    }
}
