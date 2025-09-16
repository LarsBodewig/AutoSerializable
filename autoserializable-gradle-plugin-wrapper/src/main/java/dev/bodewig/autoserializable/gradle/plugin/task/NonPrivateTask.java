package dev.bodewig.autoserializable.gradle.plugin.task;

import dev.bodewig.autoserializable.NonPrivatePlugin;
import net.bytebuddy.build.gradle.ByteBuddyJarsTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;

import java.io.File;

/**
 * Invokes the custom byte-buddy plugin NonPrivate on jars
 */
@CacheableTask
public class NonPrivateTask extends ByteBuddyJarsTask {

    /**
     * The default name of the task
     */
    public static final String TASK_NAME = "byteBuddyNonPrivate";

    /**
     * The default name of the output directory
     */
    public static final String NON_PRIVATE_DIR_NAME = "nonPrivateJars";

    /**
     * Initializes the output directory with the default value and adds the NonPrivatePlugin
     */
    public NonPrivateTask() {
        setTarget(getProject().getLayout().getBuildDirectory().dir(NON_PRIVATE_DIR_NAME).get().getAsFile());
        transformation(tf -> tf.setPlugin(NonPrivatePlugin.class));
    }

    @Override
    @Classpath
    public Iterable<File> getDiscoverySet() {
        return super.getDiscoverySet();
    }
}
