package netty.demo.kafka.customerevents.receiver;

@FunctionalInterface
public interface EventListener {
  void onEvent(ReceiveEvent event);
}
