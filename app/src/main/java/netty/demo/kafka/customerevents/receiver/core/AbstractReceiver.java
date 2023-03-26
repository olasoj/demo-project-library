package netty.demo.kafka.customerevents.receiver.core;

import netty.demo.kafka.customerevents.receiver.EventListener;
import netty.demo.kafka.customerevents.receiver.ReceiveEvent;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractReceiver implements EventReceiver {
    private final Set<EventListener> listeners = new HashSet<>();

    public final void addListener(EventListener listener) {
        listeners.add(listener);
    }

    protected final void fire(ReceiveEvent event) {
        for (var listener : listeners) {
            listener.onEvent(event);
        }
    }
}
