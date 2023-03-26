package netty.demo.kafka.customerevents.receiver.core;

import netty.demo.kafka.customerevents.receiver.EventListener;

import java.io.*;

public interface EventReceiver extends Closeable {
  void addListener(EventListener listener);
  
  void start();
  
  @Override
  void close();
}
