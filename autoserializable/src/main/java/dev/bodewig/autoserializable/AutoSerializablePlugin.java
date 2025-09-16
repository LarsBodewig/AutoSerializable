package dev.bodewig.autoserializable;

import dev.bodewig.autoserializable.api.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * A Byte-Buddy plugin to mark all classes and interfaces as Serializable.
 */
public class AutoSerializablePlugin extends NonPrivatePlugin implements Plugin.WithPreprocessor {

    private static final Logger logger = Logger.getLogger(AutoSerializablePlugin.class.getCanonicalName());
    private static final Map<TypeDescription, TypeDescription> typeToSerializer = new HashMap<>();
    private static volatile boolean classpathSearched = false;

    /**
     * Initializes the plugin by searching the classpath elements for custom serializers.
     *
     * @param classpathElements The classpath configured in the byte-buddy plugin
     */
    public AutoSerializablePlugin(File[] classpathElements) {
        initialize(classpathElements);
    }

    /**
     * Registers a custom serializer to use during transformation.
     *
     * @param typeDescription The custom serializer
     */
    protected static void addSerializer(TypeDescription typeDescription) {
        AnnotationDescription.Loadable<AutoSerializable> annotation =
                typeDescription.getDeclaredAnnotations().ofType(AutoSerializable.class);
        if (annotation == null) {
            throw new MissingAnnotationException(
                    "Type " + typeDescription.getName() + " implements " + AutoSerializer.class.getName() +
                            " but is missing an " + AutoSerializable.class.getName() + " annotation");
        }
        TypeDescription value = annotation.getValue("value").resolve(TypeDescription.class);
        logger.info("Registered custom serializer " + typeDescription.getName() + " for " + value.getName());
        typeToSerializer.put(value, typeDescription);
    }

