package com.noncheat.launcher;

import static de.robv.android.xposed.XposedBridge.log;

import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Member;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "NoncheatLauncher ";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classWorkspaceTouchListener = XposedHelpers.findClass("com.android.launcher3.touch.WorkspaceTouchListener", loadPackageParam.classLoader);
        Class<?> classLauncher = XposedHelpers.findClass("com.android.launcher3.Launcher", loadPackageParam.classLoader);
        Class<?> classWorkspace = XposedHelpers.findClass("com.android.launcher3.Workspace", loadPackageParam.classLoader);
        XposedHelpers.findAndHookConstructor(
                classWorkspaceTouchListener,
                classLauncher,
                classWorkspace,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Class<?> classGestureDetector$SimpleOnGestureListener = XposedHelpers.findClass("android.view.GestureDetector$SimpleOnGestureListener", loadPackageParam.classLoader);
                        XposedHelpers.findAndHookMethod(
                                classGestureDetector$SimpleOnGestureListener,
                                "onDoubleTap",
                                MotionEvent.class,
                                new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) {
                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName("com.asdoi.quicktiles", "com.asdoi.quicksettings.MainActivity"));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        AndroidAppHelper.currentApplication().startActivity(intent);
                                    }
                                }
                        );
                    }
                }
        );
    }
}
