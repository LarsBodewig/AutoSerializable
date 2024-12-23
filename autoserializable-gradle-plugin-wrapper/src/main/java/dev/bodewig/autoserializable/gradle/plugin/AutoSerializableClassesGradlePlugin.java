package dev.bodewig.autoserializable.gradle.plugin;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

public class AutoSerializableClassesGradlePlugin implements Plugin<Project> {

    public static final String SERIALIZERS_CONFIGURATION_NAME = "autoSerializers";
    public static final String TEST_CONFIGURATION_NAME = "autoSerializableTest";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        // create configuration with compiled classes and non private libs to find serializers
        NamedDomainObjectProvider<Configuration> serializersConfig =
                project.getConfigurations().register(SERIALIZERS_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        dependencies.add(AutoSerializableDependencies.autoserializableApi(project));
                    });
                    Configuration compileClasspathConfig =
                            project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
                    config.extendsFrom(compileClasspathConfig);
                });

        // create task to make downloaded dependencies serializable
        TaskProvider<AutoSerializableClassesTask> autoSerializableClassesTask = project.getTasks()
                .register(AutoSerializableClassesTask.TASK_NAME, AutoSerializableClassesTask.class, task -> {
                    TaskProvider<JavaCompile> compileTask =
                            project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
                    task.setInPlace(compileTask.get().getDestinationDirectory());
                    task.setClassPath(serializersConfig.get());
                    task.dependsOn(compileTask);
                });

        // add autoserializable jars and autoserializable-junit to test configuration
        NamedDomainObjectProvider<Configuration> testConfig =
                project.getConfigurations().register(TEST_CONFIGURATION_NAME, config -> {
                    config.defaultDependencies(dependencies -> {
                        dependencies.add(AutoSerializableDependencies.autoserializableJunit(project));
                    });
                });
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, config -> {
            config.extendsFrom(testConfig.get());
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
