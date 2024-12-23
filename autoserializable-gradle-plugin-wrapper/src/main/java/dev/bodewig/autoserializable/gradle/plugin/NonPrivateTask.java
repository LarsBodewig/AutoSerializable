package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.NonPrivatePlugin;
import net.bytebuddy.build.gradle.ByteBuddyJarsTask;
import org.gradle.api.tasks.*;

import java.io.File;

@CacheableTask
public class NonPrivateTask extends ByteBuddyJarsTask {

    public static final String TASK_NAME = "byteBuddyNonPrivate";
    public static final String NON_PRIVATE_DIR_NAME = "nonPrivateJars";

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
