package dev.bodewig.autoserializable.api.function;

/**
 * A specialization of {@link java.util.function.Supplier} returning a {@code short}
 */
@FunctionalInterface
public interface ShortSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    short getAsShort();
}
