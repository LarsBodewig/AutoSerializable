package dev.bodewig.autoserializable.gradle.plugin.output;

import org.gradle.api.provider.SetProperty;

import java.io.File;

/**
 * Output container for class list
 */
public abstract class JavaClassesOutput {

    /**
     * The extension name
     */
    public static final String NAME = "JavaClassesOutput";

    /**
     * Empty default constructor
     */
    public JavaClassesOutput() {
    }

    /**
     * Returns the classes
     *
     * @return the class set
     */
    public abstract SetProperty<File> getClassFiles();
}
