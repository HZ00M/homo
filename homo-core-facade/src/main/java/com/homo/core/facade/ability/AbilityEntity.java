package com.homo.core.facade.ability;

import com.homo.core.utils.concurrent.queue.CallQueueProducer;
import com.homo.core.utils.rector.Homo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 能力对象，可以组合各种Ability获得各种能力
 */
public interface AbilityEntity extends Entity, CallQueueProducer {
    Logger logger = LoggerFactory.getLogger(Ability.class);

    <T extends Ability> T getAbility(String abilityName);

    default <T extends Ability> T getAbility(Class<T> ability) {
        return getAbility(ability.getSimpleName());
    }

    void setAbility(String abilityName, Ability ability);

    default void setAbility(Ability ability) {
        setAbility(ability.getClass().getSimpleName(), ability);
    }

    <SELF> Homo<SELF> promiseInit();

    Homo<Void> promiseDestroy();
}
