package com.qiyi.video.injector

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform

public class TransformProxy extends Transform {
    protected Transform base

    public TransformProxy(Transform base) {
        this.base = base
    }

    @Override
    String getName() {
        return base.getName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return base.getInputTypes()
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return base.getScopes()
    }

    @Override
    boolean isIncremental() {
        return base.isIncremental()
    }

}