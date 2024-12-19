package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.AutoSerializablePlugin;
import dev.bodewig.autoserializable.NonPrivatePlugin;
import net.bytebuddy.build.gradle.ByteBuddyJarsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.jvm.tasks.Jar;

public class AutoSerializableGradlePlugin implements Plugin<Project> {

    public static final String GROUP_ID = "dev.bodewig.autoserializable";
    public static final String API_ARTIFACT_ID = "autoserializable-api";
    public static final String JUNIT_ARTIFACT_ID = "autoserializable-junit";
    public static final String AUTO_SERIALIZABLE_VERSION = "2.0.0";

    public static final String PULL_TASK_NAME = "pullJars";
    public static final String NON_PRIVATE_TASK_NAME = "byteBuddyNonPrivate";
    public static final String AUTO_SERIALIZABLE_TASK_NAME = "byteBuddyAutoSerializable";
    public static final String TEMP_COMPILE_JAR = "tempCompileJar";

    public static final String NON_PRIVATE_CONFIGURATION_NAME = "nonPrivateConfig";
    public static final String AUTO_SERIALIZABLE_CONFIGURATION_NAME = "autoserializable";
    public static final String SERIALIZERS_CONFIGURATION_NAME = "autoSerializers";
    public static final String API_DEPENDENCIES_CONFIGURATION_NAME = "autoserializableDeps";
    public static final String TEST_CONFIGURATION_NAME = "autoSerializableTest";

    public static final String PULLED_DIR_NAME = "pulledJars";
    public static final String NON_PRIVATE_DIR_NAME = "nonPrivateJars";
    public static final String AUTO_SERIALIZABLE_DIR_NAME = "autoSerializableJars";
    public static final String TEMP_COMPILE_JAR_DIR_NAME = "tempCompileJar";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        // create configuration to register dependencies to transform
        project.getConfigurations().create(AUTO_SERIALIZABLE_CONFIGURATION_NAME);

        // create task to download registered dependencies
        project.getTasks().register(PULL_TASK_NAME, Copy.class, task -> {
            task.getInputs().files(project.getConfigurations().named(AUTO_SERIALIZABLE_CONFIGURATION_NAME));
            task.from(project.getConfigurations().named(AUTO_SERIALIZABLE_CONFIGURATION_NAME));
            task.into(project.getLayout().getBuildDirectory().dir(PULLED_DIR_NAME));
        });

        // create task to make downloaded dependencies non private
        project.getTasks().register(NON_PRIVATE_TASK_NAME, ByteBuddyJarsTask.class, task -> {
            Directory buildDir = project.getLayout().getBuildDirectory().get();
            task.getInputs().dir(buildDir.dir(PULLED_DIR_NAME));
            task.setSource(buildDir.dir(PULLED_DIR_NAME).getAsFile());
            task.setTarget(buildDir.dir(NON_PRIVATE_DIR_NAME).getAsFile());
            task.transformation(tf -> tf.setPlugin(NonPrivatePlugin.class));
            task.setClassPath(project.getConfigurations().getByName(AUTO_SERIALIZABLE_CONFIGURATION_NAME));
            task.dependsOn(project.getTasks().named(PULL_TASK_NAME));
        });

