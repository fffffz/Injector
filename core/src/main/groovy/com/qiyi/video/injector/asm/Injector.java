package com.qiyi.video.injector.asm;

import com.qiyi.video.injector.Configuration;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by linjianjun on 2017/5/18.
 */
public class Injector {

    private static boolean injected;

    public static boolean inject(File file, Configuration configuration) throws IOException {
        injected = false;
        injectInternal(file, configuration);
        return injected;
    }

    private static void injectInternal(File file, Configuration configuration) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                injectInternal(child, configuration);
            }
        } else if (file.getName().endsWith(".class")) {
            ClassReader cr = new ClassReader(new FileInputStream(file));
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            InjectorClassVisitor cv = new InjectorClassVisitor(cw, configuration);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            if (cv.hasModified()) {
                injected = true;
                System.out.println("-----injector----- inject " + file.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(cw.toByteArray());
                fos.close();
            }
        }
    }

}
