package dev.bodewig.autoserializable.api.function;

/**
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface FloatSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    float getAsFloat();
}
