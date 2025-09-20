package dev.bodewig.autoserializable.gradle.plugin.task;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

/**
 * Prematurely creates a jar file from the compiled classes to use during transformation
 */
@CacheableTask
public class PreAssembleJarTask extends Jar {

    /**
     * The default name of the task
     */
    public static final String TASK_NAME = "preAssembleJar";

    /**
     * The default name of the output directory
     */
    public static final String PRE_ASSEMBLE_JAR_DIR_NAME = "preAssembleJar";

    private Provider<Directory> classFiles;

    /**
     * Initializes the output directory with the default value
     */
    public PreAssembleJarTask() {
        setDestinationDirectory(getProject().getLayout().getBuildDirectory().dir(PRE_ASSEMBLE_JAR_DIR_NAME));
    }

    /**
     * Gets the directory of compiled classes to assemble in a jar
     *
     * @return The classFiles
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public Provider<Directory> getClassFiles() {
        return classFiles;
    }

    /**
     * Sets the directory of compiled classes to assemble in a jar
     *
     * @param classFiles The classFiles directory
     */
    public void setClassFiles(Provider<Directory> classFiles) {
        this.classFiles = classFiles;
        from(classFiles);
    }

    @Override
    @OutputDirectory
    public @NotNull DirectoryProperty getDestinationDirectory() {
        return super.getDestinationDirectory();
    }

    /**
     * Overloaded setter for destinationDirectory with Provider&lt;Directory&gt;
     *
     * @param destinationDirectory The destinationDirectory Provider
     */
    public void setDestinationDirectory(Provider<Directory> destinationDirectory) {
        getDestinationDirectory().set(destinationDirectory);
    }
}
