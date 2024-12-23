package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Gradle plugin that invokes the custom byte-buddy plugin AutoSerializable on compiled classes
 */
public class AutoSerializableClassesGradlePlugin implements Plugin<Project> {

    /**
     * The name of the configuration used as classpath for the byte-buddy gradle plugin
     */
    public static final String SERIALIZERS_CONFIGURATION_NAME = "autoSerializers";
    /**
     * The name of the configuration used to add api dependencies
     */
    public static final String API_DEPENDENCIES_CONFIGURATION_NAME = "autoserializableDeps";
    /**
     * The name of the configuration used to add test dependencies
     */
    public static final String TEST_CONFIGURATION_NAME = "autoSerializableTest";

    /**
     * Default constructor
     */
    public AutoSerializableClassesGradlePlugin() {
    }

    /**
     * Creates a filter on a ConfigurableFileTree for jar files
     *
     * @return The filter
     */
    public static Action<? super ConfigurableFileTree> jarFilter() {
        return files -> files.include("*.jar");
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        // run non private task before compiling
        TaskProvider<JavaCompile> compileTask =
                project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);

        // create jar from compiled classes to reference on autoSerializable classpath
        PreAssembleJarTask preAssembleJarTask =
                project.getTasks().maybeCreate(PreAssembleJarTask.TASK_NAME, PreAssembleJarTask.class);
        preAssembleJarTask.setClassFiles(compileTask.get().getDestinationDirectory());
        preAssembleJarTask.dependsOn(compileTask);

        // create configuration with compiled classes and non private libs to find serializers
        Configuration serializersConfig = project.getConfigurations().maybeCreate(SERIALIZERS_CONFIGURATION_NAME);
        serializersConfig.defaultDependencies(dependencies -> {
            DirectoryProperty preAssembleJarOutput = preAssembleJarTask.getDestinationDirectory();
            Dependency compiledClasses =
                    project.getDependencies().create(project.fileTree(preAssembleJarOutput, jarFilter()));
            dependencies.add(compiledClasses);

            dependencies.add(AutoSerializableDependencies.autoserializableApi(project));
        });
        Configuration compileClasspathConfig =
                project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
        serializersConfig.extendsFrom(compileClasspathConfig);

        // create task to make downloaded dependencies serializable
        TaskProvider<AutoSerializableClassesTask> autoSerializableClassesTask = project.getTasks()
                .register(AutoSerializableClassesTask.TASK_NAME, AutoSerializableClassesTask.class, task -> {
                    task.setInPlace(compileTask.get().getDestinationDirectory());
                    task.setClassPath(serializersConfig);
                    task.dependsOn(preAssembleJarTask);
                    // order tasks if both plugins are used
                    task.mustRunAfter(project.getTasks().withType(AutoSerializableJarsTask.class));
                });

        // add autoserializable-api to api configuration
        Configuration apiConfig = project.getConfigurations().maybeCreate(API_DEPENDENCIES_CONFIGURATION_NAME);
        apiConfig.defaultDependencies(dependencies -> {
            Dependency autoserializableApi = AutoSerializableDependencies.autoserializableApi(project);
            dependencies.add(autoserializableApi);
        });
        project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME, config -> {
            config.extendsFrom(apiConfig);
        });

        // add autoserializable jars and autoserializable-junit to test configuration
        Configuration testConfig = project.getConfigurations().maybeCreate(TEST_CONFIGURATION_NAME);
        testConfig.defaultDependencies(dependencies -> {
            dependencies.add(AutoSerializableDependencies.autoserializableJunit(project));
        });
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, config -> {
            config.extendsFrom(testConfig);
        });

        // run autoserializable task before compiling tests
        project.getTasks().named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).configure(task -> {
            task.dependsOn(autoSerializableClassesTask);
        });
        project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME).configure(task -> {
            task.dependsOn(autoSerializableClassesTask);
        });
    }
}
