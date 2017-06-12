package com.qiyi.video.injector.util;

import java.io.File;

public class FileUtil {

    public static void ensureDir(File file) {
        if (file == null || file.exists()) {
            return;
        }
        file.mkdirs();
    }

    public static void delete(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        } else {
            file.delete();
        }
    }

}
