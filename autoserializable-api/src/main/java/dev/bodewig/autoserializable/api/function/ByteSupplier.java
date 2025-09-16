package dev.bodewig.autoserializable.api.function;

/**
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface ByteSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    byte getAsByte();
}
