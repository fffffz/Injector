package com.qiyi.video.injector

import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import com.android.build.gradle.internal.transforms.ProGuardTransform
import com.qiyi.video.injector.util.Util
import groovy.json.JsonSlurper
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
            configuration.watchFragment = configuration.leakCanaryClass != null && configuration.leakCanaryClass.length() > 0 && configuration.watchFragment
            configuration.trackActivity = configuration.trackClass != null && configuration.trackClass.length() > 0 && configuration.trackActivity
            if (configuration.trackTargetFile != null && configuration.trackTargetFile.length() > 0) {
                System.out.println("-----injector----- configuration.trackTargetFile=" + configuration.trackTargetFile)
                configuration.trackTargets = Util.json2TrackTarget(new File(configuration.trackTargetFile).text)
                System.out.println("-----injector----- configuration.trackTargets=" + configuration.trackTargets)
            }

            if (!configuration.watchFragment && !configuration.trackActivity && configuration.trackTargets?.isEmpty()) {
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
            boolean hasInjected = false
            project.extensions.android.applicationVariants.all { variant ->
                project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                    @Override
                    public void graphPopulated(TaskExecutionGraph taskGraph) {
                        if (hasInjected) {
                            return
                        }
                        for (Task task : taskGraph.allTasks) {
                            if (task instanceof TransformTask && task.project == project
                                    && (task.transform instanceof JarMergingTransform
                                    || task.transform instanceof ProGuardTransform
                                    || task.transform instanceof DexTransform)) {
                                hasInjected = true
                                //替换 Transform
                                InjectorTransform injectorTransform = new InjectorTransform(project, task.transform, configuration)
                                ReflectUtil.setField(task, 'transform', injectorTransform)
                                return
                            }
                        }
                    }
                })
            }
        }
    }

}