package dev.bodewig.autoserializable;

import net.bytebuddy.dynamic.ClassFileLocator;

import java.io.IOException;

public class ClassFileLoader extends ClassLoader {

    private final ClassFileLocator locator;

    public ClassFileLoader(ClassFileLocator locator) {
        this.locator = locator;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name, true);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            ClassFileLocator.Resolution resolution = locator.locate(name);
            if (resolution.isResolved()) {
                byte[] bytes = resolution.resolve();
                return this.defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
