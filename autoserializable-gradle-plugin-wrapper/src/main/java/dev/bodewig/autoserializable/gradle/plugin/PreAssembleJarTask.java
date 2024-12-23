package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;


@CacheableTask
public class PreAssembleJarTask extends Jar {

    public static final String TASK_NAME = "preAssembleJar";
    public static final String PRE_ASSEMBLE_JAR_DIR_NAME = "preAssembleJar";

    private final DirectoryProperty classFiles;

    public PreAssembleJarTask() {
        classFiles = getProject().getObjects().directoryProperty();
        setDestinationDirectory(getProject().getLayout().getBuildDirectory().dir(PRE_ASSEMBLE_JAR_DIR_NAME));
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getClassFiles() {
        return classFiles;
    }

    @Override
    @OutputDirectory
    public @NotNull DirectoryProperty getDestinationDirectory() {
        return super.getDestinationDirectory();
    }

    public void setDestinationDirectory(Provider<Directory> destinationDirectory) {
        getDestinationDirectory().set(destinationDirectory);
    }
}
