package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

/**
 * Utility class to generate project Dependencies
 */
public final class AutoSerializableDependencies {

    /**
     * The groupId of the project
     */
    public static final String GROUP_ID = "dev.bodewig.autoserializable";
    /**
     * The artifactId for the api dependency
     */
    public static final String API_ARTIFACT_ID = "autoserializable-api";
    /**
     * The artifactId for the junit dependency
     */
    public static final String JUNIT_ARTIFACT_ID = "autoserializable-junit";
    /**
     * The version of the project
     */
    public static final String VERSION = "2.0.3-SNAPSHOT";

    private AutoSerializableDependencies() {
    }

    /**
     * Creates a dependency for the autoserializable-api
     *
     * @param project The project used to create the dependency
     * @return The dependency
     */
    public static Dependency autoserializableApi(Project project) {
        return project.getDependencies()
                .create(AutoSerializableDependencies.GROUP_ID + ":" + AutoSerializableDependencies.API_ARTIFACT_ID +
                        ":" + AutoSerializableDependencies.VERSION);
    }

    /**
     * Creates a dependency for the autoserializable-junit
     *
     * @param project The project used to create the dependency
     * @return The dependency
     */
    public static Dependency autoserializableJunit(Project project) {
        return project.getDependencies()
                .create(AutoSerializableDependencies.GROUP_ID + ":" + AutoSerializableDependencies.JUNIT_ARTIFACT_ID +
                        ":" + AutoSerializableDependencies.VERSION);
    }
}
