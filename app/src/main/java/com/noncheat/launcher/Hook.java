package com.noncheat.launcher;

import static de.robv.android.xposed.XposedBridge.log;

import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "NoncheatLauncher ";
    protected final String packageName = "com.android.systemui";
    protected final String packageNameLauncher = "com.google.android.apps.nexuslauncher";
    private Long lastClick = null;
    private PowerManager mPowerManager = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals(packageName)) {
            Class<?> classNotificationPanelViewController = XposedHelpers.findClass("com.android.systemui.shade.NotificationPanelViewController", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(classNotificationPanelViewController, "onEmptySpaceClick", float.class, float.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (mPowerManager == null) {
                        Context mContext = AndroidAppHelper.currentApplication().getApplicationContext();
                        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                        log(TAG + "set power manager " + (mPowerManager != null ? "[success]" : "[fail]"));
                    }
                    onClick();
                }
            });
        } else if (loadPackageParam.packageName.equals(packageNameLauncher)) {
            Class<?> classWorkspace = XposedHelpers.findClass("com.android.launcher3.Workspace", loadPackageParam.classLoader);
            XposedHelpers.findAndHookConstructor(classWorkspace, Context.class, AttributeSet.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Class<?> classGestureDetector$SimpleOnGestureListener = XposedHelpers.findClass("android.view.GestureDetector$SimpleOnGestureListener", loadPackageParam.classLoader);
                    XposedHelpers.findAndHookMethod(classGestureDetector$SimpleOnGestureListener, "onDoubleTap", MotionEvent.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.noncheat.lockscreen", "com.noncheat.lockscreen.MainActivity"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AndroidAppHelper.currentApplication().startActivity(intent);
                        }
                    });
                }
            });
        }
    }

    private void onClick() {
        Long currentTime = System.currentTimeMillis();
        if (lastClick != null && currentTime - lastClick <= ViewConfiguration.getDoubleTapTimeout()) {
            if (mPowerManager == null) {
                log(TAG + "no power manager");
                return;
            }
            XposedHelpers.callMethod(mPowerManager, "goToSleep", SystemClock.uptimeMillis());
            lastClick = null;
        } else {
            lastClick = currentTime;
        }
    }
}
