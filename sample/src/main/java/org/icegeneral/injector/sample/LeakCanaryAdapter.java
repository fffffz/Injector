package org.icegeneral.injector.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class LeakCanaryAdapter {

    private static RefWatcher sRefWatcher;

    public static void install(Application application) {
        if (LeakCanary.isInAnalyzerProcess(application)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        sRefWatcher = LeakCanary.install(application);
    }

    public static void watch(Object obj) {
        if (sRefWatcher != null) {
            sRefWatcher.watch(obj);
        }
    }

}
