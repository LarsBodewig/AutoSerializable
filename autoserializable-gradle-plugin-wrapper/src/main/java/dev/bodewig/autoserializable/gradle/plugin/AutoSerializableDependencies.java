package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public final class AutoSerializableDependencies {

    public static final String GROUP_ID = "dev.bodewig.autoserializable";
    public static final String API_ARTIFACT_ID = "autoserializable-api";
    public static final String JUNIT_ARTIFACT_ID = "autoserializable-junit";
    public static final String VERSION = "2.0.0";

    private AutoSerializableDependencies() {}

    public static Dependency autoserializableApi(Project project) {
        return project.getDependencies()
                .create(AutoSerializableDependencies.GROUP_ID + ":" +
                        AutoSerializableDependencies.API_ARTIFACT_ID + ":" +
                        AutoSerializableDependencies.VERSION);
    }

    public static Dependency autoserializableJunit(Project project) {
        return project.getDependencies()
                .create(AutoSerializableDependencies.GROUP_ID + ":" +
                        AutoSerializableDependencies.JUNIT_ARTIFACT_ID + ":" +
                        AutoSerializableDependencies.VERSION);
    }
}
