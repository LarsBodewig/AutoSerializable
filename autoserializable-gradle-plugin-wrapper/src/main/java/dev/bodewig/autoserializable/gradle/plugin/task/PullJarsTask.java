package dev.bodewig.autoserializable.gradle.plugin.task;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.Set;

/**
 * Places the dependencies specified as configuration in a directory
 */
@CacheableTask
public class PullJarsTask extends Copy {

    /**
     * The default name of the task
     */
    public static final String TASK_NAME = "pullJars";
    /**
     * The default name of the output directory
     */
    public static final String PULLED_DIR_NAME = "pulledJars";

    private Set<File> configuration;
    private Provider<Directory> pulledDir;

    /**
     * Initializes the output directory with the default value
     */
    public PullJarsTask() {
        setPulledDir(getProject().getLayout().getBuildDirectory().dir(PULLED_DIR_NAME));
    }

    /**
     * Gets the resolved files of the configuration to fetch
     *
     * @return The configuration
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public Set<File> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration to fetch using a NamedDomainObjectProvider
     *
     * @param configuration The configuration NamedDomainObjectProvider
     */
    public void setConfiguration(NamedDomainObjectProvider<Configuration> configuration) {
        setConfiguration(configuration.get());
    }

    /**
     * Sets the configuration to fetch
     *
     * @param configuration The configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration.getFiles();
        from(configuration);
    }

    /**
     * Gets the output directory
     *
     * @return The pulledDir
     */
    @OutputDirectory
    public Provider<Directory> getPulledDir() {
        return pulledDir;
    }

    /**
     * Sets the output directory
     *
     * @param pulledDir The pulledDir
     */
    public void setPulledDir(Provider<Directory> pulledDir) {
        this.pulledDir = pulledDir;
        into(pulledDir);
    }
}
