package com.core.ability.base.timer;

import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerTask;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象添加定时器能力实现
 */
public class TimeAbility extends AbstractAbility {
    Map<String, HomoTimerTask> timeTaskMap = new HashMap<>();

    public TimeAbility(AbilityEntity abilityEntity) {
        attach(abilityEntity);
    }

    private void clearAll() {
        for (HomoTimerTask timer : timeTaskMap.values()) {
            log.info("unAttach cancel timer type_{}  id_{}  result_{}", getOwner().getType(), getOwner().getId(), timer.justCancel());
        }
        timeTaskMap.clear();
    }

    public boolean cancel(String id) {
        HomoTimerTask timerTask = timeTaskMap.get(id);
        if (timerTask != null) {
            log.info("TimeAbility cancel id {} hashCode {}", id,timerTask.hashCode());
            timerTask.cancel();
        }
        return true;
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        clearAll();
    }

    public HomoTimerTask newTimer(String id, Runnable task, long time, long period, int runCount) {
        HomoTimerTask timerTask = HomoTimerMgr.getInstance().schedule(id, task, time, period, runCount);
        addTimer(timerTask);
        return timerTask;
    }

    private boolean addTimer(HomoTimerTask task) {
        String id = task.id;
        timeTaskMap.put(id, task);
        task.setOnCancelConsumer(tpfTimer1 -> timeTaskMap.remove(id));
        return true;
    }
}
