package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.AutoSerializablePlugin;
import net.bytebuddy.build.gradle.ByteBuddySimpleTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;

@CacheableTask
public class AutoSerializableClassesTask extends ByteBuddySimpleTask {

    public static final String TASK_NAME = "autoSerializableClasses";
    public static final String MOVED_CLASSES_DIR_NAME = "movedClasses";
    private final Provider<Directory> movedDir;
    private Provider<Directory> inPlace;

    public AutoSerializableClassesTask() {
        movedDir = getProject().getLayout().getBuildDirectory().dir(MOVED_CLASSES_DIR_NAME);
        setSource(getProject().getLayout().getBuildDirectory().get().getAsFile());
        transformation(tf -> tf.setPlugin(AutoSerializablePlugin.class));
    }

    @Override
    @Classpath
    public Iterable<File> getDiscoverySet() {
        return super.getDiscoverySet();
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public Provider<Directory> getInPlace() {
        return inPlace;
    }

    public void setInPlace(Provider<Directory> inPlace) {
        this.inPlace = inPlace;
        setTarget(inPlace.get().getAsFile());
    }

    @Override
    public void apply() throws IOException {
        getProject().copy(spec -> {
            spec.from(inPlace);
            spec.into(movedDir);
        });
        setSource(movedDir.get().getAsFile());
        super.apply();
    }
}
