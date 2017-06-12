package com.qiyi.video.injector.asm

import com.qiyi.video.injector.Configuration
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by linjianjun on 2017/5/27.
 */
public class InjectorClassVisitor extends ClassVisitor {

    public static final String ACTIVITY = "android/app/Activity";
    public static final String FRAGMENT = "android/app/Fragment";
    public static final String V4FRAGMENT = "android/support/v4/app/Fragment";
    public static final String ON_CREATE = "onCreate";
    public static final String ON_CREATE_DESC = "(Landroid/os/Bundle;)V";
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
    private boolean isActivityOnCreateHandled;
    private boolean isActivityOnDestroyHandled;
    private boolean isFragmentOnCreateHandled;
    private boolean isFragmentOnDestroyHandled;

    public boolean hasModified() {
        return isActivityOnCreateHandled || isActivityOnDestroyHandled || isFragmentOnCreateHandled || isFragmentOnDestroyHandled;
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
            if (configuration.track) {
                if (name.equals(ON_CREATE) && desc.equals(ON_CREATE_DESC)) {
                    injectTrack(mw, name);
                    isActivityOnCreateHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
                if (name.equals(ON_DESTROY) && desc.equals(ON_DESTROY_DESC)) {
                    injectTrack(mw, name);
                    isActivityOnDestroyHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
            }
        } else if (superIsFragment) {
            if (name.equals(ON_CREATE) && desc.equals(ON_CREATE_DESC)) {
                if (configuration.track) {
                    injectTrack(mw, name);
                    isFragmentOnCreateHandled = true;
                    return new InjectorMethodVisitor(mw, configuration);
                }
            } else if (name.equals(ON_DESTROY) && desc.equals(ON_DESTROY_DESC)) {
                if (configuration.track) {
                    injectTrack(mw, name);
                    isFragmentOnDestroyHandled = true;
                }
                if (!configuration.leakCanary) {
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
            if (configuration.track) {
                if (!isActivityOnCreateHandled) {
                    injectSuper(ON_CREATE, ON_CREATE_DESC, true, false);
                    isActivityOnCreateHandled = true;
                }
                if (!isActivityOnDestroyHandled) {
                    injectSuper(ON_DESTROY, ON_DESTROY_DESC, true, false);
                    isActivityOnDestroyHandled = true;
                }
            }
        } else if (superIsFragment) {
            if (!isFragmentOnCreateHandled && configuration.track) {
                injectSuper(ON_CREATE, ON_CREATE_DESC, true, false);
                isFragmentOnCreateHandled = true;
            }
            if (!isFragmentOnDestroyHandled && (configuration.track || configuration.leakCanary)) {
                injectSuper(ON_DESTROY, ON_DESTROY_DESC, configuration.track, configuration.leakCanary);
                isFragmentOnDestroyHandled = true;
            }
        }
        super.visitEnd();
    }

    private void injectSuper(String methodName, String methodDesc, boolean track, boolean watch) {
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
        mw.visitCode();
        int instCount = 0;
        if (track) {
            injectTrack(mw, methodName);
            instCount++;
        }
        if (watch) {
            injectWatch(mw);
            instCount++;
        }
        if (methodName.equals(ON_CREATE)) {
            injectSuperOnCreate(mw, methodName, methodDesc);
        } else if (methodName.equals(ON_DESTROY)) {
            injectSuperOnDestroy(mw, methodName, methodDesc, instCount);
        }
        mw.visitEnd();
    }

    private void injectSuperOnCreate(MethodVisitor mw, String methodName, String methodDesc) {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, methodName, methodDesc, false);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(2, 2);
    }

    private void injectSuperOnDestroy(MethodVisitor mw, String methodName, String methodDesc, int instCount) {
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
        if (methodName.length() == ON_CREATE.length()) {
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