    /**
     * Searches the classpath elements for custom serializers and registers them for use.
     *
     * @param classpathElements The classpath configured in the byte-buddy plugin
     */
    public void initialize(File[] classpathElements) {
        if (!classpathSearched) {
            classpathSearched = true;
            String classpath = Arrays.stream(classpathElements).map(File::toPath).map(Path::toString)
                    .collect(Collectors.joining(File.pathSeparator));
            logger.info("Scanning classpath for custom serializers");
            logger.fine("Classpath elements: " + Arrays.toString(classpathElements));
            try (ScanResult result = new ClassGraph().overrideClasspath(classpath).enableAnnotationInfo().scan()) {
                List<TypeDescription> types =
                        result.getClassesWithAnyAnnotation(AutoSerializableAll.class, AutoSerializable.class)
                                .loadClasses().stream().map(TypeDescription.ForLoadedType::of).toList();
                logger.info("Found " + types.size() + " custom serializers on classpath");
                types.forEach(AutoSerializablePlugin::addSerializer);
            }
        }
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
        // skip already processed classes
        if (typeDescription.getDeclaredAnnotations().isAnnotationPresent(AutoSerialized.class)) {
            return builder;
        }

        // mark type as AutoSerialized
        builder = builder.annotateType(AnnotationDescription.Builder.ofType(AutoSerialized.class).build());

        // make all private types and fields package-private
        builder = super.apply(builder, typeDescription, classFileLocator);

        // serializers should not be made serializable
        if (typeDescription.isAssignableTo(AutoSerializer.class)) {
            return builder;
        }

        boolean implementsSerializable = typeDescription.isEnum()// typeDescriptions of Enums don't declare implementing
                // Serializable, but do after compilation leading to exceptions when trying to add it explicitly
                || typeDescription.getInterfaces().stream().anyMatch(i -> i.represents(Serializable.class));

        // implements Serializable
        if (!implementsSerializable) {
            builder = builder.implement(Serializable.class);
        }

        // transformation of interfaces done
        if (typeDescription.isInterface()) {
            return builder;
        }

        // find annotated AutoSerializer or use DefaultSerializer
        TypeDescription serializer = TypeDescription.ForLoadedType.of(AutoSerializer.class);
        List<StackManipulation> initializer = new ArrayList<>();
        if (typeToSerializer.containsKey(typeDescription)) {
            serializer = typeToSerializer.get(typeDescription);
            logger.info("Injected custom serializer " + serializer.getName() + " into " + typeDescription.getName());

            if (serializer.getDeclaredAnnotations().isAnnotationPresent(SerialPersistentFields.class)) {
                AnnotationDescription.Loadable<SerialPersistentFields> annotation =
                        serializer.getDeclaredAnnotations().ofType(SerialPersistentFields.class);
                String[] annotationValues = annotation.getValue("value").resolve(String[].class);
                TypeDescription osfType = TypeDescription.ForLoadedType.of(ObjectStreamField.class);
                Map<String, TypeDescription> serialPersistentFields = Arrays.stream(annotationValues).collect(
                        Collectors.toMap(Function.identity(), name -> typeDescription.getDeclaredFields().stream()
                                .filter(field -> field.getName().equals(name))
                                .map(FieldDescription.InDefinedShape::getType).map(TypeDefinition::asErasure).findAny()
                                .orElseThrow(() -> new RuntimeException(new NoSuchFieldException(name)))));
                List<StackManipulation.Compound> osfValues = serialPersistentFields.entrySet().stream()
                        .map(entry -> new StackManipulation.Compound(TypeCreation.of(osfType), Duplication.SINGLE,
                                new TextConstant(entry.getKey()), ClassConstant.of(entry.getValue()),
                                MethodInvocation.invoke(osfType.getDeclaredMethods()
                                        .filter(isConstructor().and(takesArguments(String.class, Class.class)))
                                        .getOnly()))).toList();

                // private static final ObjectStreamField[] serialPersistentFields;
                builder = builder.defineField(SerialPersistentFields.FIELD_NAME,
                        TypeDescription.ForLoadedType.of(ObjectStreamField[].class), Visibility.PRIVATE,
                        Ownership.STATIC, FieldManifestation.FINAL);

                // serialPersistentFields = new ObjectStreamField[]{ new ObjectStreamField(...), ... };
                StackManipulation osfInitializer = new StackManipulation.Compound(
                        ArrayFactory.forType(osfType.asGenericType()).withValues(osfValues), FieldAccess.forField(
                        builder.toTypeDescription().getDeclaredFields().filter(named(SerialPersistentFields.FIELD_NAME))
                                .getOnly()).write());
                initializer.add(osfInitializer);

                logger.info("Added serialPersistentFields " + Arrays.toString(annotationValues) + " to " +
                        typeDescription.getName());
            }
        }

        // private static final AutoSerializer _serializer;
        builder = builder.defineField(AutoSerializer.FIELD_NAME, TypeDescription.ForLoadedType.of(AutoSerializer.class),
                Visibility.PRIVATE, Ownership.STATIC, FieldManifestation.FINAL);

        // _serializer = new <serializer>();
        StackManipulation serializerInit =
                new StackManipulation.Compound(TypeCreation.of(serializer), Duplication.SINGLE, MethodInvocation.invoke(
                        serializer.getDeclaredMethods().filter(isConstructor().and(takesArguments(0))).getOnly()),
                        FieldAccess.forField(
                                builder.toTypeDescription().getDeclaredFields().filter(named(AutoSerializer.FIELD_NAME))
                                        .getOnly()).write());
        initializer.add(serializerInit);
        initializer.add(MethodReturn.VOID);

        builder = builder.invokable(isTypeInitializer())
                .intercept(new Implementation.Simple(new StackManipulation.Compound(initializer)));

        // private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        //     _serializer.writeObject(out, this);
        // }
        try {
            builder = builder.defineMethod("writeObject", void.class, Opcodes.ACC_PRIVATE)
                    .withParameter(ObjectOutputStream.class, "out").throwing(IOException.class).intercept(
                            MethodCall.invoke(
                                            AutoSerializer.class.getDeclaredMethod("writeObject",
                                                    ObjectOutputStream.class,
                                                    Object.class)).onField(AutoSerializer.FIELD_NAME).withArgument(0)
                                    .withThis());
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
                            .onField(AutoSerializer.FIELD_NAME).withArgument(0).withThis());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return builder;
    }
}