        // add nonprivate jars to compile configuration
        project.getConfigurations().create(NON_PRIVATE_CONFIGURATION_NAME, config -> {
            config.defaultDependencies(dependencies -> {
                Dependency nonPrivateJars = project.getDependencies()
                        .create(project.fileTree(
                                project.getLayout().getBuildDirectory().dir(NON_PRIVATE_DIR_NAME),
                                files -> files.include("*.jar")));
                dependencies.add(nonPrivateJars);
            });
        });
        project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME, config -> {
            config.extendsFrom(project.getConfigurations().getByName(NON_PRIVATE_CONFIGURATION_NAME));
        });

        // run non private task before compiling
        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME).configure(task -> {
            task.dependsOn(project.getTasks().named(NON_PRIVATE_TASK_NAME));
        });

        // create jar from compiled classes to reference on autoSerializable classpath
        project.getTasks().register(TEMP_COMPILE_JAR, Jar.class, task -> {
            task.getInputs()
                    .files(project.getTasks()
                            .getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME)
                            .getOutputs()
                            .getFiles());
            task.from(project.fileTree(
                    project.getLayout().getBuildDirectory().dir(LibraryElements.CLASSES + "/java/main"),
                    files -> files.include("**/*.class")));
            task.getDestinationDirectory()
                    .set(project.getLayout().getBuildDirectory().dir(TEMP_COMPILE_JAR_DIR_NAME));
            task.dependsOn(project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME));
        });

        // create configuration with compiled classes and libs to bind serializers
        project.getConfigurations().create(SERIALIZERS_CONFIGURATION_NAME, config -> {
            config.defaultDependencies(dependencies -> {
                Dependency compiledClasses = project.getDependencies()
                        .create(project.fileTree(
                                project.getLayout().getBuildDirectory().dir(TEMP_COMPILE_JAR_DIR_NAME),
                                files -> files.include("*.jar")));
                dependencies.add(compiledClasses);

                Dependency autoserializableApi = project.getDependencies()
                        .create(GROUP_ID + ":" + API_ARTIFACT_ID + ":" + AUTO_SERIALIZABLE_VERSION);
                dependencies.add(autoserializableApi);
            });
            config.extendsFrom(project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME));
        });

        // create task to make downloaded dependencies serializable
        project.getTasks().register(AUTO_SERIALIZABLE_TASK_NAME, ByteBuddyJarsTask.class, task -> {
            Directory buildDir = project.getLayout().getBuildDirectory().get();
            task.getInputs().dir(buildDir.dir(PULLED_DIR_NAME));
            task.getInputs()
                    .files(project.getTasks()
                            .getByName(TEMP_COMPILE_JAR)
                            .getOutputs()
                            .getFiles());
            task.setSource(buildDir.dir(PULLED_DIR_NAME).getAsFile());
            task.setTarget(buildDir.dir(AUTO_SERIALIZABLE_DIR_NAME).getAsFile());
            task.transformation(tf -> tf.setPlugin(AutoSerializablePlugin.class));
            task.setClassPath(project.getConfigurations().getByName(SERIALIZERS_CONFIGURATION_NAME));
            task.dependsOn(project.getTasks().named(TEMP_COMPILE_JAR));
            /*task.doFirst(t -> project.getConfigurations()
            .getByName(SERIALIZERS_CONFIGURATION_NAME)
            .getFiles()
            .forEach(System.out::println));*/
        });

        // run autoserializable task before compiling tests
        project.getTasks().named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).configure(task -> {
            task.dependsOn(project.getTasks().named(AUTO_SERIALIZABLE_TASK_NAME));
        });
        project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME).configure(task -> {
            task.dependsOn(project.getTasks().named(AUTO_SERIALIZABLE_TASK_NAME));
        });

        // add autoserializable jars and the autoserializable-api to api configuration
        project.getConfigurations().create(API_DEPENDENCIES_CONFIGURATION_NAME, config -> {
            config.defaultDependencies(dependencies -> {
                Dependency autoserializableApi = project.getDependencies()
                        .create(GROUP_ID + ":" + API_ARTIFACT_ID + ":" + AUTO_SERIALIZABLE_VERSION);
                dependencies.add(autoserializableApi);

                Dependency transformedDir = project.getDependencies()
                        .create(project.fileTree(
                                project.getLayout().getBuildDirectory().dir(AUTO_SERIALIZABLE_DIR_NAME),
                                files -> files.include("*.jar")));
                dependencies.add(transformedDir);
            });
        });
        project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME, config -> {
            config.extendsFrom(project.getConfigurations().getByName(API_DEPENDENCIES_CONFIGURATION_NAME));
        });

        // add autoserializable jars and autoserializable-junit to test configuration
        project.getConfigurations().create(TEST_CONFIGURATION_NAME, config -> {
            config.defaultDependencies(dependencies -> {
                Dependency autoserializableJunit = project.getDependencies()
                        .create(GROUP_ID + ":" + JUNIT_ARTIFACT_ID + ":" + AUTO_SERIALIZABLE_VERSION);
                dependencies.add(autoserializableJunit);

                Dependency transformedDir = project.getDependencies()
                        .create(project.fileTree(
                                project.getLayout().getBuildDirectory().dir(AUTO_SERIALIZABLE_DIR_NAME),
                                files -> files.include("*.jar")));
                dependencies.add(transformedDir);
            });
        });
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, config -> {
            config.extendsFrom(project.getConfigurations().getByName(TEST_CONFIGURATION_NAME));
        });
    }
}
