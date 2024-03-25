package com.homo.core.utils.module;

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
        modules = new ArrayList<>(moduleMap.values());
        modules.sort(Comparator.comparing(Module::getOrder));
        for (Module module : modules) {
            log.info("module {} initModules start", module.getClass().getName());
            module.moduleInit();
            log.info("module {} initModules end", module.getClass().getName());
        }
    }

    @Override
    public void afterInitModules() {
        modules = new ArrayList<>(moduleMap.values());
        modules.sort(Comparator.comparing(Module::getOrder));
        for (Module module : modules) {
            log.info("module {} afterInitModules start", module.getClass().getName());
            module.afterAllModuleInit();
            log.info("module {} afterInitModules end", module.getClass().getName());
        }
    }

    @Override
    public String getPodName() {
        return rootModule.getPodName();
    }

    @Override
    public void stop() {
        for (Module module : moduleMap.values()) {
            try {
                module.beforeClose();
                log.warn("module {} had been stop", module.getClass().getName());
            } catch (Exception e) {
                log.error("ModuleMgr close error !", e);
            }
        }
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
