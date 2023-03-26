package netty.demo.kafka.customerevents.sender;

import netty.demo.kafka.customerevents.event.*;

import java.util.UUID;

import static netty.demo.kafka.customerevents.sender.EventSender.SendException;

public final class ProducerBusinessLogic {
    private final EventSender sender;

    public ProducerBusinessLogic(EventSender sender) {
        this.sender = sender;
    }

    public void generateRandomEvents() throws SendException {
        final var create = new CreateCustomer(UUID.randomUUID(), "Bob", "Brown");
        blockingSend(create);

        if (Math.random() > 0.5) {
            final var update = new UpdateCustomer(create.getId(), "Charlie", "Brown");
            blockingSend(update);
        }

        if (Math.random() > 0.5) {
            final var suspend = new SuspendCustomer(create.getId());
            blockingSend(suspend);

            if (Math.random() > 0.5) {
                final var reinstate = new ReinstateCustomer(create.getId());
                blockingSend(reinstate);
            }
        }
    }

    private void blockingSend(CustomerPayload payload) throws EventSender.SendException {
        System.out.format("Publishing %s%n", payload);
        sender.blockingSend(payload);
    }
}
