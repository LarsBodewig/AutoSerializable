package net.bytebuddy.dynamic;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.jar.asm.Opcodes;

import java.util.logging.Logger;

/**
 * Interface for logger implementations used by {@link LoggingFieldModifierTransformer}
 */
public interface FieldModifierTransformerLogger {

    /**
     * Called by {@link LoggingFieldModifierTransformer#transform(TypeDescription, FieldDescription.Token)} when a
     * modifier was changed
     *
     * @param type        The {@code TypeDescription} being transformed
     * @param original    The {@code FieldDescription.Token} being transformed
     * @param transformed The same {@code FieldDescription.Token} after transformation
     */
    void log(TypeDescription type, FieldDescription.Token original, FieldDescription.Token transformed);

    /**
     * Logger implementations for changes of visibility modifiers
     */
    interface ForVisibility {

        /**
         * Maps a numeric representation of the modifiers to a {@code Visibility} enum value
         *
         * @param modifiers The modifiers
         * @return The {@code Visibility} instance
         */
        static Visibility parseVisibility(int modifiers) {
            if ((modifiers & Opcodes.ACC_PUBLIC) != 0) {
                return Visibility.PUBLIC;
            } else if ((modifiers & Opcodes.ACC_PRIVATE) != 0) {
                return Visibility.PRIVATE;
            } else if ((modifiers & Opcodes.ACC_PROTECTED) != 0) {
                return Visibility.PROTECTED;
            } else {
                return Visibility.PACKAGE_PRIVATE;
            }
        }

        /**
         * Maps a {@code Visibility} enum value to a {@code String} for log messages
         *
         * @param visibility The {@code Visibility} instance
         * @return The loggable {@code String}
         */
        static String stringify(Visibility visibility) {
            return switch (visibility) {
                case PUBLIC -> "public";
                case PACKAGE_PRIVATE -> "package-private";
                case PROTECTED -> "protected";
                case PRIVATE -> "private";
            };
        }

        /**
         * Default implementation for changes of visibility modifiers using JUL (java.util.logging)
         */
        class DefaultJUL implements FieldModifierTransformerLogger {

            /**
             * The logger instance to use
             */
            protected final Logger logger;

            /**
             * Constructs a new instance with the supplied {@code Logger}
             *
             * @param logger The {@code Logger} to use
             */
            public DefaultJUL(Logger logger) {
                this.logger = logger;
            }

            @Override
            public void log(TypeDescription type, FieldDescription.Token original, FieldDescription.Token transformed) {
                logger.fine("Changed visibility of " + type.getName() + "." + original.getName() + " from " +
                        stringify(parseVisibility(original.getModifiers())) + " to " +
                        stringify(parseVisibility(transformed.getModifiers())));
            }
        }
    }
}
