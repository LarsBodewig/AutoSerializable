package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.gradle.plugin.output.AutoSerializableJarsOutput;
import dev.bodewig.autoserializable.gradle.plugin.output.JavaClassesOutput;
import dev.bodewig.autoserializable.gradle.plugin.task.AutoSerializableJarsTask;
import dev.bodewig.autoserializable.gradle.plugin.task.NonPrivateTask;
import dev.bodewig.autoserializable.gradle.plugin.task.PreAssembleJarTask;
import dev.bodewig.autoserializable.gradle.plugin.task.PullJarsTask;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;

/**
 * Gradle plugin that invokes the custom byte-buddy plugin AutoSerializable on jars
 */
public class AutoSerializableJarsGradlePlugin implements Plugin<Project> {

    /**
     * The name of the configuration used for jars to transform with byte-buddy
     */
    public static final String AUTO_SERIALIZABLE_CONFIGURATION_NAME = "autoSerializable";
    /**
     * The name of the configuration used for custom serializer compilation
     */
    public static final String NON_PRIVATE_CONFIGURATION_NAME = "nonPrivateConfig";
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
    public AutoSerializableJarsGradlePlugin() {
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

        // register extensions for task output
        JavaClassesOutput javaClassesOutput = project.getExtensions().findByType(JavaClassesOutput.class) != null ?
                project.getExtensions().getByType(JavaClassesOutput.class) :
                project.getExtensions().create(JavaClassesOutput.NAME, JavaClassesOutput.class);
        AutoSerializableJarsOutput autoSerializableJarsOutput =
                project.getExtensions().create(AutoSerializableJarsOutput.NAME, AutoSerializableJarsOutput.class);

        // create configuration to register dependencies to transform
        Configuration autoSerializableConfig =
                project.getConfigurations().maybeCreate(AUTO_SERIALIZABLE_CONFIGURATION_NAME);

        // create task to download registered dependencies
        TaskProvider<PullJarsTask> pullJarsTask =
                project.getTasks().register(PullJarsTask.TASK_NAME, PullJarsTask.class, task -> {
                    task.setConfiguration(autoSerializableConfig);
                });

        // create task to make downloaded dependencies non private
        TaskProvider<NonPrivateTask> nonPrivateTask =
                project.getTasks().register(NonPrivateTask.TASK_NAME, NonPrivateTask.class, task -> {
                    task.setSource(pullJarsTask.map(PullJarsTask::getPulledDir).get().get().getAsFile());
                    task.setClassPath(autoSerializableConfig);
                    task.dependsOn(pullJarsTask);
                });

        // add nonprivate jars to compile configuration
        Configuration nonPrivateConfig = project.getConfigurations().maybeCreate(NON_PRIVATE_CONFIGURATION_NAME);
        nonPrivateConfig.defaultDependencies(dependencies -> {
            Provider<File> nonPrivateOutput = nonPrivateTask.map(NonPrivateTask::getTarget);
            Dependency nonPrivateJars =
                    project.getDependencies().create(project.fileTree(nonPrivateOutput, jarFilter()));
            dependencies.add(nonPrivateJars);
        });
        Configuration compileClasspathConfig =
                project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
        compileClasspathConfig.extendsFrom(nonPrivateConfig);

        // run non private task before compiling
        TaskProvider<JavaCompile> compileTask =
                project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);

        compileTask.configure(task -> {
            task.dependsOn(nonPrivateTask);
            task.doLast(t -> javaClassesOutput.getClassFiles().addAll(t.getOutputs().getFiles().getAsFileTree()));
        });

        // create jar from compiled classes to reference on autoSerializable classpath
        TaskProvider<PreAssembleJarTask> preAssembleJarTask =
                project.getTasks().register(PreAssembleJarTask.TASK_NAME, PreAssembleJarTask.class, task -> {
                    task.setClassFiles(compileTask.get().getDestinationDirectory());
                    task.dependsOn(compileTask);
                });

        // create configuration with compiled classes and non private libs to find serializers
        Configuration serializersConfig = project.getConfigurations().maybeCreate(SERIALIZERS_CONFIGURATION_NAME);
        serializersConfig.defaultDependencies(dependencies -> {
            DirectoryProperty preAssembleJarOutput = preAssembleJarTask.get().getDestinationDirectory();
            Dependency compiledClasses =
                    project.getDependencies().create(project.fileTree(preAssembleJarOutput, jarFilter()));
            dependencies.add(compiledClasses);

            dependencies.add(AutoSerializableDependencies.autoserializableApi(project));
        });
        serializersConfig.extendsFrom(compileClasspathConfig);

        // create task to make downloaded dependencies serializable
        TaskProvider<AutoSerializableJarsTask> autoSerializableJarsTask = project.getTasks()
                .register(AutoSerializableJarsTask.TASK_NAME, AutoSerializableJarsTask.class, task -> {
                    Directory pulledDir = pullJarsTask.map(PullJarsTask::getPulledDir).get().get();
                    task.setSource(pulledDir.getAsFile());
                    task.setClassPath(serializersConfig);
                    task.dependsOn(preAssembleJarTask);
                    autoSerializableJarsOutput.getJarFiles().addAll(pulledDir.getAsFileTree().getFiles());
                });

        // add autoserializable jars and the autoserializable-api to api configuration
        Configuration apiConfig = project.getConfigurations().maybeCreate(API_DEPENDENCIES_CONFIGURATION_NAME);
        apiConfig.defaultDependencies(dependencies -> {
            Dependency autoserializableApi = AutoSerializableDependencies.autoserializableApi(project);
            dependencies.add(autoserializableApi);

            Provider<File> autoSerializableOutput = autoSerializableJarsTask.map(AutoSerializableJarsTask::getTarget);
            Dependency transformedDir =
                    project.getDependencies().create(project.fileTree(autoSerializableOutput, jarFilter()));
            dependencies.add(transformedDir);
        });
        project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME, config -> {
            config.extendsFrom(apiConfig);
        });

        // add autoserializable jars and autoserializable-junit to test configuration
        Configuration testConfig = project.getConfigurations().maybeCreate(TEST_CONFIGURATION_NAME);
        testConfig.defaultDependencies(dependencies -> {
            dependencies.add(AutoSerializableDependencies.autoserializableJunit(project));

            Provider<File> autoSerializableOutput = autoSerializableJarsTask.map(AutoSerializableJarsTask::getTarget);
            Dependency transformedDir =
                    project.getDependencies().create(project.fileTree(autoSerializableOutput, jarFilter()));
            dependencies.add(transformedDir);
        });
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, config -> {
            config.extendsFrom(testConfig);
        });

        // run autoserializable task before compiling tests
        project.getTasks().named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).configure(task -> {
            task.dependsOn(autoSerializableJarsTask);
        });
        project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME).configure(task -> {
            task.dependsOn(autoSerializableJarsTask);
        });
    }
}
