package com.fffz.injector.sample;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanaryAdapter.install(this);
        Tracker.install(this);
    }

}
