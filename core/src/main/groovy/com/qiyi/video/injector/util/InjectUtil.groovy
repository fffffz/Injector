package com.qiyi.video.injector.util

import com.qiyi.video.injector.Configuration
import com.qiyi.video.injector.asm.Injector
import org.gradle.api.Project

public class InjectUtil {

    public static void injectJar(Project project, File jar, Configuration configuration) {
        File tmpDir = new File(project.buildDir.absolutePath + "/com.qiyi.video.injector/" + jar.absolutePath.substring(project.buildDir.absolutePath.length()))
        FileUtil.delete(tmpDir)
        FileUtil.ensureDir(tmpDir)
        project.copy {
            from project.zipTree(jar)
            into tmpDir
        }
        if (Injector.inject(tmpDir, configuration)) {
            project.ant.zip(baseDir: tmpDir, destFile: jar)
        }
//        FileUtil.delete(tmpDir)
    }

}