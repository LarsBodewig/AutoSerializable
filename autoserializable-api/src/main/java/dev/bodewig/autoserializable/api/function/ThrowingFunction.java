package dev.bodewig.autoserializable.api.function;

@FunctionalInterface
public interface ThrowingFunction<I, O, T extends Throwable> {
    O apply(I input) throws T;
}
