package dev.bodewig.autoserializable.api.function;

/**
 * A specialization of {@link java.util.function.Supplier} returning a {@code float}
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
