package dev.bodewig.autoserializable.gradle.plugin.output;

import org.gradle.api.provider.SetProperty;

import java.io.File;

/**
 * Output container for processed jars list
 */
public abstract class AutoSerializableJarsOutput {

    /**
     * The extension name
     */
    public static final String NAME = "AutoSerializableJarsOutput";

    /**
     * Empty default constructor
     */
    public AutoSerializableJarsOutput() {
    }

    /**
     * Returns the processed jars
     *
     * @return the jar set
     */
    public abstract SetProperty<File> getJarFiles();
}
