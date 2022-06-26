package com.homo.core.mysql.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态数据源路由，读写使用主库，只读使用从库
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * 从数据源
     */
    private List<Object> slaveDataSources;

    public DynamicDataSource(Map dataSourceMap) {
        super.setTargetDataSources(dataSourceMap);
        Object maserDataSource = dataSourceMap.get(DBType.MASTER);
        setDefaultTargetDataSource(maserDataSource);
        dataSourceMap.remove(DBType.MASTER);
        slaveDataSources = new ArrayList<>(dataSourceMap.values());

    }

    /**
     * 轮询计数
     */
    private AtomicInteger inr = new AtomicInteger(0);

    @Override
    protected Object determineCurrentLookupKey() {
        Object key = "";
        //主库
        if (DynamicDataSourceHolder.isMaster() || slaveDataSources.isEmpty()) {
            key = DBType.MASTER;
        } else {
            //从库
            key = getSlaveKey();
        }
        log.debug("==> select datasource key [{}]", key);
        return key;
    }

    /**
     * 轮询获取从库
     *
     * @return
     */
    public Object getSlaveKey() {
        if (inr.intValue() == Integer.MAX_VALUE) {
            synchronized (inr) {
                if (inr.intValue() == Integer.MAX_VALUE) {
                    inr = new AtomicInteger(0);
                }
            }
        }
        int idx = inr.getAndIncrement() % slaveDataSources.size();
        return slaveDataSources.get(idx);
    }
}
