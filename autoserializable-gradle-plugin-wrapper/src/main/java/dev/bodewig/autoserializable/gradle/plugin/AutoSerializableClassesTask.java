package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.AutoSerializablePlugin;
import net.bytebuddy.build.gradle.ByteBuddySimpleTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;

/**
 * Invokes the custom byte-buddy plugin AutoSerializable on compiled classes
 */
@CacheableTask
public class AutoSerializableClassesTask extends ByteBuddySimpleTask {

    /**
     * The default name of the task
     */
    public static final String TASK_NAME = "autoSerializableClasses";
    /**
     * The default name of the output directory
     */
    public static final String MOVED_CLASSES_DIR_NAME = "movedClasses";
    private final Provider<Directory> movedDir;
    private Provider<Directory> inPlace;

    /**
     * Initializes the output directory with the default value, sets a dummy value for the source and adds the
     * AutoSerializablePlugin
     */
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

    /**
     * Getter for inPlace
     * @return inPlace
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public Provider<Directory> getInPlace() {
        return inPlace;
    }

    /**
     * Sets the source and target directory for the byte-buddy transformation
     * @param inPlace The directory containing the compiled classes
     */
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
