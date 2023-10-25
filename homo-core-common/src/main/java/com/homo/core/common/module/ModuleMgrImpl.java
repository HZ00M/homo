package com.homo.core.common.module;

import com.homo.core.facade.module.Module;
import com.homo.core.facade.module.ModuleMgr;
import com.homo.core.facade.module.RootModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

import java.util.*;

@Slf4j
public class ModuleMgrImpl implements ModuleMgr, SmartLifecycle {
    @Autowired(required = false)
    RootModule rootModule;
    @Autowired(required = false)
    Map<String, Module> moduleMap;
    List<Module> modules;
    private boolean isRunning = false;

    @Override
    public void start() {
        /**
         * spring容器加载所有bean并初始化之后调用
         */
        try {
            initModules();
            afterInitModules();
            isRunning = true;
        } catch (Exception e) {
            log.error("ModuleMgr init error !", e);
            System.exit(-1);
        }
    }

    @Override
    public List<Module> getModules() {
        return modules;
    }

    @Override
    public Module getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }

    @Override
    public void initModules() {
        modules = new ArrayList(moduleMap.values());
        Collections.sort(modules, Comparator.comparing(Module::getOrder));
        modules.forEach(module -> {
            log.info("module {} init start", module.getClass().getName());
            module.init(rootModule);
            module.init();
            log.info("module {} init end", module.getClass().getName());
        });
    }

    @Override
    public void afterInitModules() {
        moduleMap.values().forEach(Module::afterAllModuleInit);
    }

    @Override
    public String getPodName() {
        return rootModule.getPodName();
    }

    @Override
    public void stop() {
        for (Module module : moduleMap.values()) {
            module.close();
            log.warn("module {} had been stop",module.getClass().getName());
        }
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
