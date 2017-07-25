package com.qiyi.video.injector

public class Configuration {

    public String leakCanaryClass;
    public boolean watchFragment;

    public String trackClass;
    public boolean trackActivity;

    public String trackTargetFile;
    public List<TrackTarget> trackTargets;

}