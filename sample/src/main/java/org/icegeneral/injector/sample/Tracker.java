package org.icegeneral.injector.sample;

import android.app.Application;

/**
 * Created by linjianjun on 2017/6/9.
 */
public class Tracker {

    private static TrackWriter sTrackWriter;

    public static void install(Application application) {
        sTrackWriter = new TrackWriter(application);
    }

    public static void track(String log) {
        if (sTrackWriter != null) {
            sTrackWriter.write(log);
        }
    }

}
