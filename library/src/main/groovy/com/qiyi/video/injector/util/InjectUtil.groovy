package com.qiyi.video.injector.util

import android.ZipUtil
import com.qiyi.video.injector.Configuration
import com.qiyi.video.injector.asm.InjectorClassVisitor
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

public class InjectUtil {

    public static void injectJar(Project project, File jar, File outJar, Configuration configuration) throws IOException {
        File tmpDir = new File(project.buildDir.absolutePath + "/com.qiyi.video.injector/" + jar.absolutePath.substring(project.buildDir.absolutePath.length()))
        FileUtil.delete(tmpDir);
        ZipFile zipFile = new ZipFile(jar);
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        boolean hasModified = false;
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            String name = zipEntry.getName();
            if (name.endsWith(".class")) {
                InputStream is = zipFile.getInputStream(zipEntry);
                if (injectClass(is, new File(tmpDir, name), configuration)) {
                    hasModified = true;
                    System.out.println("-----injector----- inject " + jar.absolutePath + "/" + name);
                }
            }
        }
        outJar.parentFile.mkdirs()
        if (hasModified) {
            ZipUtil.extractZip(zipFile, tmpDir, false);
            project.ant.zip(baseDir: tmpDir, destFile: outJar)
            FileUtil.delete(tmpDir);
        } else {
            android.FileUtil.copyFile(jar, outJar);
        }
    }

    public static void injectDir(File file, File outFile, Configuration configuration) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (child.isDirectory()) {
                    injectDir(child, new File(outFile, child.getName()), configuration);
                } else {
                    injectClass(child, new File(outFile, child.getName()), configuration);
                }
            }
        } else {
            injectClass(file, new File(outFile, file.getName()), configuration);
        }
    }

    public static boolean injectClass(File inputFile, File outFile, Configuration configuration) throws IOException {
        if (inputFile == null || !inputFile.exists() || inputFile.isDirectory() || !inputFile.getName().endsWith(".class")) {
            return false;
        }
        boolean hasModified = injectClass(new FileInputStream(inputFile), outFile, configuration);
        if (hasModified) {
            System.out.println("-----injector----- inject " + inputFile.getAbsolutePath());
        }
        return hasModified;
    }

    public static boolean injectClass(InputStream inputStream, File outFile, Configuration configuration) {
        try {
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            InjectorClassVisitor cv = new InjectorClassVisitor(cw, configuration);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            outFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(cw.toByteArray());
            fos.close();
            return cv.hasModified();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}