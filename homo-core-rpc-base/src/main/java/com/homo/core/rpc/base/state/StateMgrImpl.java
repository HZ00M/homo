package com.homo.core.rpc.base.state;

import com.homo.core.common.module.Module;
import com.homo.core.facade.service.StateMgr;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class StateMgrImpl implements StateMgr, Module {

    @Override
    public void init(){

    }

    @Override
    public boolean isStateful() {
        return false;
    }

    @Override
    public void setState(Integer state) {

    }

    @Override
    public Integer getState(Integer state) {
        return null;
    }

    @Override
    public String getPodName() {
        return null;
    }

    @Override
    public Integer getPodIndex() {
        return null;
    }

    @Override
    public Homo<Boolean> setLinkedPod(String uid, String serviceName, Integer podIndex) {
        return null;
    }

    @Override
    public Homo<Integer> getLinkedPod(String uid, String serviceName) {
        return null;
    }

    @Override
    public Homo<Integer> getLinkedPodNoCache(String uid, String serviceName) {
        return null;
    }

    @Override
    public Homo<Integer> computeLinkedPodIfAbsent(String uid, String serviceName, Boolean persist) {
        return null;
    }

    @Override
    public Homo<Boolean> removeLinkedPod(String uid, String serviceName, Boolean immediately) {
        return null;
    }

    @Override
    public Homo<Map<String, Integer>> getAllLinkInfo(String uid) {
        return null;
    }

    @Override
    public Homo<Integer> getBestPod(String serviceName) {
        return null;
    }

    @Override
    public Homo<Map<String, Integer>> getAllStateInfo(String serviceName) {
        return null;
    }
}
