package com.core.ability.base.config;

import com.core.ability.base.EntityProxyFactory;
import com.core.ability.base.StorageEntityMgr;
import com.core.ability.base.call.CallSystem;
import com.homo.core.facade.ability.ICallSystem;
import com.core.ability.base.notify.NotifySystem;
import com.core.ability.base.storage.StorageSystem;
import com.core.ability.base.timer.TimeSystem;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.IEntityService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import java.util.Set;

@Configuration
@Log4j2
@Import(AbilityProperties.class)
public class AbilityAutoConfiguration {
    @Autowired
    private AbilityProperties abilityProperties;
//    @Bean("entityService")
//    @DependsOn("callSystem")
//    public IEntityService entityService(){
//        log.info("register bean entityService");
//        return new EntityService();
//    }
    @Bean("entityProxyFactory")
    public EntityProxyFactory serviceStateMgr(){
        log.info("register bean serviceStateMgr");
        return new EntityProxyFactory();
    }
    @Bean("callSystem")
    public ICallSystem callSystem(){
        log.info("register bean callSystem");
        return new CallSystem();
    }

    @Bean("notifySystem")
    public NotifySystem notifySystem(){
        log.info("register bean notifySystem");
        return new NotifySystem();
    }

    @Bean("storageSystem")
    public StorageSystem storageSystem(){
        log.info("register bean storageSystem");
        return new StorageSystem();
    }

    @Bean("timeSystem")
    public TimeSystem timeSystem(){
        log.info("register bean timeSystem");
        return new TimeSystem();
    }

    @Bean("entityMgr")
    public StorageEntityMgr storageEntityMgr(Set<? extends AbilitySystem> abilitySystems){
        log.info("register bean entityMgr");
        return new StorageEntityMgr(abilitySystems,abilityProperties);
    }


}
