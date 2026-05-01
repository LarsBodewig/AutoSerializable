package dev.bodewig.autoserializable.api.gradle.output;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Output container for class list
 */
public class JavaClassesOutput {

    /**
     * The extension name
     */
    public static final String NAME = "JavaClassesOutput";

    private Set<File> classFiles;

    /**
     * Empty default constructor
     */
    public JavaClassesOutput() {
        classFiles = new HashSet<>();
    }

    /**
     * Returns the classes
     *
     * @return the class set
     */
    public Set<File> getClassFiles() {
        return classFiles;
    }

    /**
     * Sets the classes
     *
     * @param classFiles the class set
     */
    public void setClassFiles(Set<File> classFiles) {
        this.classFiles = classFiles;
    }
}
