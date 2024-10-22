A precompiled gradle plugin should contain the following parts:

```java
// create configuration to register dependencies to transform
project.getConfigurations().create(CONFIGURATION_NAME);

// create task to download registered dependencies
project.getTasks().register(PULL_TASK_NAME, Copy.class, t -> {
    t.from(project.getConfigurations().named(CONFIGURATION_NAME));
    t.into(project.getLayout().getBuildDirectory().dir(PULLED_DIR_NAME));
});

// create task to run bytebuddy on downloaded dependencies
project.getTasks().register(BYTEBUDDY_TASK_NAME, ByteBuddyJarsTask.class, t -> {
    Directory buildDir = project.getLayout().getBuildDirectory().get();
    t.setSource(buildDir.dir(PULLED_DIR_NAME).getAsFile());
    t.setTarget(buildDir.dir(TRANSFORMED_DIR_NAME).getAsFile());
    t.transformation(tf -> tf.setPlugin(AutoSerializablePlugin.class));
    t.setClassPath(project.getConfigurations().getByName(CONFIGURATION_NAME));
    t.dependsOn(project.getTasks().named(PULL_TASK_NAME));
});

// execute bytebuddy before compiling to access types and fields that would otherwise be private
project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME).configure(t -> {
    t.dependsOn(project.getTasks().named("byteBuddyJars"));
});

// inject dependencies to the transformed dependencies and the autoserializable-api into project
Configuration depsConfig = project.getConfigurations().create("autoserializableDeps");
depsConfig.defaultDependencies(dependencySet -> {
    Dependency autoserializableApi = project.getDependencies()
            .create("dev.bodewig.autoserializable:autoserializable-api:2.0.0-SNAPSHOT");
    dependencySet.add(autoserializableApi);

    Dependency transformedDir = project.getDependencies()
            .create(project.fileTree(
                    project.getLayout().getBuildDirectory().dir(TRANSFORMED_DIR_NAME),
                    ft -> ft.include("*.jar")));
    dependencySet.add(transformedDir);
});
Configuration apiConfig = project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME);
apiConfig.extendsFrom(depsConfig);
```
