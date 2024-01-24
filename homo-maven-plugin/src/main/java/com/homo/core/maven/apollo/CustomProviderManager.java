package com.homo.core.maven.apollo;

import com.ctrip.framework.foundation.internals.DefaultProviderManager;

public class CustomProviderManager extends DefaultProviderManager {
    private static CustomProviderManager instance;
    public static CustomProviderManager getInstance(){
        return instance;
    }

    public CustomProviderManager(){
        CustomServerProvider serverProvider = new CustomServerProvider();
        serverProvider.initialize();
        register(serverProvider);
        instance = this;
    }
}
