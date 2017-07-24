package com.qiyi.video.injector.asm;

import com.qiyi.video.injector.Configuration;

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

    private final Configuration configuration;
    private final ClassWriter cw;

    private boolean superIsActivity;
    private boolean superIsFragment;
    private String superName;
    private boolean isActivityOnResumeHandled;
    private boolean isActivityOnPauseHandled;
    private boolean isActivityOnDestroyHandled;
    private boolean isFragmentOnDestroyHandled;

    private static final int TYPE_TRACK = 0;
    private static final int TYPE_WATCH = 1;

    public boolean hasModified() {
        return isActivityOnResumeHandled || isActivityOnDestroyHandled || isActivityOnPauseHandled || isFragmentOnDestroyHandled;
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
                if (name.equals(ON_DESTROY) && desc.equals(ON_DESTROY_DESC)) {
                    injectTrack(mw, name);
                    isActivityOnDestroyHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
            }
        } else if (superIsFragment) {
            if (name.equals(ON_DESTROY) && desc.equals(ON_DESTROY_DESC)) {
                if (!configuration.watchFragment) {
                    injectWatch(mw);
                    isFragmentOnDestroyHandled = true;
                }
                return new InjectorMethodVisitor(mw, configuration);
            }
        }
        return mw;
    }

    @Override
    public void visitEnd() {
        if (superIsActivity) {
            if (configuration.trackActivity) {
                if (!isActivityOnResumeHandled) {
                    injectSuper(ON_RESUME, ON_RESUME_DESC, TYPE_TRACK);
                    isActivityOnResumeHandled = true;
                }
                if (!isActivityOnPauseHandled) {
                    injectSuper(ON_PAUSE, ON_PAUSE_DESC, TYPE_TRACK);
                    isActivityOnPauseHandled = true;
                }
                if (!isActivityOnDestroyHandled) {
                    injectSuper(ON_DESTROY, ON_DESTROY_DESC, TYPE_TRACK);
                    isActivityOnDestroyHandled = true;
                }
            }
        } else if (superIsFragment) {
            if (!isFragmentOnDestroyHandled && configuration.watchFragment) {
                injectSuper(ON_DESTROY, ON_DESTROY_DESC, TYPE_WATCH);
                isFragmentOnDestroyHandled = true;
            }
        }
        super.visitEnd();
    }

    private void injectSuper(String methodName, String methodDesc, int type) {
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
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
        if (methodName.length() == ON_RESUME.length()) {
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

}