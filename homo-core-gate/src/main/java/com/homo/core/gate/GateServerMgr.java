package com.homo.core.gate;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.facade.module.ServiceModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class GateServerMgr implements ServiceModule {
    @Autowired
    public GateDriver gateDriver;
    @Autowired
    public GateCommonProperties commonProperties;
    public static Map<String,GateServer> gateServerMap = new HashMap<>();

    @Override
    public void init(){
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
