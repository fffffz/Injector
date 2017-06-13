package com.qiyi.video.injector.util;

import android.ZipUtil;

import com.qiyi.video.injector.Configuration;
import com.qiyi.video.injector.asm.InjectorClassVisitor;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InjectUtil {

    public static void injectJar(Project project, File jar, Configuration configuration) throws IOException {
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
        if (!hasModified) {
            return;
        }
        ZipUtil.extractZip(zipFile, tmpDir, false);
        project.ant.zip(baseDir:tmpDir, destFile:jar)
        FileUtil.delete(tmpDir);
    }

    public static void injectDir(File file, Configuration configuration) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (child.isDirectory()) {
                    injectDir(child, configuration);
                } else {
                    injectClass(child, child, configuration);
                }
            }
        } else {
            injectClass(file, file, configuration);
        }
    }

    public static boolean injectClass(File inputFile, File outputFile, Configuration configuration) throws IOException {
        if (inputFile == null || !inputFile.exists() || inputFile.isDirectory() || !inputFile.getName().endsWith(".class") || outputFile == null) {
            return false;
        }
        boolean hasModified = injectClass(new FileInputStream(inputFile), outputFile, configuration);
        if (hasModified) {
            System.out.println("-----injector----- inject " + inputFile.getAbsolutePath());
        }
        return hasModified;
    }

    public static boolean injectClass(InputStream inputStream, File outputFile, Configuration configuration) {
        try {
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            InjectorClassVisitor cv = new InjectorClassVisitor(cw, configuration);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            if (cv.hasModified()) {
                outputFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(cw.toByteArray());
                fos.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}