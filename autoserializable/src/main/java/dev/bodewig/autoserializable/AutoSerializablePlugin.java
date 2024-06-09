package dev.bodewig.autoserializable;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * A Byte-Buddy plugin to mark all classes and interfaces as Serializable.
 */
public class AutoSerializablePlugin implements Plugin {

    private static final DynamicType writeInterface = new ByteBuddy()
            .makeInterface()
            .name("dev.bodewig.autoserializable.WriteInterface")
            .defineMethod("apply", void.class, Visibility.PUBLIC)
            .withParameter(ObjectOutputStream.class, "out")
            .throwing(IOException.class)
            .withoutCode()
            .make();

    private static final DynamicType writeDefaultImpl;

    static {
        try {
            writeDefaultImpl = new ByteBuddy()
                    .subclass(Object.class)
                    .name("dev.bodewig.autoserializable.WriteInterface.DefaultImpl")
                    .implement(writeInterface.getTypeDescription())
                    .defineMethod("apply", void.class, Visibility.PUBLIC)
                    .withParameter(ObjectOutputStream.class, "out")
                    .throwing(IOException.class)
                    .intercept(MethodCall.invoke(new MethodDescription.ForLoadedMethod(
                            ObjectOutputStream.class.getMethod("defaultWriteObject"))).onArgument(0))
                    .make();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static final DynamicType readInterface = new ByteBuddy()
            .makeInterface()
            .name("dev.bodewig.autoserializable.ReadInterface")
            .defineMethod("apply", void.class, Opcodes.ACC_PUBLIC)
            .withParameter(ObjectInputStream.class, "in")
            .throwing(IOException.class, ClassNotFoundException.class)
            .withoutCode()
            .make();

    private static final DynamicType readDefaultImpl;

    static {
        try {
            readDefaultImpl = new ByteBuddy()
                    .subclass(Object.class)
                    .name("dev.bodewig.autoserializable.ReadInterface.DefaultImpl")
                    .implement(readInterface.getTypeDescription())
                    .defineMethod("apply", void.class, Opcodes.ACC_PUBLIC)
                    .withParameter(ObjectInputStream.class, "in")
                    .throwing(IOException.class, ClassNotFoundException.class)
                    .intercept(MethodCall.invoke(new MethodDescription.ForLoadedMethod(
                            ObjectInputStream.class.getMethod("defaultReadObject"))).onArgument(0))
                    .make();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor
     */
    public AutoSerializablePlugin() {
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        boolean implementsSerializable = typeDescription.getInterfaces().stream()
                .anyMatch(interf -> interf.getTypeName().equals("java.io.Serializable"));
        if (!implementsSerializable) {
            builder = builder.implement(Serializable.class);
        }
        if (!typeDescription.isInterface()) {
            // make class public to allow access to static fields
            int modPublic = typeDescription.getModifiers() | Opcodes.ACC_PUBLIC;
            builder = builder.modifiers(modPublic);

            if (typeDescription.getDeclaredMethods().filter(
                    named("writeObject")
                            .and(returns(void.class))
                            .and(takesArguments(ObjectOutputStream.class))
                            .and(declaresException(IOException.class))
            ).isEmpty()) {
                builder = builder.require(writeInterface);
                builder = builder.require(writeDefaultImpl);

                // public static WriteInterface _writeObject;
                builder = builder.defineField("_writeObject",
                        writeInterface.getTypeDescription(), Visibility.PUBLIC, Ownership.STATIC);

                // static { _writeObject = new WriteInterface.DefaultImpl(); }
                builder = builder.invokable(isTypeInitializer()).intercept(
                        MethodCall.construct(writeDefaultImpl.getTypeDescription().getDeclaredMethods().filter(isDefaultConstructor()).getOnly())
                                .setsField(named("_writeObject")));

                // private void writeObject(java.io.ObjectOutputStream out) throws IOException
                builder = builder.defineMethod("writeObject", void.class, Opcodes.ACC_PRIVATE)
                        .withParameter(ObjectOutputStream.class, "out")
                        .throwing(IOException.class)
                        .intercept(MethodCall.invoke(writeInterface.getTypeDescription().getDeclaredMethods().filter(named("apply")).getOnly())
                                .onField("_writeObject")
                                .withAllArguments());
            }

            if (typeDescription.getDeclaredMethods().filter(
                    named("readObject")
                            .and(returns(void.class))
                            .and(takesArguments(ObjectInputStream.class))
                            .and(declaresException(IOException.class).and(declaresException(ClassNotFoundException.class)))
            ).isEmpty()) {
                builder = builder.require(readInterface);
                builder = builder.require(readDefaultImpl);

                // public static ReadInterface _readObject;
                builder = builder.defineField("_readObject",
                        readInterface.getTypeDescription(), Visibility.PUBLIC, Ownership.STATIC);

                // static { _readObject = new ReadInterface.DefaultImpl(); }
                builder = builder.invokable(isTypeInitializer()).intercept(
                        MethodCall.construct(readDefaultImpl.getTypeDescription().getDeclaredMethods().filter(isDefaultConstructor()).getOnly())
                                .setsField(named("_readObject")));

                // private void writeObject(java.io.ObjectOutputStream out) throws IOException
                builder = builder.defineMethod("readObject", void.class, Opcodes.ACC_PRIVATE)
                        .withParameter(ObjectInputStream.class, "in")
                        .throwing(IOException.class, ClassNotFoundException.class)
                        .intercept(MethodCall.invoke(readInterface.getTypeDescription().getDeclaredMethods().filter(named("apply")).getOnly())
                                .onField("_readObject")
                                .withAllArguments());
            }
        }

        return builder;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return typeDefinitions != null;
    }
}
