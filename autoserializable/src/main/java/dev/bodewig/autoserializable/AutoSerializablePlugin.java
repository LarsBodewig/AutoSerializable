package dev.bodewig.autoserializable;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.Serializable;

public class AutoSerializablePlugin implements Plugin {

    public AutoSerializablePlugin() {
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        boolean implementsSerializable = typeDescription.getInterfaces().stream()
                .anyMatch(interf -> interf.getTypeName().equals("java.io.Serializable"));
        if (!implementsSerializable) {
            builder = builder.implement(Serializable.class);
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
