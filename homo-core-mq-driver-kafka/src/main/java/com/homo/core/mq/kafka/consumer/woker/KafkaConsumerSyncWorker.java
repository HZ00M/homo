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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class KafkaConsumerSyncWorker extends ConsumerWorker {
    static final Logger log = LoggerFactory.getLogger(KafkaConsumerSyncWorker.class);
    static final Marker marker = MarkerFactory.getMarker("ConsumeWorker");

    /**
     * 最近一次打印提交消息的时间
     */
    private Instant lastPrintTime;
    /**
     * 最近一次提交offset时间
     */
    private Instant lastOffsetCommitTime;
    public static final int PRINT_INFO_INTERVAL = 60;
    public static final int COMMIT_OFFSET_INTERVAL = 10;
    /**
     * Kafka消费者匀衡监听者
     */
    private ConsumerRebalanceListener rebalanceListener;
    final AtomicInteger partitionRevokedCount = new AtomicInteger(0);
    final AtomicInteger partitionAssignedCount = new AtomicInteger(0);
    private final Map<TopicPartition, OffsetPair> offsets = new ConcurrentHashMap<>(64);
    private final Map<TopicPartition, OffsetPair> preOffsets = new ConcurrentHashMap<>(64);
    /**
     * 消费者poll的时间参数
     */
    private long pollWaitMs;
    private Duration pollWaitDuration;

    public KafkaConsumerSyncWorker(String name, KafkaConsumer<String, Bytes> consumer, String topic, ReceiverSink<byte[]> sink, long pollWaitMs) {
        super(name, consumer, topic, sink);
        this.pollWaitMs = pollWaitMs;
        this.pollWaitDuration = Duration.ofMillis(pollWaitMs);
    }

    public void init() {
        this.lastPrintTime = Instant.now();
        this.lastOffsetCommitTime = Instant.now();
        rebalanceListener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                partitionRevokedCount.incrementAndGet();
                try {
                    commitOffsets();
                } catch (Exception e) {
                    log.error(marker, "{} topic {} onPartitionsRevoked commitOffsets ", name, topic, e);
                }
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                partitionAssignedCount.incrementAndGet();
            }
        };
        consumer.subscribe(Collections.singleton(topic), rebalanceListener);
    }

    @Override
    public void process() {
        try {
            init();
            while (status != Status.TERMINATED) {
                ConsumerRecords<String, Bytes> records = receive();
                if (records.isEmpty()) {
                    mayCommitOffsets();
                    continue;
                }
                // sink自己处理异常。不要抛出异常
                for (ConsumerRecord<String, Bytes> record : records) {
                    sink.onSink(topic, record.value().get(), new ConsumerCallback() {
                        @Override
                        public void confirm() {
                            log.info(marker, "{} process sink.onSink confirm is unnecessary,The offset is automatically submitted", name);
                        }
                    });
                }
                mayCommitOffsets();
            }
        } catch (WakeupException e) {
            log.error(marker, "kafka consumer {} throw wakeup exception, ready to close", name);
        } catch (Throwable throwable) {
            log.error(marker, "kafka consumer {} throwable {}", name, throwable);
        } finally {
            try {
                //提交剩余offset
                commitOffsets();
                //关闭消费者以及业务处理
                consumer.close();
            } catch (Exception e) {
                log.error(marker, "kafka consumer {} close throwable {}", name, e);
            }
        }
    }

    public ConsumerRecords<String, Bytes> receive() {
        ConsumerRecords<String, Bytes> records = consumer.poll(pollWaitDuration);
        if (!records.isEmpty()) {
            synchronized (offsets) {
                log.debug(marker, "{} receive size topic {} size {} partitions {}", name, topic, records.count(), records.partitions());
                //如果有消费到消息，更新offsets数据为各分区第一条和最后一条消息的offset
                for (TopicPartition topicPartition : records.partitions()) {
                    List<ConsumerRecord<String, Bytes>> partitionRecords = records.records(topicPartition);
                    long beginOffset = partitionRecords.get(0).offset();
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    int partition = topicPartition.partition();
                    OffsetPair offsetPair = OffsetPair.of(partition,beginOffset, lastOffset);
                    offsets.put(topicPartition, offsetPair);
                    OffsetPair preOffsetPair = preOffsets.get(topicPartition);
                    if (preOffsetPair != null) {
                        if (preOffsetPair.getLast() + 1 != beginOffset) {
                            log.error(marker, "{} tp={}, offset break error ,preOffset:{},curOffset:{}", name, topicPartition.toString(), preOffsetPair, offsetPair);
                        }
                    }
                }
                preOffsets.clear();
                preOffsets.putAll(offsets);
            }
        }
        return records;
    }

    public void mayCommitOffsets() throws Exception {
        if (Duration.between(lastOffsetCommitTime, Instant.now()).getSeconds() > COMMIT_OFFSET_INTERVAL) {
            commitOffsets();
            lastOffsetCommitTime = Instant.now();
        }
    }

    public void commitOffsets() throws Exception {
        int retry = 0;
        boolean needRetry = true;
        while (needRetry) {
            try {
                doCommitOffsets();
                needRetry = false;
                lastOffsetCommitTime = Instant.now();
            } catch (WakeupException e) {
                // we only call wakeup() once to close the consumer,
                // so if we catch it in commit we can safely retry
                // and re-throw to break the loop、
                //close() may close to
                //用于优化退出的异常，这里进行重试提交
                commitOffsets();
                throw e;
            } catch (TimeoutException e) {
                try {
                    Map<String, List<PartitionInfo>> visibleTopics = consumer.listTopics();
                    //移除在新的分区信息中不存在的offsets项
                    offsets.entrySet().removeIf(item -> !visibleTopics.containsKey(item.getKey().topic()));
                    retry++;
                } catch (Exception ex) {
                    log.warn(marker, "{} Failed to list all authorized topics after committing offsets timed out: ", name, ex);
                }
                log.warn(marker, "{} TimeoutException retry {}", name, retry);
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (RebalanceInProgressException e) {
                /**
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
                log.error(marker, "{} rebalance in progress", name, e);
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (CommitFailedException e) {
                //在这种情况下，通常无法重试提交因为某些分区可能已分配给组中的另一个成员。
                needRetry = false;
                log.error(marker, "{} Failed to commit offsets because the consumer group has rebalanced and assigned partitions to " +
                        "another instance. If you see this regularly, it could indicate that you need to either increase " +
                        "the consumer's SESSION_TIMEOUT_MS_CONFIG or reduce the number of records " +
                        "handled on each iteration with MAX_POLL_RECORDS_CONFIG", name);
            }
        }
    }

    public void doCommitOffsets() {
        if (offsets.isEmpty()) {
            return;
        }
        synchronized (offsets) {
            Map<TopicPartition, OffsetAndMetadata> commitMaps = new HashMap<>();
            for (Map.Entry<TopicPartition, OffsetPair> offsetPairEntry : offsets.entrySet()) {
                TopicPartition partition = offsetPairEntry.getKey();
                OffsetPair offsetPair = offsetPairEntry.getValue();
                //提交的offset，必须是上次获取的最后一条的offset+1的值，这样才会从上次offset的下一条开始消费
                commitMaps.put(partition, new OffsetAndMetadata(offsetPair.getLast()));
            }
            if (Duration.between(lastPrintTime, Instant.now()).getSeconds() >= PRINT_INFO_INTERVAL) {
                log.info(marker, "{} commitOffsets topic {} commit offset {}", name, topic, commitMaps);
                lastPrintTime = Instant.now();
            }
            consumer.commitSync(commitMaps);
            offsets.clear();
        }
    }
}
