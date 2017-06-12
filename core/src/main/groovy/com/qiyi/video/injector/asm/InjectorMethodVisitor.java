package com.qiyi.video.injector.asm;

import com.qiyi.video.injector.Configuration;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by linjianjun on 2017/5/31.
 */
public class InjectorMethodVisitor extends MethodVisitor {

    public final Configuration configuration;

    public InjectorMethodVisitor(MethodVisitor mw, Configuration configuration) {
        super(Opcodes.ASM5, mw);
        this.configuration = configuration;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (owner.equals(configuration.leakCanaryClass) && name.equals(InjectorClassVisitor.WATCH)) {
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

}
