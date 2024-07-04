package dev.bodewig.autoserializable;

import dev.bodewig.autoserializable.api.AutoSerializable;
import dev.bodewig.autoserializable.api.AutoSerializer;
import dev.bodewig.autoserializable.api.MissingAnnotationException;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.Transformer;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * A Byte-Buddy plugin to mark all classes and interfaces as Serializable.
 */
public class AutoSerializablePlugin implements Plugin.WithPreprocessor {

    private static final String FIELD_NAME = "_serializer";

    private static final Logger logger = Logger.getLogger(AutoSerializablePlugin.class.getCanonicalName());

    protected Map<TypeDescription, TypeDescription> typeToSerializer;

    /**
     * Default constructor
     */
    public AutoSerializablePlugin() {
        typeToSerializer = new HashMap<>();
    }

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return typeDefinitions != null;
    }

    @Override
    public void onPreprocess(TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        if (typeDescription.isAssignableTo(AutoSerializer.class)) {
            AnnotationDescription.Loadable<AutoSerializable> annotation =
                    typeDescription.getDeclaredAnnotations().ofType(AutoSerializable.class);
            if (annotation == null) {
                throw new MissingAnnotationException(
                        "Type " + typeDescription.getName() + " implements " + AutoSerializer.class.getName() +
                                " but is missing an " + AutoSerializable.class.getName() + " annotation");
            }
            TypeDescription value = annotation.getValue("value").resolve(TypeDescription.class);
            typeToSerializer.put(value, typeDescription);
        }
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        // serializers should not be made serializable
        if (typeDescription.isAssignableTo(AutoSerializer.class)) {
            return builder;
        }

        // make all private types package private
        if (typeDescription.isPrivate()) {
            builder = builder.modifiers(Visibility.PACKAGE_PRIVATE);
        }

        // implements Serializable
        boolean implementsSerializable =
                typeDescription.getInterfaces().stream().anyMatch(interf -> interf.represents(Serializable.class));
        if (!implementsSerializable) {
            builder = builder.implement(Serializable.class);
        }

        // transformation of interfaces done
        if (typeDescription.isInterface()) {
            return builder;
        }

        // make all private fields package private
        builder = builder.field(isPrivate()).transform(Transformer.ForField.withModifiers(Visibility.PACKAGE_PRIVATE));

        // find annotated AutoSerializer or use DefaultSerializer
        TypeDescription serializer = TypeDescription.ForLoadedType.of(AutoSerializer.DefaultSerializer.class);
        if (typeToSerializer.containsKey(typeDescription)) {
            serializer = typeToSerializer.get(typeDescription);
            logger.info("Binding custom serializer " + serializer.getCanonicalName() + " to " +
                    typeDescription.getCanonicalName());
        }

        // private static AutoSerializer _serializer;
        builder = builder.defineField(FIELD_NAME, TypeDescription.Generic.Builder.rawType(AutoSerializer.class).build(),
                Visibility.PRIVATE, Ownership.STATIC, FieldManifestation.FINAL);

        // static { _serializer = new <serializer>(); }
        builder = builder.invokable(isTypeInitializer()).intercept(
                MethodCall.construct(serializer.getDeclaredMethods().filter(isDefaultConstructor()).getOnly())
                        .setsField(named(FIELD_NAME)));

        // private void writeObject(java.io.ObjectOutputStream out) throws IOException
        try {
            builder = builder.defineMethod("writeObject", void.class, Opcodes.ACC_PRIVATE)
                    .withParameter(ObjectOutputStream.class, "out").throwing(IOException.class).intercept(
                            MethodCall.invoke(
                                    AutoSerializer.class.getDeclaredMethod("writeObject", ObjectOutputStream.class,
                                            Object.class)).onField(FIELD_NAME).withArgument(0).withThis());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
        try {
            builder = builder.defineMethod("readObject", void.class, Opcodes.ACC_PRIVATE)
                    .withParameter(ObjectInputStream.class, "in")
                    .throwing(IOException.class, ClassNotFoundException.class).intercept(MethodCall.invoke(
                                    AutoSerializer.class.getDeclaredMethod("readObject", ObjectInputStream.class,
                                            Object.class))
                            .onField(FIELD_NAME).withArgument(0).withThis());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return builder;
    }

    @Override
    public void close() {
        // no-op
    }
}
