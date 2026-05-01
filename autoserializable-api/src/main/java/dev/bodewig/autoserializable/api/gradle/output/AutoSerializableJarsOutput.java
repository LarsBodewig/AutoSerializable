package dev.bodewig.autoserializable.api.gradle.output;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Output container for processed jars list
 */
public class AutoSerializableJarsOutput {

    /**
     * The extension name
     */
    public static final String NAME = "AutoSerializableJarsOutput";

    private Set<File> jarFiles;

    /**
     * Empty default constructor
     */
    public AutoSerializableJarsOutput() {
        jarFiles = new HashSet<>();
    }

    /**
     * Returns the processed jars
     *
     * @return the jar set
     */
    public Set<File> getJarFiles() {
        return jarFiles;
    }

    /**
     * Sets the processed jars
     *
     * @param jarFiles the jar set
     */
    public void setJarFiles(Set<File> jarFiles) {
        this.jarFiles = jarFiles;
    }
}
