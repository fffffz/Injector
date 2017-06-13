package com.qiyi.video.injector

import com.android.build.api.transform.*
import com.qiyi.video.injector.util.FileUtil
import org.gradle.api.Project
import com.qiyi.video.injector.util.InjectUtil

public class InjectorTransform extends TransformProxy {

    private Project project
    private Configuration configuration

    public InjectorTransform(Project project, Transform base, Configuration configuration) {
        super(base)
        this.project = project
        this.configuration = configuration
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        for (TransformInput input : transformInvocation.inputs) {
            Collection<DirectoryInput> directoryInputs = input.directoryInputs
            if (directoryInputs != null) {
                for (DirectoryInput directoryInput : directoryInputs) {
                    InjectUtil.injectDir(directoryInput.file, configuration)
                }
            }
            Collection<JarInput> jarInputs = input.jarInputs
            if (jarInputs != null) {
                for (JarInput jarInput : jarInputs) {
                    /**
                     * 只扫描 build 目录下的 jar
                     */
                    if (jarInput.file.absolutePath.startsWith(project.buildDir.absolutePath)) {
                        InjectUtil.injectJar(project, jarInput.file, configuration)
                    }
                }
                FileUtil.delete(new File(project.buildDir.absolutePath + "/com.qiyi.video.injector"))
            }
        }
        base.transform(transformInvocation);
    }

}