package com.homo.core.mq.kafka.consumer.woker;


import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

@ToString
public class ConfirmOffsetPair {
    private int partition;
    /**
     * 消费到的offset
     */
    final AtomicLong consumeOffset = new AtomicLong(0);
    /**
     * 被确认的消息offset累计
     */
    final AtomicLong confirmOffset = new AtomicLong(0);

    /**
     * 已提交的消息offset
     */
    final AtomicLong commitOffset = new AtomicLong(0);

    public ConfirmOffsetPair(int partition,long first, long last) {
        this.partition = partition;
        consumeOffset.set(last);
        confirmOffset.set(first - 1); //因为确认一条此值就加1，confirmOffset应该是first的前一个offset位置
        commitOffset.set(confirmOffset.get()); //初始提交的位置设置与confirmSet一致
    }

    public static ConfirmOffsetPair of(int partition,long first,long last){
        return new ConfirmOffsetPair(partition,first,last);
    }

    public long addConsumerCount(int count){
        return consumeOffset.addAndGet(count);
    }
    public long getConsumerCount(){
        return consumeOffset.get();
    }

    public long incrementConfirmCount(){
        return confirmOffset.incrementAndGet();
    }

    public long getConfirmCount(){
        return confirmOffset.get();
    }

    public void setCommitOffset(long newCommitOffset){
        commitOffset.set(newCommitOffset);
    }

    public long getCommitOffset(){
        return commitOffset.get();
    }
}
