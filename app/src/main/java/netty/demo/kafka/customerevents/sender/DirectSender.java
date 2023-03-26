package netty.demo.kafka.customerevents.sender;


import netty.demo.kafka.customerevents.event.CustomerPayload;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public final class DirectSender implements EventSender {
    private final Producer<String, CustomerPayload> producer;

    private final String topic;

    public DirectSender(Map<String, Object> producerConfig, String topic) {
        this.topic = topic;

        final var mergedConfig = new HashMap<String, Object>();
        mergedConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        mergedConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomerPayloadSerializer.class.getName());
        mergedConfig.putAll(producerConfig);
        producer = new KafkaProducer<>(mergedConfig);
    }

    @Override
    public Future<RecordMetadata> send(CustomerPayload payload) {
        final var producerRecord = new ProducerRecord<>(topic, payload.getId().toString(), payload);
        return producer.send(producerRecord);
    }

    @Override
    public void close() {
        producer.close();
    }
}
