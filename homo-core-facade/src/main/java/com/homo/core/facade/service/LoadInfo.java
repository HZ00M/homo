package com.homo.core.facade.service;

import java.util.Comparator;

public class LoadInfo implements Comparator<LoadInfo> {
    public Integer id;
    public Integer load;
    public Long timestamp;
    public Integer state;

    public LoadInfo(Integer id,Integer load, Long timestamp, Integer state) {
        this.id = id;
        this.load = load;
        this.timestamp = timestamp;
        this.state = state;
    }

    private static final String DELIMITER = "-";

    public static String join(Integer load, long timestamp, int state) {
        return String.join(DELIMITER, String.valueOf(load), String.valueOf(timestamp), String.valueOf(state));
    }

    public static LoadInfo build(String key, String value) {
        String[] ss = value.split(DELIMITER);
        return new LoadInfo(Integer.valueOf(key),Integer.valueOf(ss[0]), Long.valueOf(ss[1]), Integer.valueOf(ss[2]));
    }
    @Override
    public int compare(LoadInfo o1, LoadInfo o2) {
        return o1.load.compareTo(o2.load);
    }

}
