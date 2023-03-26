package netty.demo.kafka.customerevents.receiver;

import netty.demo.kafka.customerevents.receiver.core.EventReceiver;

public final class ConsumerBusinessLogic {
    public ConsumerBusinessLogic(EventReceiver receiver) {
        receiver.addListener(this::onEvent);
    }

    private void onEvent(ReceiveEvent event) {
        if (!event.isError()) {
            System.out.format("Received %s%n", event.payload());
        } else {
            System.err.format("Error in record %s: %s%n", event.consumerRecord(), event.error());
        }
    }
}
