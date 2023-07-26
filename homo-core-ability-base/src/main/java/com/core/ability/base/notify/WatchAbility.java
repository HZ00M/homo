package com.core.ability.base.notify;

import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.ListenerAble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对象监听事件能力
 */
public class WatchAbility extends AbstractAbility {
    Map<String, List<ListenerAble>> listenerMap = new HashMap<>();

    public WatchAbility(AbilityEntity tpfObject) {
        attach(tpfObject);
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {

    }

    public void watch(String event, ListenerAble listenerAble){
        listenerMap.computeIfAbsent(event, e-> new ArrayList<>()).add(listenerAble);
    }

    public void notify(String event,Object... messages){
        listenerMap.computeIfPresent(event,(k,list)->{
            for (ListenerAble listenerAble : list) {
                listenerAble.notify(event,messages);
            }
            return list;
        });
    }
}
