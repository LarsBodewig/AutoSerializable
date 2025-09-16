package dev.bodewig.autoserializable.api.function;

/**
 * @see java.util.function.Supplier
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
