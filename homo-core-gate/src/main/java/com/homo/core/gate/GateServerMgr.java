package com.homo.core.gate;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GateServerMgr implements ServiceModule {
    @Autowired
    public GateDriver gateDriver;
    @Autowired
    public GateCommonProperties commonProperties;
    public static Map<String,GateServer> gateServerMap = new HashMap<>();

    @Override
    public void moduleInit(){
    }

    public GateServer getServer(String serverName){
        return gateServerMap.get(serverName);
    }

    public GateDriver getGateDriver(){
        return gateDriver;
    }

    public void startGateServer(GateServer gateServer){
        if (gateServerMap.containsKey(gateServer.getName())){
            throw HomoError.throwError(HomoError.gateError,"listener port repeat error !");
        }
        gateServer.setDriver(gateDriver);
        gateDriver.startGate(gateServer);
        gateServerMap.put(gateServer.getName(),gateServer);
    }

    public void startGateServer(String name,int port){
        GateServer gateServer = new DefaultGateServer(name,port);
        this.startGateServer(gateServer);
    }

}
