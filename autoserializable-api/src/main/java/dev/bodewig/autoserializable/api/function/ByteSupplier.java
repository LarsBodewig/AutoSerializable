package dev.bodewig.autoserializable.api.function;

/**
 * A specialization of {@link java.util.function.Supplier} returning a {@code byte}
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
