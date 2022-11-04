package com.homo.core.gate;

import com.homo.core.common.module.ServiceModule;
import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.excption.HomoError;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class GateServerMgr implements ServiceModule {
    @Autowired
    private GateDriver gateDriver;
    @Autowired
    private GateCommonProperties commonProperties;
    public static Map<String,GateServer> gateServerMap = new HashMap<>();

    @Override
    public void init(){
    }

    public GateServer getServer(String serverName){
        return gateServerMap.get(serverName);
    }

    public void startGateServer(String name,int port){
        if (gateServerMap.containsKey(name)){
            throw HomoError.throwError(HomoError.gateError,"listener port repeat error !");
        }
        GateServer gateServer = new GateServerImpl(name,port,gateDriver,commonProperties);
        gateDriver.startGate(gateServer);
        gateServerMap.put(gateServer.getName(),gateServer);
    }
}
