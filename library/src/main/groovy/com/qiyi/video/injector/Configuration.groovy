package com.qiyi.video.injector

class Configuration {

    public String leakCanaryClass;
    public boolean watchFragment;

    public String trackClass;
    public boolean trackActivity;

    public String trackTargetFile;
    public List<TrackTarget> trackTargets;

}