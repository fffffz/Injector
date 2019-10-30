package com.qiyi.video.injector.asm;

import com.qiyi.video.injector.Configuration;
import com.qiyi.video.injector.TrackTarget;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectorMethodVisitor extends MethodVisitor {

    private Configuration configuration;

    public InjectorMethodVisitor(MethodVisitor mw, Configuration configuration) {
        super(Opcodes.ASM5, mw);
        this.configuration = configuration;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (owner.equals(configuration.leakCanaryClass) && name.equals(InjectorClassVisitor.WATCH)) {
            return;
        }
        if (owner.equals(configuration.trackClass) && name.equals(InjectorClassVisitor.TRACK)) {
            return;
        }
        if (configuration.trackTargets != null) {
            for (TrackTarget trackTarget : configuration.trackTargets) {
                if (owner.equals(trackTarget.inst.owner) && name.equals(trackTarget.inst.methodName)) {
                    return;
                }
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

}
