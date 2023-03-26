package netty.demo.annotation.exception;

public class ClassGenerationException extends RuntimeException {

    public ClassGenerationException(String message) {
        super(message);
    }

    public ClassGenerationException(Throwable t) {
        super(t);
    }

}
