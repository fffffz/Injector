package com.qiyi.video.injector.util

import com.qiyi.video.injector.TrackTarget
import groovy.json.JsonSlurper

/**
 * Created by linjianjun on 2017/7/24.
 */
public class Util {

    public static List<TrackTarget> json2TrackTarget(String json) {
        def jsonList = new JsonSlurper().parseText(json)
        List<TrackTarget> trackTargets = new ArrayList<>()
        jsonList.each {
            it ->
                def jsonInst = it.inst
                TrackTarget.Inst inst = new TrackTarget.Inst(jsonInst.owner, jsonInst.methodName, jsonInst.methodDesc, jsonInst.argIndexes)
                TrackTarget trackTarget = new TrackTarget(it.methodName, it.methodDesc, inst)
                trackTargets.add(trackTarget)
        }
        return trackTargets
    }

}
