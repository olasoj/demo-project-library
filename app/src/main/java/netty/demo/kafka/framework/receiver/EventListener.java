package netty.demo.kafka.framework.receiver;

@FunctionalInterface
public interface EventListener<P> {
  void onEvent(ReceiveEvent<? extends P> event);
}
