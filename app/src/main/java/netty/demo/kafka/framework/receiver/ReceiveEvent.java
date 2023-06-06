package netty.demo.kafka.framework.receiver;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public final class ReceiveEvent<P> {
    private final P payload;

    private final ConsumerRecord<?, ?> consumerRecord;

    private final Throwable error;

    public ReceiveEvent(P payload, ConsumerRecord<?, ?> consumerRecord, Throwable error) {
        this.payload = payload;
        this.consumerRecord = consumerRecord;
        this.error = error;
    }

    public P getPayload() {
        return payload;
    }

    public ConsumerRecord<?, ?> getRecord() {
        return consumerRecord;
    }

    public Throwable getError() {
        return error;
    }
}
