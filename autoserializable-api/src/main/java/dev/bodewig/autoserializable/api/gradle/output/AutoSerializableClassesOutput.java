package dev.bodewig.autoserializable.api.gradle.output;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Output container for processed classes list
 */
public class AutoSerializableClassesOutput {

    /**
     * The extension name
     */
    public static final String NAME = "AutoSerializableClassesOutput";

    private Set<File> classFiles;

    /**
     * Empty default constructor
     */
    public AutoSerializableClassesOutput() {
        classFiles = new HashSet<>();
    }

    /**
     * Returns the processed classes
     *
     * @return the class set
     */
    public Set<File> getClassFiles() {
        return classFiles;
    }

    /**
     * Sets the processed classes
     *
     * @param classFiles the class set
     */
    public void setClassFiles(Set<File> classFiles) {
        this.classFiles = classFiles;
    }
}
