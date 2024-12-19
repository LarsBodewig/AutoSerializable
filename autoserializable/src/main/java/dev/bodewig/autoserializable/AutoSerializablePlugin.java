package dev.bodewig.autoserializable;

import dev.bodewig.autoserializable.api.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * A Byte-Buddy plugin to mark all classes and interfaces as Serializable.
 */
public class AutoSerializablePlugin extends NonPrivatePlugin implements Plugin.WithPreprocessor {

    private static final String FIELD_NAME = "_serializer";

    private static final Logger logger = Logger.getLogger(AutoSerializablePlugin.class.getCanonicalName());

    private static final Map<TypeDescription, TypeDescription> typeToSerializer = new HashMap<>();
    private static final OneWayToggle classpathSearched = new OneWayToggle();

    /**
     * Default constructor
     */
    public AutoSerializablePlugin(File[] classpathElements) {
        initialize(classpathElements);
    }

    public void initialize(File[] classpathElements) {
        if (!classpathSearched.get()) {
            classpathSearched.toggle();
            String classpath = Arrays.stream(classpathElements).map(File::toPath).map(Path::toString)
                    .collect(Collectors.joining(File.pathSeparator));
            logger.severe("initialize: Found " + classpathElements.length);
            try (ScanResult result = new ClassGraph()
                    .overrideClasspath(classpath)
                    .enableAnnotationInfo()
                    .scan()) {
                List<TypeDescription> types =
                        result.getClassesWithAnyAnnotation(AutoSerializableAll.class, AutoSerializable.class)
                                .loadClasses()
                                .stream()
                                .map(TypeDescription.ForLoadedType::of)
                                .toList();
                types.forEach(System.out::println);
                logger.severe("Found " + types.size() + " AutoSerializers");
                for (TypeDescription type : types) {
                    logger.severe("AddSerializer " + type.getName());
                    addSerializer(type);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "unable to load Lwjgl3ApplicationConfigurationSerializer", e);
            }
        }
    }

    protected static void addSerializer(TypeDescription typeDescription) {
        AnnotationDescription.Loadable<AutoSerializable> annotation =
                typeDescription.getDeclaredAnnotations().ofType(AutoSerializable.class);
        if (annotation == null) {
            throw new MissingAnnotationException(
                    "Type " + typeDescription.getName() + " implements " + AutoSerializer.class.getName() +
                            " but is missing an " + AutoSerializable.class.getName() + " annotation");
        }
        TypeDescription value = annotation.getValue("value").resolve(TypeDescription.class);
        logger.severe("Registered custom serializer " + typeDescription.getCanonicalName() + " for " +
                value.getCanonicalName());
        typeToSerializer.put(value, typeDescription);
    }

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return typeDefinitions != null;
    }

    @Override
    public void onPreprocess(TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        if (typeDescription.isAssignableTo(AutoSerializer.class)) {
            addSerializer(typeDescription);
        }
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        // make all private types and fields package-private
        builder = super.apply(builder, typeDescription, classFileLocator);

        // serializers should not be made serializable
        if (typeDescription.isAssignableTo(AutoSerializer.class)) {
            return builder;
        }

        // mark type as AutoSerialized
        if (typeDescription.getDeclaredAnnotations().isAnnotationPresent(AutoSerialized.class)) {
            return builder;
        }
        builder = builder.annotateType(AnnotationDescription.Builder.ofType(AutoSerialized.class).build());

        // implements Serializable
        boolean implementsSerializable = typeDescription.isEnum()// typeDescriptions of Enums don't declare implementing
                // Serializable, but do after compilation leading to exceptions when trying to add it explicitly
                || typeDescription.getInterfaces().stream().anyMatch(i -> i.represents(Serializable.class));
        if (!implementsSerializable) {
            builder = builder.implement(Serializable.class);
        }

        // transformation of interfaces done
        if (typeDescription.isInterface()) {
            return builder;
        }

        // find annotated AutoSerializer or use DefaultSerializer
        TypeDescription serializer = TypeDescription.ForLoadedType.of(AutoSerializer.DefaultSerializer.class);
        if (typeToSerializer.containsKey(typeDescription)) {
            serializer = typeToSerializer.get(typeDescription);
            logger.severe("Binding custom serializer " + serializer.getCanonicalName() + " to " +
                    typeDescription.getCanonicalName());
        }

        // private static final AutoSerializer _serializer;
        builder = builder.defineField(FIELD_NAME, TypeDescription.Generic.Builder.rawType(AutoSerializer.class).build(),
                Visibility.PRIVATE, Ownership.STATIC, FieldManifestation.FINAL);

        // static { _serializer = new <serializer>(); }
        builder = builder.invokable(isTypeInitializer()).intercept(
                MethodCall.construct(serializer.getDeclaredMethods().filter(isDefaultConstructor()).getOnly())
                        .setsField(named(FIELD_NAME)));

        // private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        //     _serializer.writeObject(out, this);
        // }
        try {
            builder = builder.defineMethod("writeObject", void.class, Opcodes.ACC_PRIVATE)
                    .withParameter(ObjectOutputStream.class, "out").throwing(IOException.class).intercept(
                            MethodCall.invoke(
                                    AutoSerializer.class.getDeclaredMethod("writeObject", ObjectOutputStream.class,
                                            Object.class)).onField(FIELD_NAME).withArgument(0).withThis());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        //     _serializer.readObject(in, this);
        // }
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
}
