package netty.demo.kafka.customerevents.sender;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import netty.demo.kafka.customerevents.event.CustomerPayload;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Serial;

public final class CustomerPayloadSerializer implements Serializer<CustomerPayload> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final class MarshallingException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        private MarshallingException(Throwable cause) { super(cause); }
    }

    @Override
    public byte[] serialize(String topic, CustomerPayload data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new MarshallingException(e);
        }
    }
}