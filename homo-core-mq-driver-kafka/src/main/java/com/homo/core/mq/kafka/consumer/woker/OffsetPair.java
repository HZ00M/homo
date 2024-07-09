package com.homo.core.mq.kafka.consumer.woker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@Data
@ToString
public class OffsetPair implements Serializable,Comparable<OffsetPair> {
    private int partition;
    private long first;
    private long last;

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }

    @Override
    public int compareTo(OffsetPair o) {
        return Long.compare(last,o.last);
    }

    public static OffsetPair of(int partition,long first,long last){
        return new OffsetPair(partition,first,last);
    }
}
