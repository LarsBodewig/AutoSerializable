package dev.bodewig.autoserializable.api.function;

/**
 * A specialization of {@link java.util.function.Function} declaring a {@code Throwable} of type {@code T}
 *
 * @param <I> the type of the input to the function
 * @param <O> the type of the result of the function
 * @param <T> the type of the exception of the function
 */
@FunctionalInterface
public interface ThrowingFunction<I, O, T extends Throwable> {

    /**
     * Applies this function to the given argument.
     *
     * @param input the function argument
     * @return the function result
     * @throws T the declared exception
     */
    O apply(I input) throws T;
}
