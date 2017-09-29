package com.qiyi.video.injector

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.qiyi.video.injector.util.FileUtil
import com.qiyi.video.injector.util.InjectUtil
import org.gradle.api.Project

public class InjectorTransform extends Transform {

    private Project project
    private Configuration configuration

    public InjectorTransform(Project project, Configuration configuration) {
        this.project = project
        this.configuration = configuration
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        for (TransformInput input : transformInvocation.inputs) {
            Collection<DirectoryInput> directoryInputs = input.directoryInputs
            if (directoryInputs != null) {
                for (DirectoryInput directoryInput : directoryInputs) {
                    File outDir = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    InjectUtil.injectDir(directoryInput.file, outDir, configuration)
                }
            }
            Collection<JarInput> jarInputs = input.jarInputs
            if (jarInputs != null) {
                for (JarInput jarInput : jarInputs) {
                    File outJar = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    InjectUtil.injectJar(project, jarInput.file, outJar, configuration)
                }
                FileUtil.delete(new File(project.buildDir.absolutePath + "/com.qiyi.video.injector"))
            }
        }
    }

    @Override
    String getName() {
        return "inject"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false
    }

}