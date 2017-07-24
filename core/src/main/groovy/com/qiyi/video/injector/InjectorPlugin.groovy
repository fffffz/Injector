package com.qiyi.video.injector

import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import com.android.build.gradle.internal.transforms.ProGuardTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import com.qiyi.video.injector.util.ReflectUtil

public class InjectorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Configuration configuration = project.extensions.create("injector", Configuration);
        project.afterEvaluate {
            if (configuration == null) {
                return
            }
            configuration.watchFragment = configuration.leakCanaryClass != null ? configuration.leakCanaryClass.length() > 0 : false
            configuration.trackActivity = configuration.trackClass != null ? configuration.trackClass.length() > 0 : false
            if (!configuration.watchFragment && !configuration.trackActivity) {
                return
            }
            if (configuration.watchFragment) {
                configuration.leakCanaryClass = configuration.leakCanaryClass.replaceAll("\\.", "/")
                System.out.println("-----injector----- configuration.leakCanaryClass=" + configuration.leakCanaryClass)
            }
            if (configuration.trackActivity) {
                configuration.trackClass = configuration.trackClass.replaceAll("\\.", "/")
                System.out.println("-----injector----- configuration.trackClass=" + configuration.trackClass)
            }
            project.extensions.android.applicationVariants.all { variant ->
                project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                    @Override
                    public void graphPopulated(TaskExecutionGraph taskGraph) {
                        for (Task task : taskGraph.allTasks) {
                            if (task instanceof TransformTask && task.project == project
                                    && (task.transform instanceof JarMergingTransform
                                    || task.transform instanceof ProGuardTransform
                                    || task.transform instanceof DexTransform)) {
                                //替换 Transform
                                InjectorTransform injectorTransform = new InjectorTransform(project, task.transform, configuration)
                                ReflectUtil.setField(task, 'transform', injectorTransform)
                                return
                            }
                        }
                    }
                });
            }
        }
    }

}