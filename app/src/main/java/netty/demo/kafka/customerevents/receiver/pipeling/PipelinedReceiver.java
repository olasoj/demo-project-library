package netty.demo.kafka.customerevents.receiver.pipeling;

import com.obsidiandynamics.worker.Terminator;
import com.obsidiandynamics.worker.WorkerOptions;
import com.obsidiandynamics.worker.WorkerThread;
import netty.demo.kafka.customerevents.receiver.CustomerPayloadOrError;
import netty.demo.kafka.customerevents.receiver.ReceiveEvent;
import netty.demo.kafka.customerevents.receiver.core.AbstractReceiver;
import netty.demo.kafka.customerevents.receiver.deserializer.CustomerPayloadDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class PipelinedReceiver extends AbstractReceiver {
    private final WorkerThread pollingThread;

    private final WorkerThread processingThread;

    private final Consumer<String, CustomerPayloadOrError> consumer;

    private final Duration pollTimeout;

    private final BlockingQueue<ReceiveEvent> receivedEvents;

    private final Queue<Map<TopicPartition, OffsetAndMetadata>> pendingOffsets = new LinkedBlockingQueue<>();

    public PipelinedReceiver(Map<String, Object> consumerConfig, String topic, Duration pollTimeout, int queueCapacity) {
        this.pollTimeout = pollTimeout;
        receivedEvents = new LinkedBlockingQueue<>(queueCapacity);

        final var mergedConfig = new HashMap<String, Object>();
        mergedConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        mergedConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomerPayloadDeserializer.class.getName());
        mergedConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        mergedConfig.putAll(consumerConfig);
        consumer = new KafkaConsumer<>(mergedConfig);
        consumer.subscribe(Set.of(topic));

        pollingThread = WorkerThread.builder()
                .withOptions(new WorkerOptions().daemon().withName(PipelinedReceiver.class, "poller"))
                .onCycle(this::onPollCycle)
                .build();

        processingThread = WorkerThread.builder()
                .withOptions(new WorkerOptions().daemon().withName(PipelinedReceiver.class, "processor"))
                .onCycle(this::onProcessCycle)
                .build();
    }

    @Override
    public void start() {
        pollingThread.start();
        processingThread.start();
    }

    private void onPollCycle(WorkerThread t) throws InterruptedException {
        final ConsumerRecords<String, CustomerPayloadOrError> records;

        try {
            records = consumer.poll(pollTimeout);
        } catch (InterruptException e) {
            throw new InterruptedException("Interrupted during poll");
        }

        if (!records.isEmpty()) {
            for (var consumerRecord : records) {
                final var value = consumerRecord.value();
                final var event = new ReceiveEvent(value.payload(), value.error(), consumerRecord, value.encodedValue());
                receivedEvents.put(event);
            }
        }

        for (Map<TopicPartition, OffsetAndMetadata> pendingOffset; (pendingOffset = pendingOffsets.poll()) != null; ) {
            consumer.commitAsync(pendingOffset, null);
        }
    }

    private void onProcessCycle(WorkerThread t) throws InterruptedException {
        final var event = receivedEvents.take();
        super.fire(event);
        final var consumerRecord = event.consumerRecord();
        pendingOffsets.add(Map.of(
                 new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                new OffsetAndMetadata(consumerRecord.offset() + 1)));
    }

    @Override
    public void close() {
        Terminator.of(pollingThread, processingThread).terminate().joinSilently();
        consumer.close();
    }
}
