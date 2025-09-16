package dev.bodewig.autoserializable.gradle.plugin.task;

import dev.bodewig.autoserializable.AutoSerializablePlugin;
import net.bytebuddy.build.gradle.ByteBuddyJarsTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;

import java.io.File;

/**
 * Invokes the custom byte-buddy plugin AutoSerializable on jars
 */
@CacheableTask
public class AutoSerializableJarsTask extends ByteBuddyJarsTask {

    /**
     * The default name of the task
     */
    public static final String TASK_NAME = "autoSerializableJars";

    /**
     * The default name of the output directory
     */
    public static final String AUTO_SERIALIZABLE_DIR_NAME = "autoSerializableJars";

    /**
     * Initializes the output directory with the default value and adds the AutoSerializablePlugin
     */
    public AutoSerializableJarsTask() {
        setTarget(getProject().getLayout().getBuildDirectory().dir(AUTO_SERIALIZABLE_DIR_NAME).get().getAsFile());
        transformation(tf -> tf.setPlugin(AutoSerializablePlugin.class));
    }

    @Override
    @Classpath
    public Iterable<File> getDiscoverySet() {
        return super.getDiscoverySet();
    }
}
