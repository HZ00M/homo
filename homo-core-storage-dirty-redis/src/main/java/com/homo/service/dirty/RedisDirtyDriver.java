package com.homo.service.dirty;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.lock.LockDriver;
import com.homo.core.facade.storege.dirty.Dirty;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import com.homo.core.facade.storege.dirty.DirtyHelper;
import com.homo.core.facade.storege.landing.LandingDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
public class RedisDirtyDriver implements DirtyDriver {

    @Autowired(required = false)
    @Qualifier("homoRedisPool")
    private HomoAsyncRedisPool redisPool;

    @Autowired(required = false)
    private DirtyProperties dirtyProperties;

    @Autowired(required = false)
    private LandingDriver LandingDriver;

    @Autowired(required = false)
    private LockDriver lockDriver;

    public static int seed = 0;

    public int snapShotNum;


    @Override
    public Homo<Long> dirtyUpdate(Dirty dirty) {
        String dirtyKey = DirtyHelper.chooseDirtyMap(dirty.key());
        Mono<Long> update = redisPool.hsetAsyncReactive(dirtyKey, dirty.dirtyMap());
        return Homo.warp(update);
    }

    @Override
    public String chooseDirtyMap() {
        // 尝试找到一个没有锁的DirtyMsp
        for (; ; ) {
            int key = seed % dirtyProperties.getTableNum();
            String currentDirtyName = DirtyHelper.chooseDirtyMap(key);
            // 尝试去锁这个表
            log.info("try to lock {} {}", currentDirtyName, seed);
            seed++;
            if (lockDirtyMap(currentDirtyName)) {
                // 如果锁到了就是自己的, 返回出去
                return currentDirtyName;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("chooseDirtyMap error", e);
            }
        }
    }

    @Override
    public Boolean lockDirtyMap(String dirtyName) {
        return lockDriver.lock(DirtyHelper.APP_ID, DirtyHelper.REGION_ID, DirtyHelper.LOGIC_ID, DirtyHelper.OWNER_ID, dirtyName, dirtyProperties.getLockExpireTime());
    }

    @Override
    public Boolean unlockDirtyMap(String dirtyName) {
        return lockDriver.unlock(DirtyHelper.APP_ID, DirtyHelper.REGION_ID, DirtyHelper.LOGIC_ID, DirtyHelper.OWNER_ID, dirtyName);
    }

    @Override
    public boolean landing(String dirtyTableName, String dirtySaving) {
        log.info("-------------landing dirtySaving {} begin----------------", dirtySaving);
        String index = "0";
        do {
            log.info("landing batch dirtySaving {} begin----------------", dirtySaving);
            // 一次取一批数据吗？
            List iterationList = getDirtyList(dirtySaving, index, dirtyProperties.getBatchNum());
            if (iterationList.isEmpty()) {
                log.info("no data needs to land");
                return true;
            }
            // 取出这一批要落地的数据
            List<String> dirtyList = (List<String>) iterationList.get(1);
            boolean batchLandingResult = false;
            batchLandingResult = LandingDriver.batchLanding(dirtyTableName, dirtyList);
            log.info("batchUpdate result is {}", batchLandingResult);
            if (!batchLandingResult) {
                // 保持失败了，开始单个保存
                log.info("single update begin");
                boolean singleLandingResult = LandingDriver.singleLanding(dirtyList, dirtyTableName);
                log.info("single update end singleLandingResult {}", singleLandingResult);
            }
            index = (String) iterationList.get(0);
        } while (!"0".equals(index));
        String dirtySavingDone = dirtySaving + ":done";
        redisPool.rename(dirtySaving, dirtySavingDone);
        redisPool.expire(dirtySavingDone, 1);
        log.info("-------------landing  dirtySaving is {} end----------------", dirtySaving);
        return true;
    }


    @Override
    public List getDirtyList(String key, String index, String count) {
        String dirtyKeyScript = LuaScriptHelper.getDirtyKeyScript;
        String[] keyList = new String[1];
        keyList[0] = key;
        String[] args = new String[2];
        args[0] = index;
        args[1] = count;
        //同步的方法
        Object object = redisPool.eval(dirtyKeyScript, keyList, args);
        List list = new ArrayList<>();
        if (!Collections.emptyList().equals(object)) {
            list = (List) object;
        }
        return list;
    }


    @Override
    public String snapShot(String dirtyName) {
        // 判断是不是第一次落地
        boolean down = snapShotNum == 0;
        log.info("begin landing down is {}", down);

        // 创建新的名字
        String dirtySaving = DirtyHelper.getSnapShotName(dirtyName);

        //如果存在错误落地的key，追加在这次落地中， 这里可能有问题
        String dirtyError = DirtyHelper.getErrorName(dirtyName);
        if (redisPool.exists(dirtyError)) {
            // 有错误的数据
            // 获取所有错误数据
            Map<String, String> errorKeys = redisPool.hgetAll(dirtyError);
            // 这里只打key
            log.info("errorKeys [{}]", errorKeys);
            for (String key : errorKeys.keySet()) {
                log.warn("error key is {}", key);
                // 写入带保存脏表，为什么要设删除标志？
                redisPool.hset(dirtySaving, key, DirtyHelper.DEL_SUFFIX);
            }
            // 删除错误数据
            redisPool.del(dirtyError);
        }
        //如果不是刚启动, 改脏表名字，脏表存在，脏表saving不存在的情况才改名字
        if (!down // 不是刚启动
                && redisPool.exists(dirtyName) // 脏表存在
                && !redisPool.exists(dirtySaving)) // 待保存的表不存在
        {
            // 刚启动的时候就没有改脏表的名字
            redisPool.rename(dirtyName, dirtySaving);
        }
        snapShotNum++;
        // 返回保存的表
        return dirtySaving;
    }


}
