package netty.demo.kafka.basic;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static java.lang.System.out;

public class BasicProducerSample {

    public static void main(String[] args) {
        final var topic = "getting-started";
        final Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        );

        Instant now = Instant.now();
        Instant plus = now.plus(Duration.ofMillis(500));

        try (var producer = new KafkaProducer<String, String>(config)) {
            do {
                final var key = "myKey";
                final var value = Instant.now().toString();
                out.format("Publishing record with value %s%n", value);

                final Callback callback = (metadata, exception) -> out.format("Published with metadata: %s, error: %s%n", metadata, exception);

                // publish the record, handling the metadata in the callback
                producer.send(new ProducerRecord<>(topic, key, value), callback);
                // wait a second before publishing another
//                Thread.sleep(1000);

            } while (!Instant.now().isAfter(plus));
        }
    }
}
