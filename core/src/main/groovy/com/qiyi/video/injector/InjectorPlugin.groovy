package com.qiyi.video.injector

import com.android.build.gradle.AppExtension
import com.qiyi.video.injector.util.Util
import org.gradle.api.Plugin
import org.gradle.api.Project

public class InjectorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Configuration configuration = project.extensions.create("injector", Configuration);
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new InjectorTransform(project, configuration))
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
        }
    }

}