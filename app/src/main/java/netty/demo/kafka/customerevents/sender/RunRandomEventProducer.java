package netty.demo.kafka.customerevents.sender;

import org.apache.kafka.clients.producer.ProducerConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static netty.demo.kafka.customerevents.sender.EventSender.SendException;


public final class RunRandomEventProducer {
    public static void main(String[] args) throws InterruptedException, SendException {
        final var config =
                Map.<String, Object>of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                        ProducerConfig.CLIENT_ID_CONFIG, "customer-producer-sample");

        final var topic = "customer.test";

        try (var sender = new DirectSender(config, topic)) {
            final var businessLogic = new ProducerBusinessLogic(sender);

            Instant now = Instant.now();
            Instant plus = now.plus(Duration.ofMinutes(3));

            do {
                businessLogic.generateRandomEvents();
                Thread.sleep(500);

            } while (!Instant.now().isAfter(plus));
        }
    }
}