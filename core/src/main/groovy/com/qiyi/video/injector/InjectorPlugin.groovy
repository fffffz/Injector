package com.qiyi.video.injector

import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import com.qiyi.video.injector.util.ReflectUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener

public class InjectorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Configuration configuration = project.extensions.create("injector", Configuration);
        project.afterEvaluate {
            if (configuration == null) {
                return
            }
            configuration.leakCanary = configuration.leakCanaryClass != null ? configuration.leakCanaryClass.length() > 0 : false
            configuration.track = configuration.trackClass != null ? configuration.trackClass.length() > 0 : false
            if (!configuration.leakCanary && !configuration.track) {
                return
            }
            if (configuration.leakCanary) {
                configuration.leakCanaryClass = configuration.leakCanaryClass.replaceAll("\\.", "/")
                System.out.println("-----injector----- configuration.leakCanaryClass=" + configuration.leakCanaryClass)
            }
            if (configuration.track) {
                configuration.trackClass = configuration.trackClass.replaceAll("\\.", "/")
                System.out.println("-----injector----- configuration.trackClass=" + configuration.trackClass)
            }
            project.extensions.android.applicationVariants.all { variant ->
                boolean hasInjected;
                project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                    @Override
                    public void graphPopulated(TaskExecutionGraph taskGraph) {
                        for (Task task : taskGraph.allTasks) {
                            if (task.project == project
                                    && task instanceof TransformTask
                                    && task.name.toLowerCase().contains(variant.name.toLowerCase())) {
                                Transform transform = task.transform
                                if (!hasInjected &&
                                        (transform instanceof JarMergingTransform || transform instanceof DexTransform)
                                        && !(transform instanceof InjectorTransform)) {
                                    hasInjected = true
                                    //替换 Transform
                                    InjectorTransform injectorTransform = new InjectorTransform(project, transform, configuration)
                                    ReflectUtil.setField(task, 'transform', injectorTransform)
                                }
                            }
                        }
                    }
                });
            }
        }
    }

}