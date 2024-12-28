package dev.bodewig.autoserializable;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.modifier.ModifierContributor.ForType;
import net.bytebuddy.description.modifier.ModifierContributor.Resolver;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.FieldModifierTransformerLogger;
import net.bytebuddy.dynamic.LoggingFieldModifierTransformer;

import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.isPrivate;

/**
 * A Byte-Buddy plugin to make all private types and fields package-private
 */
public class NonPrivatePlugin implements Plugin {

    private static final Logger logger = Logger.getLogger(NonPrivatePlugin.class.getCanonicalName());

    /**
     * Default constructor
     */
    public NonPrivatePlugin() {
    }

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return typeDefinitions != null;
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        logger.finer("Processing " + typeDescription.getName());
        // make all private types package-private
        if (typeDescription.isPrivate()) {
            Resolver<ForType> modResolver = Resolver.of((ForType) Visibility.PACKAGE_PRIVATE);
            int modifier = modResolver.resolve(typeDescription.getModifiers());
            builder = builder.modifiers(modifier);
            logger.fine("Changed visibility of " + typeDescription.getName() + " from private to package-private");
        }

        // make all private fields package-private
        builder = builder.field(isPrivate()).transform(
                LoggingFieldModifierTransformer.withModifier(Visibility.PACKAGE_PRIVATE,
                        new FieldModifierTransformerLogger.ForVisibility.DefaultJUL(logger)));

        return builder;
    }

    @Override
    public void close() {
        // no-op
    }
}
