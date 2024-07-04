package dev.bodewig.autoserializable.api;

public class MissingAnnotationException extends RuntimeException {

    public MissingAnnotationException(String message) {
        super(message);
    }
}
