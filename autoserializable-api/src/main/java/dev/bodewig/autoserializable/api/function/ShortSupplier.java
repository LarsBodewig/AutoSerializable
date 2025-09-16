package dev.bodewig.autoserializable.api.function;

/**
 * @see java.util.function.Supplier
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
