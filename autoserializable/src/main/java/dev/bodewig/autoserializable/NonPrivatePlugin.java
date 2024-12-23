package dev.bodewig.autoserializable;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.modifier.ModifierContributor.ForType;
import net.bytebuddy.description.modifier.ModifierContributor.Resolver;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.Transformer;

import static net.bytebuddy.matcher.ElementMatchers.isPrivate;

/**
 * A Byte-Buddy plugin to make all private types and fields package-private
 */
public class NonPrivatePlugin implements Plugin {

    /**
     * Default constructor
     */
    public NonPrivatePlugin() {}

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return typeDefinitions != null;
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        // make all private types package private
        if (typeDescription.isPrivate()) {
            Resolver<ForType> modResolver = Resolver.of((ForType) Visibility.PACKAGE_PRIVATE);
            int modifier = modResolver.resolve(typeDescription.getModifiers());
            builder = builder.modifiers(modifier);
        }

        // make all private fields package private
        builder = builder.field(isPrivate()).transform(Transformer.ForField.withModifiers(Visibility.PACKAGE_PRIVATE));

        return builder;
    }

    @Override
    public void close() {
        // no-op
    }
}
