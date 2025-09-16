package dev.bodewig.autoserializable.api;

public class FieldBuilderException extends RuntimeException {
    public FieldBuilderException(Exception exception) {
        super(exception);
    }
    public FieldBuilderException(String message) {
        super(message);
    }
}
