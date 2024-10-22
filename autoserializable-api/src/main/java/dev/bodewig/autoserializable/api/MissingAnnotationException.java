package dev.bodewig.autoserializable.api;

/**
 * Indicates that the {@code AutoSerializablePlugin} found a {@link AutoSerializer} without a
 * {@link AutoSerializable} annotation.
 */
public class MissingAnnotationException extends RuntimeException {

    /**
     * Creates a new instance with an exception message
     *
     * @param message the message of the exception
     */
    public MissingAnnotationException(String message) {
        super(message);
    }
}
