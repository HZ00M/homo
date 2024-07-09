package com.homo.core.mq.kafka.consumer.woker;

import com.homo.core.facade.mq.consumer.ConsumerCallback;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RebalanceInProgressException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaConsumerConfirmWorker extends ConsumerWorker {
    static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfirmWorker.class);
    static final Marker marker = MarkerFactory.getMarker("ConsumeWorker");
    /**
     * 每10秒上报一次数据。
     */
    public static final int COMMIT_OFFSET_INTERVAL = 10;
    /**
     * 每60秒打印一次信息。
     */
    public static final int PRINT_INFO_INTERVAL = 60;
    final AtomicLong confirmCount = new AtomicLong(0);
    final AtomicInteger partitionRevokedCount = new AtomicInteger(0);
    final AtomicInteger partitionAssignedCount = new AtomicInteger(0);
    /**
     * 消费者poll的时间参数
     */
    private final long pollWaitMs;
    private final Duration pollWaitDuration;
    private final int maxPollRecords;

    /**
     * 最近一次打印提交消息的时间
     */
    private Instant lastPrintTime;
    /**
     * 最近一次提交offset时间
     */
    private Instant lastOffsetCommitTime;
    /**
     * 此topic所有partition的offset缓存map。保存这一批消息的开头与结束offset
     */
    private final Map<TopicPartition, ConfirmOffsetPair> offsets = new ConcurrentHashMap<>(64);
    /**
     * Kafka消费者匀衡监听者
     */
    private ConsumerRebalanceListener rebalanceListener;

    public KafkaConsumerConfirmWorker(String name,KafkaConsumer<String, Bytes> consumer, String topic, ReceiverSink<byte[]> sink, long pollWaitMs, int maxPollRecords) {
        super(name,consumer, topic, sink);
        this.pollWaitMs = pollWaitMs;
        this.pollWaitDuration = Duration.ofMillis(pollWaitMs);
        this.maxPollRecords = maxPollRecords;
    }

    public void init() {
        this.lastPrintTime = Instant.now();
        this.lastOffsetCommitTime = Instant.now();
        this.rebalanceListener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                partitionRevokedCount.incrementAndGet();
                if (partitions != null) {
                    log.warn(marker, "{} partitions revoked topic {} partitions {} ",name, topic, partitions);
                }
                try {
                    //当发生rebalance时，提交全部offset，不管offset是否有确认成功
                } catch (Exception e) {
                    log.error(marker, "{} commitOffsets after partition revoked exception ",name, e);
                }
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                partitionAssignedCount.incrementAndGet();
                if (partitions != null) {
                    log.warn(marker, "{} partitions assigned topic {} partitions {} ",name, topic, partitions);
                }
            }
        };
        this.consumer.subscribe(Collections.singleton(topic), rebalanceListener);
    }

    @Override
    public void process() {
        long consumeCount = 0;
        final AtomicLong confirmCount = new AtomicLong(0);
        try {
            init();
            while (status != Status.TERMINATED) {
                try {
                    ConsumerRecords<String, Bytes> records = receive();
                    if (records.isEmpty()) {
                        maybeCommitOffsets();
                        continue;
                    }
                    log.debug(marker,"{} receive size receive topic {} size {} partitions {}", name,topic, records.count(), records.partitions());
                    consumeCount = consumeCount + records.count();
                    for (TopicPartition partition : records.partitions()) {
                        List<ConsumerRecord<String, Bytes>> partitionRecords = records.records(partition);
                        if (!partitionRecords.isEmpty()) {
                            for (ConsumerRecord<String, Bytes> record : partitionRecords) {
                                Bytes value = record.value();
                                byte[] bytes = value.get();
                                sink.onSink(topic,bytes, new ConsumerCallback() {
                                    final AtomicBoolean confirmed = new AtomicBoolean(false);
                                    //这么实现在所有sink中只需确认一次
                                    @Override
                                    public void confirm() {
                                        if (confirmed.get()) {
                                            String msg = String.format("the same record cannot be confirmed more than once, please check the code: topic=%s,partition=%s,offset=%s", record.topic(), record.partition(), record.offset());
                                            log.warn(marker, msg);
                                            return;
//                                            throw new RuntimeException(msg);
                                        }
                                        confirmed.set(true);
                                        confirmCount.incrementAndGet();
                                        ConfirmOffsetPair confirmOffsetPair = offsets.get(partition);
                                        if (confirmOffsetPair != null) {
                                            confirmOffsetPair.incrementConfirmCount();
                                        }
                                    }
                                });
                            }
                        }
                    }

                    //如果接收的消息总数-被确认的消息位数>maxPollRecords，则不要再继续消费
                    long remainCount;
                    int retry = 0;
                    while ((remainCount = consumeCount - confirmCount.get()) > maxPollRecords) {
                        // 如果超过maxPollRecords的消息没有被确认，用10毫秒等待，
                        TimeUnit.MILLISECONDS.sleep(pollWaitMs);
                        retry++;
                        if (retry % 2000 == 0) {
                            log.error(marker, "{} total number of currently received records: {}, total number of confirmed messages: {}. More than {} messages have not been confirmed within 2 seconds ",
                                    name,consumeCount, confirmCount, remainCount);
                        }
                    }
                    //尝试提交offset。每10秒提交一次offset。不应该放到回调中去确认，因为回调是在业务线程中
                    maybeCommitOffsets();
                } catch (RebalanceInProgressException e) {
                    /* auther:  liulang
                     *  date: 2024/02/23
                     * RebalanceInProgressException 错误，这通常意味着Kafka消费者在自动分区分配过程中正在进行重新平衡，此时尝试拉取数据或提交偏移量会失败。
                     *  正确的做法是不要退出当前线程，而是继续调用 poll() 方法。通过持续调用 poll()，消费者会参与到重新平衡的过程中，并最终完成分区的重新分配。
                     *  一旦重新平衡完成，你就可以再次尝试提交偏移量。
                     *  参与资料来源于Kafka的源代码：{@link org.apache.kafka.clients.consumer.internals.ConsumerCoordinator}
                     * 中第1282行源码
                     *
                     *    // if the client knows it is already rebalancing, we can use RebalanceInProgressException instead of
                     *    // CommitFailedException to indicate this is not a fatal error
                     *        return RequestFuture.failure(new RebalanceInProgressException("Offset commit cannot be completed since the " +
                     *                "consumer is undergoing a rebalance for auto partition assignment. You can try completing the rebalance " +
                     *                "by calling poll() and then retry the operation."));
                     */
                    log.error(marker, "{} process receive error rebalance in progress topic {} e", name,topic, e);
                    //暂停1秒，等等Kafka重新平衡完成
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        } catch (WakeupException e) {
            log.warn(marker, "kafka consumer {} throw wakeup exception, ready to close topic {}",name, topic);
        } catch (Throwable throwable) {
            log.warn(marker, "kafka consumer {} throw  topic {}", name,topic, throwable);
        } finally {
            try {
                //提交剩余全部offset，就算没有确认过的消息offset也要提交
                commitOffsets(false);
                //关闭消费者以及业务处理
                consumer.close();
            } catch (Throwable throwable) {
                log.warn(marker, "kafka consumer {} close throwable topic {}", name,topic, throwable);
            }
        }
    }

    public ConsumerRecords<String, Bytes> receive() {
        ConsumerRecords<String, Bytes> records = consumer.poll(pollWaitDuration);
        if (!records.isEmpty()) {
            synchronized (offsets) {
                //如果有消费到消息，更新offsets数据为各分区第一条和最后一条消息的offset
                for (TopicPartition topicPartition : records.partitions()) {
                    List<ConsumerRecord<String, Bytes>> partitionRecords = records.records(topicPartition);
                    if (!partitionRecords.isEmpty()) {
                        ConfirmOffsetPair confirmOffsetPair = offsets.get(topicPartition);
                        if (confirmOffsetPair != null) {
                            confirmOffsetPair.addConsumerCount(partitionRecords.size());
                        } else {
                            long beginOffset = partitionRecords.get(0).offset();
                            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                            int partition = topicPartition.partition();
                            offsets.put(topicPartition, ConfirmOffsetPair.of(partition,beginOffset, lastOffset));
                        }
                    }
                }
            }
        }
        return records;
    }

    public void maybeCommitOffsets() throws Exception {
        if (Duration.between(lastOffsetCommitTime, Instant.now()).getSeconds() > COMMIT_OFFSET_INTERVAL) {
            commitOffsets(true);
            lastOffsetCommitTime = Instant.now();
        }
    }

    private synchronized void commitOffsets(boolean onlyConfirm) throws Exception {
        int retry = 0;
        boolean needRetry = true;
        while (needRetry) {
            try {
                doCommitOffset(onlyConfirm);
                needRetry = false;
                lastOffsetCommitTime = Instant.now();
            } catch (WakeupException e) {
                // we only call wakeup() once to close the consumer,
                // so if we catch it in commit we can safely retry
                // and re-throw to break the loop
                commitOffsets(onlyConfirm);
                throw e;
            } catch (TimeoutException e) {
                try {
                    Map<String, List<PartitionInfo>> visibleTopics = consumer.listTopics();
                    retainOffsets(visibleTopics);
                    log.warn(marker, "{} committing offsets timed out, retainOffsets: ",name, e);
                } catch (Exception ex) {
                    log.warn(marker, "{} Failed to list all authorized topics after committing offsets timed out: ",name, ex);
                }
                retry++;
                log.warn(marker, "{} Failed to commit offsets because the offset commit request processing can not be completed in time. " +
                        "If you see this regularly, it could indicate that you need to increase the consumer's ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG " +
                        "Last successful offset commit timestamp={}, retry count={}",name, LocalDateTime.ofInstant(lastOffsetCommitTime, ZoneId.systemDefault()), retry);
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (CommitFailedException e) {
                needRetry = false;
                log.error(marker, "{} Failed to commit offsets because the consumer group has rebalanced and assigned partitions to " +
                        "another instance. If you see this regularly, it could indicate that you need to either increase " +
                        "the consumer's SESSION_TIMEOUT_MS_CONFIG or reduce the number of records " +
                        "handled on each iteration with MAX_POLL_RECORDS_CONFIG",name);
            }
        }
    }

    /**
     * 重置offsets变量内容，移除在新的分区信息中不存在的offsets项
     *
     * @param visibleTopics
     */
    private void retainOffsets(Map<String, List<PartitionInfo>> visibleTopics) {
        if (offsets.isEmpty()) {
            return;
        }
        offsets.entrySet().removeIf(entry -> !visibleTopics.containsKey(entry.getKey().topic()));
    }

    /**
     * 提交分区的offset
     * Params:
     * onlyConfirm – true只提交确认过的offset。 false提交消费过的所有offset
     *
     * @param onlyConfirm
     */
    private void doCommitOffset(boolean onlyConfirm) {
        if (offsets.isEmpty()) {
            return;
        }
        synchronized (offsets) {
            Map<TopicPartition, OffsetAndMetadata> commitMaps = new HashMap<>();
            for (Map.Entry<TopicPartition, ConfirmOffsetPair> entry : offsets.entrySet()) {
                TopicPartition topicPartition = entry.getKey();
                ConfirmOffsetPair confirmOffsetPair = entry.getValue();
                //根据onlyConfirm的值，判断提交的offset是消费过的offset还是确认过的offset
                long toCommitOffset = onlyConfirm ? confirmOffsetPair.getConfirmCount() : confirmOffsetPair.getConsumerCount();
                long currentCommitOffset = confirmOffsetPair.getCommitOffset();
                if (currentCommitOffset < toCommitOffset) {//需要提交offset
                    commitMaps.put(topicPartition, new OffsetAndMetadata(toCommitOffset + 1));
                }
            }
            if (Duration.between(lastPrintTime, Instant.now()).getSeconds() >= PRINT_INFO_INTERVAL) {
                if (log.isInfoEnabled()) {
                    log.info(marker, "{} doCommitOffset {} commit onlyConfirm {} offset {} commitMaps {}",name, topic, onlyConfirm, offsets, commitMaps);
                }
                lastPrintTime = Instant.now();
            }
            if (!commitMaps.isEmpty()) {
                consumer.commitSync(commitMaps);
                //提交成功，更新commitOffset
                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : commitMaps.entrySet()) {
                    offsets.get(entry.getKey()).setCommitOffset(entry.getValue().offset());
                }
            }
            //如果是提交全部消费过的offset，原有的offset要清空
            if (!onlyConfirm) {
                offsets.clear();
            }
        }
    }
}
