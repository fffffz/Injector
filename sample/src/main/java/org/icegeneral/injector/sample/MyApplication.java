package org.icegeneral.injector.sample;

import android.app.Application;

/**
 * Created by linjianjun on 2017/6/5.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanaryAdapter.install(this);
        Tracker.install(this);
    }

}
