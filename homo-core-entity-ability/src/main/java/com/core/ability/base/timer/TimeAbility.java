package com.core.ability.base.timer;

import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerTask;

import java.util.*;

/**
 * 对象添加定时器能力实现
 */
public class TimeAbility extends AbstractAbility {
    Map<String, HomoTimerTask> timeTaskMap = new HashMap<>();

    public TimeAbility(AbilityEntity abilityEntity) {
        attach(abilityEntity);
    }

    private void clearAll() {
        List<HomoTimerTask> taskList = new ArrayList<>(timeTaskMap.values());
        for (HomoTimerTask timer : taskList) {
            log.info("clearAll cancel done type {} id {} timeId {} result {}", getOwner().getType(), getOwner().getId(),timer.id, timer.cancel());
        }
        timeTaskMap.clear();
    }

    public boolean cancel(String id) {
        HomoTimerTask timerTask = timeTaskMap.get(id);
        if (timerTask != null) {
            log.info("TimeAbility cancel id {} hashCode {}", id, timerTask.hashCode());
            timerTask.cancel();
        }
        return true;
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        clearAll();
    }


    public HomoTimerTask schedule(String id, Runnable task, long delayMilliSecond, long period) {
        HomoTimerTask timerTask = HomoTimerMgr.getInstance().schedule(id, task, delayMilliSecond, period, HomoTimerMgr.UNLESS_TIMES);
        addTimer(timerTask);
        return timerTask;
    }

    public HomoTimerTask newTimer(String id, Runnable task, long period, int runCount) {
        HomoTimerTask timerTask = HomoTimerMgr.getInstance().schedule(id, task, 0, period, runCount);
        addTimer(timerTask);
        return timerTask;
    }

    public HomoTimerTask newTimer(String id, Runnable task, long delayMilliSecond, long period, int runCount) {
        HomoTimerTask timerTask = HomoTimerMgr.getInstance().schedule(id, task, delayMilliSecond, period, runCount);
        addTimer(timerTask);
        return timerTask;
    }

    private boolean addTimer(HomoTimerTask task) {
        String uuid = UUID.randomUUID().toString();
        timeTaskMap.put(uuid, task);
        task.setOnCancelConsumer(tpfTimer1 -> timeTaskMap.remove(uuid));
        return true;
    }
}
