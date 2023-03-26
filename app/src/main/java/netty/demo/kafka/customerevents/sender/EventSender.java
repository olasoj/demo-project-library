package netty.demo.kafka.customerevents.sender;


import netty.demo.kafka.customerevents.event.CustomerPayload;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.Closeable;
import java.io.Serial;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface EventSender extends Closeable {
    Future<RecordMetadata> send(CustomerPayload payload);

    default RecordMetadata blockingSend(CustomerPayload payload) throws SendException {
        try {
            return send(payload).get();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new SendException(interruptedException.getCause());
        } catch (ExecutionException e) {
            throw new SendException(e.getCause());
        }
    }

    @Override
    void close();

    final class SendException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        SendException(Throwable cause) {
            super(cause);
        }
    }
}