package netty.demo.kafka.customerevents.receiver.pipeling;

import netty.demo.kafka.customerevents.receiver.ConsumerBusinessLogic;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;
import java.util.Map;

public final class RunPipelinedConsumer {
    public static void main(String[] args) throws InterruptedException {
        final var consumerConfig = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                ConsumerConfig.GROUP_ID_CONFIG, "customer-pipelined-consumer",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        try (var receiver = new PipelinedReceiver(consumerConfig, "customer.test", Duration.ofMillis(100), 10)) {
            new ConsumerBusinessLogic(receiver);
            receiver.start();
            Thread.sleep(10_000);
        }
    }
}
