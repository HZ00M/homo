package com.homo.service.state.driver.redis;

public class LoadTimestamp {
    public Integer load;
    public Long timestamp;
    public LoadTimestamp(Integer load, Long timestamp){
        this.load = load;
        this.timestamp = timestamp;
    }
    private static final String DELIMITER = "-";
    public static String join(Integer state,long timestamp){
        return String.join(DELIMITER,String.valueOf(state),String.valueOf(timestamp));
    }
    public static LoadTimestamp split(String stateTimestamp){
        String[] ss = stateTimestamp.split(DELIMITER);
        return new LoadTimestamp(Integer.valueOf(ss[0]),Long.valueOf(ss[1]));
    }
}
