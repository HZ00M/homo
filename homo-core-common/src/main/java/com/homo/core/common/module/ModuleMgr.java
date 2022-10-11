package com.homo.core.common.module;


import java.util.List;

/**
 * 提供组件统一管理
 */
public interface ModuleMgr {
    /**
     * 获取所有组件
     * @return
     */
    List<Module> getModules();

    /**
     * 获取指定组件
     * @param moduleName
     * @return
     */
    Module getModule(String moduleName);

    /**
     * 初始化所有组件
     */
    void initModules();

    /**
     * 所有组件初始化完后调用
     */
    void afterInitModules();

    /**
     * 获取k8s上pod的名字
     * @return
     */
    public String getPodName();
}
