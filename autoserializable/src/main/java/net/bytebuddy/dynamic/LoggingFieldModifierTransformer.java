package net.bytebuddy.dynamic;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.ModifierContributor;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Extends the {@link Transformer.ForField.FieldModifierTransformer} to log in case the field modifier was changed
 */
public class LoggingFieldModifierTransformer extends Transformer.ForField.FieldModifierTransformer {

    /**
     * The logger implementation
     */
    protected final FieldModifierTransformerLogger logger;

    /**
     * Constructs a new instance with the supplied field modifier resolver and logger implementation
     *
     * @param resolver The resolver to change the field modifier
     * @param logger   The logger called in case the modifier changes
     */
    public LoggingFieldModifierTransformer(ModifierContributor.Resolver<ModifierContributor.ForField> resolver,
                                           FieldModifierTransformerLogger logger) {
        super(resolver);
        this.logger = logger;
    }

    /**
     * Creates a field transformer to set the supplied modifier using a {@code LoggingFieldModifierTransformer}
     *
     * @param modifier The field modifier to set
     * @param logger   The logger called in case the modifier changes
     * @return The field transformer using the {@code LoggingFieldModifierTransformer}
     */
    public static Transformer.ForField withModifier(ModifierContributor.ForField modifier,
                                                    FieldModifierTransformerLogger logger) {
        ModifierContributor.Resolver<ModifierContributor.ForField> resolver = ModifierContributor.Resolver.of(modifier);
        return new Transformer.ForField(new LoggingFieldModifierTransformer(resolver, logger));
    }

    @Override
    public FieldDescription.Token transform(TypeDescription instrumentedType, FieldDescription.Token target) {
        int originalMod = target.getModifiers();
        FieldDescription.Token transformed = super.transform(instrumentedType, target);
        if (originalMod != transformed.getModifiers()) {
            logger.log(instrumentedType, target, transformed);
        }
        return transformed;
    }
}
