package dev.bodewig.autoserializable.api;

/**
 * Indicates a problem in the usage of {@link GetFieldBuilder} or {@link PutFieldBuilder}.
 */
public class FieldBuilderException extends RuntimeException {

    /**
     * Creates a new instance with a root exception
     *
     * @param exception the cause
     */
    public FieldBuilderException(Exception exception) {
        super(exception);
    }

    /**
     * Creates a new instance with an exception message
     *
     * @param message the message of the exception
     */
    public FieldBuilderException(String message) {
        super(message);
    }
}
