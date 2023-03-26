package netty.demo.kafka.customerevents.receiver;

import netty.demo.kafka.customerevents.event.CustomerPayload;

public record CustomerPayloadOrError(
        CustomerPayload payload,
        Throwable error,
        String encodedValue
) {

    @Override
    public String toString() {
        return CustomerPayloadOrError.class.getSimpleName() + " [payload=" + payload +
                ", error=" + error + ", encodedValue=" + encodedValue + "]";
    }
}
