package com.qiyi.video.injector;

import java.util.List;

/**
 * Created by linjianjun on 2017/7/24.
 */
public class TrackTarget {
    public final String methodName;
    public final String methodDesc;
    public final Inst inst;

    public TrackTarget(String methodName, String methodDesc, Inst inst) {
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.inst = inst;
    }

    public static class Inst {
        public final String owner;
        public final String methodName;
        public final String methodDesc;
        public final List<Integer> argIndexes;

        public Inst(String owner, String methodName, String methodDesc, List<Integer> argIndexes) {
            this.owner = owner;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.argIndexes = argIndexes;
        }
    }
}
