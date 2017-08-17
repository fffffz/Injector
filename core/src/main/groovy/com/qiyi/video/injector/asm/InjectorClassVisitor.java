package com.qiyi.video.injector.asm;

import com.qiyi.video.injector.Configuration;
import com.qiyi.video.injector.TrackTarget;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by linjianjun on 2017/5/27.
 */
public class InjectorClassVisitor extends ClassVisitor {

    public static final String ACTIVITY = "android/app/Activity";
    public static final String FRAGMENT = "android/app/Fragment";
    public static final String V4FRAGMENT = "android/support/v4/app/Fragment";

    public static final String ON_RESUME = "onResume";
    public static final String ON_RESUME_DESC = "()V";
    public static final String ON_PAUSE = "onPause";
    public static final String ON_PAUSE_DESC = "()V";
    public static final String ON_DESTROY = "onDestroy";
    public static final String ON_DESTROY_DESC = "()V";

    public static final String WATCH = "watch";
    public static final String WATCH_DESC = "(Ljava/lang/Object;)V";
    public static final String TRACK = "track";
    public static final String TRACK_DESC = "(Ljava/lang/String;)V";

    final Configuration configuration;
    final ClassWriter cw;

    boolean superIsActivity;
    boolean superIsFragment;
    String superName;
    String className;
    boolean isActivityOnResumeHandled;
    boolean isActivityOnPauseHandled;
    boolean isFragmentOnDestroyHandled;
    boolean hasModified;

    static final int TYPE_TRACK = 0;
    static final int TYPE_WATCH = 1;

    public boolean hasModified() {
        return isActivityOnResumeHandled || isActivityOnPauseHandled || isFragmentOnDestroyHandled || hasModified;
    }

    public InjectorClassVisitor(ClassWriter cw, Configuration configuration) {
        super(Opcodes.ASM5, cw);
        this.cw = cw;
        this.configuration = configuration;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        superIsActivity = superName.equals(ACTIVITY);
        superIsFragment = superName.equals(FRAGMENT) || superName.equals(V4FRAGMENT);
        this.superName = superName;
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mw = cw.visitMethod(access, name, desc, signature, exceptions);
        if (superIsActivity) {
            if (configuration.trackActivity) {
                if (name.equals(ON_RESUME) && desc.equals(ON_RESUME_DESC)) {
                    injectTrack(mw, name);
                    isActivityOnResumeHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
                if (name.equals(ON_PAUSE) && desc.equals(ON_PAUSE_DESC)) {
                    injectTrack(mw, name);
                    isActivityOnPauseHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
            }
        } else if (superIsFragment) {
            if (name.equals(ON_DESTROY) && desc.equals(ON_DESTROY_DESC)) {
                if (configuration.watchFragment) {
                    injectWatch(mw);
                    isFragmentOnDestroyHandled = true;
                }
                return new InjectorMethodVisitor(mw, configuration);
            }
        }
        if (configuration.trackTargets != null) {
            for (TrackTarget trackTarget : configuration.trackTargets) {
                if ((trackTarget.className == null || trackTarget.className.trim().equals("") || trackTarget.className.equals(className)) &&
                        name.equals(trackTarget.methodName) && desc.equals(trackTarget.methodDesc)) {
                    injectTrackTarget(mw, trackTarget.inst);
                    hasModified = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
            }
        }
        return mw;
    }

    @Override
    public void visitEnd() {
        if (superIsActivity) {
            if (configuration.trackActivity) {
                if (!isActivityOnResumeHandled) {
                    injectSuper(Opcodes.ACC_PROTECTED, ON_RESUME, ON_RESUME_DESC, TYPE_TRACK);
                    isActivityOnResumeHandled = true;
                }
                if (!isActivityOnPauseHandled) {
                    injectSuper(Opcodes.ACC_PROTECTED, ON_PAUSE, ON_PAUSE_DESC, TYPE_TRACK);
                    isActivityOnPauseHandled = true;
                }
            }
        } else if (superIsFragment) {
            if (!isFragmentOnDestroyHandled && configuration.watchFragment) {
                injectSuper(Opcodes.ACC_PUBLIC, ON_DESTROY, ON_DESTROY_DESC, TYPE_WATCH);
                isFragmentOnDestroyHandled = true;
            }
        }
        super.visitEnd();
    }

    private void injectSuper(int access, String methodName, String methodDesc, int type) {
        MethodVisitor mw = cw.visitMethod(access, methodName, methodDesc, null, null);
        mw.visitCode();
        int instCount = 0;
        if (type == TYPE_TRACK) {
            injectTrack(mw, methodName);
            instCount++;
        } else if (type == TYPE_WATCH) {
            injectWatch(mw);
            instCount++;
        }
        injectSuper(mw, methodName, methodDesc, instCount);
        mw.visitEnd();
    }


    private void injectSuper(MethodVisitor mw, String methodName, String methodDesc, int instCount) {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, methodName, methodDesc, false);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(instCount, 1);
    }

    private void injectWatch(MethodVisitor mw) {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, configuration.leakCanaryClass, WATCH, WATCH_DESC, false);
    }

    private void injectTrack(MethodVisitor mw, String methodName) {
        mw.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mw.visitInsn(Opcodes.DUP);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        if (methodName.length() == ON_PAUSE.length()) {
            methodName = methodName + " ";
        }
        mw.visitLdcInsn(methodName + " ");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, configuration.trackClass, TRACK, TRACK_DESC, false);
    }

    private void injectTrackTarget(MethodVisitor mw, TrackTarget.Inst inst) {
        if (inst.argIndexes != null) {
            for (int argIndex : inst.argIndexes) {
                mw.visitVarInsn(Opcodes.ALOAD, argIndex);
            }
        }
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, inst.owner, inst.methodName, inst.methodDesc, false);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, configuration.trackClass, TRACK, TRACK_DESC, false);
    }

}