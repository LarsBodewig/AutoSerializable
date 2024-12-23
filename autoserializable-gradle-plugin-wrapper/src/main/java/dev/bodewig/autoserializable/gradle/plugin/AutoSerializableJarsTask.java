package dev.bodewig.autoserializable.gradle.plugin;

import dev.bodewig.autoserializable.AutoSerializablePlugin;
import net.bytebuddy.build.gradle.ByteBuddyJarsTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;

import java.io.File;

@CacheableTask
public class AutoSerializableJarsTask extends ByteBuddyJarsTask {

    public static final String TASK_NAME = "autoSerializableJars";
    public static final String AUTO_SERIALIZABLE_DIR_NAME = "autoSerializableJars";

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
