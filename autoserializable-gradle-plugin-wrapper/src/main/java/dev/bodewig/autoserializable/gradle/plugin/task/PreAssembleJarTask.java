package dev.bodewig.autoserializable.gradle.plugin.task;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.jvm.tasks.Jar;

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
        forceDestinationDirectory(getProject().getLayout().getBuildDirectory().dir(PRE_ASSEMBLE_JAR_DIR_NAME));
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

    /**
     * Overloaded setter for destinationDirectory with Provider&lt;Directory&gt;
     *
     * @param destinationDirectory The destinationDirectory Provider
     */
    public void forceDestinationDirectory(Provider<Directory> destinationDirectory) {
        getDestinationDirectory().set(destinationDirectory);
    }
}
