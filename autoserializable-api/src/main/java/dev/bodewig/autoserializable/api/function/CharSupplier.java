package dev.bodewig.autoserializable.api.function;

/**
 * A specialization of {@link java.util.function.Supplier} returning a {@code char}
 */
@FunctionalInterface
public interface CharSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    char getAsChar();
}
