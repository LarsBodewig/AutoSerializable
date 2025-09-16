package dev.bodewig.autoserializable.gradle.plugin.output;

import org.gradle.api.provider.SetProperty;

import java.io.File;

/**
 * Output container for processed classes list
 */
public abstract class AutoSerializableClassesOutput {

    /**
     * The extension name
     */
    public static final String NAME = "AutoSerializableClassesOutput";

    /**
     * Empty default constructor
     */
    public AutoSerializableClassesOutput() {
    }

    /**
     * Returns the processed classes
     *
     * @return the class set
     */
    public abstract SetProperty<File> getClassFiles();
}
