package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;

public class AutoSerializableJarsGradlePlugin implements Plugin<Project> {

    public static final String AUTO_SERIALIZABLE_CONFIGURATION_NAME = "autoSerializable";
    public static final String NON_PRIVATE_CONFIGURATION_NAME = "nonPrivateConfig";
    public static final String SERIALIZERS_CONFIGURATION_NAME = "autoSerializers";
    public static final String API_DEPENDENCIES_CONFIGURATION_NAME = "autoserializableDeps";
    public static final String TEST_CONFIGURATION_NAME = "autoSerializableTest";

    public static Action<? super ConfigurableFileTree> jarFilter() {
        return files -> files.include("*.jar");
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        // create configuration to register dependencies to transform
        NamedDomainObjectProvider<Configuration> autoSerializableConfig =
                project.getConfigurations().register(AUTO_SERIALIZABLE_CONFIGURATION_NAME);

        // create task to download registered dependencies
        TaskProvider<PullJarsTask> pullJarsTask =
                project.getTasks().register(PullJarsTask.TASK_NAME, PullJarsTask.class, task -> {
                    task.setConfiguration(autoSerializableConfig);
                });

        // create task to make downloaded dependencies non private
        TaskProvider<NonPrivateTask> nonPrivateTask =
                project.getTasks().register(NonPrivateTask.TASK_NAME, NonPrivateTask.class, task -> {
                    task.setSource(pullJarsTask.map(PullJarsTask::getPulledDir).get().get().getAsFile());
                    task.setClassPath(autoSerializableConfig.get());
                    task.dependsOn(pullJarsTask);
                });

        // add nonprivate jars to compile configuration
        NamedDomainObjectProvider<Configuration> nonPrivateConfig =
                project.getConfigurations().register(NON_PRIVATE_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        Provider<File> nonPrivateOutput = nonPrivateTask.map(NonPrivateTask::getTarget);
                        Dependency nonPrivateJars =
                                project.getDependencies().create(project.fileTree(nonPrivateOutput, jarFilter()));
                        dependencies.add(nonPrivateJars);
                    });
                });
        Configuration compileClasspathConfig =
                project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
        compileClasspathConfig.extendsFrom(nonPrivateConfig.get());

        // run non private task before compiling
        TaskProvider<JavaCompile> compileTask =
                project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class, task -> {
                    task.dependsOn(nonPrivateTask);
                });

        // create jar from compiled classes to reference on autoSerializable classpath
        TaskProvider<PreAssembleJarTask> preAssembleJarTask =
                project.getTasks().register(PreAssembleJarTask.TASK_NAME, PreAssembleJarTask.class, task -> {
                    task.getClassFiles().set(compileTask.get().getDestinationDirectory());
                    task.dependsOn(compileTask);
                });

        // create configuration with compiled classes and non private libs to find serializers
        NamedDomainObjectProvider<Configuration> serializersConfig =
                project.getConfigurations().register(SERIALIZERS_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        DirectoryProperty preAssembleJarOutput = preAssembleJarTask.get().getDestinationDirectory();
                        Dependency compiledClasses =
                                project.getDependencies().create(project.fileTree(preAssembleJarOutput, jarFilter()));
                        dependencies.add(compiledClasses);

                        dependencies.add(AutoSerializableDependencies.autoserializableApi(project));
                    });
                    config.extendsFrom(compileClasspathConfig);
                });

        // create task to make downloaded dependencies serializable
        TaskProvider<AutoSerializableJarsTask> autoSerializableJarsTask = project.getTasks()
                .register(AutoSerializableJarsTask.TASK_NAME, AutoSerializableJarsTask.class, task -> {
                    task.setSource(pullJarsTask.map(PullJarsTask::getPulledDir).get().get().getAsFile());
                    task.setClassPath(serializersConfig.get());
                    task.dependsOn(preAssembleJarTask);
                });

        // add autoserializable jars and the autoserializable-api to api configuration
        NamedDomainObjectProvider<Configuration> apiConfig =
                project.getConfigurations().register(API_DEPENDENCIES_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        Dependency autoserializableApi = AutoSerializableDependencies.autoserializableApi(project);
                        dependencies.add(autoserializableApi);

                        Provider<File> autoSerializableOutput =
                                autoSerializableJarsTask.map(AutoSerializableJarsTask::getTarget);
                        Dependency transformedDir =
                                project.getDependencies().create(project.fileTree(autoSerializableOutput, jarFilter()));
                        dependencies.add(transformedDir);
                    });
                });
        project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME, config -> {
            config.extendsFrom(apiConfig.get());
        });

        // add autoserializable jars and autoserializable-junit to test configuration
        NamedDomainObjectProvider<Configuration> testConfig =
                project.getConfigurations().register(TEST_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        dependencies.add(AutoSerializableDependencies.autoserializableJunit(project));

                        Provider<File> autoSerializableOutput =
                                autoSerializableJarsTask.map(AutoSerializableJarsTask::getTarget);
                        Dependency transformedDir =
                                project.getDependencies().create(project.fileTree(autoSerializableOutput, jarFilter()));
                        dependencies.add(transformedDir);
                    });
                });
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, config -> {
            config.extendsFrom(testConfig.get());
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
