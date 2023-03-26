package netty.demo.kafka.customerevents.receiver;

import netty.demo.kafka.customerevents.event.CustomerPayload;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public record ReceiveEvent(
        CustomerPayload payload,
        Throwable error,
        ConsumerRecord<String, ?> consumerRecord,
        String encodedValue
) {

    public boolean isError() {
        return error != null;
    }

    @Override
    public String toString() {
        return ReceiveEvent.class.getSimpleName() + " [payload=" + payload + ", error=" + error +
                ", record=" + consumerRecord + ", encodedValue=" + encodedValue + "]";
    }
}
