package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.Set;

@CacheableTask
public class PullJarsTask extends Copy {

    public static final String TASK_NAME = "pullJars";
    public static final String PULLED_DIR_NAME = "pulledJars";

    private Set<File> configuration;
    private Provider<Directory> pulledDir;

    public PullJarsTask() {
        setPulledDir(getProject().getLayout().getBuildDirectory().dir(PULLED_DIR_NAME));
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public Set<File> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(NamedDomainObjectProvider<Configuration> configuration) {
        setConfiguration(configuration.get());
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration.getFiles();
        from(configuration);
    }

    @OutputDirectory
    public Provider<Directory> getPulledDir() {
        return pulledDir;
    }

    public void setPulledDir(Provider<Directory> pulledDir) {
        this.pulledDir = pulledDir;
        into(pulledDir);
    }
}
