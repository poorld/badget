package com.poorld.badget.hook;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.LoadLibraryUtil;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 2023/07/05 by teenyda
 */
public class Badget implements IXposedHookLoadPackage {

    // /data/local/tmp/badget/arm64-v8a/libfrida_gadget.so
    // /data/local/tmp/badget/armeabi-v7a/libfrida_gadget.so
    // /data/local/tmp/badget/com.aaa.bbb/hook.js

    // /data/user/0/com.aaa.bbb/app_libs/libfrida_gadget.so
    // 动态生成
    // /data/user/0/com.aaa.bbb/app_libs/libfrida_gadget.config.so

    public static final String TAG = "Badget";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: " + loadPackageParam.packageName);
        //if ("poorld.xp.badget".equals(loadPackageParam.packageName)) {
        //    return;
        //}
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "beforeHookedMethod: ");
                try {
                    Context base = (Context) param.args[0];

                    // /data/user/0/packageName/app_libs/
                    String applib = ConfigUtils.getAppPrivLibDir(base);
                    String ABI = android.os.Process.is64Bit() ? "arm64-v8a" : "armeabi-v7a";

                    Log.d(TAG, "attach beforeHookedMethod toPath:" + applib);

                    // /data/user/0/packageName/app_libs/libfrida_gadget.so
                    File appGadgetLibPath = ConfigUtils.getAppGadgetLibPath(base);
                    Log.d(TAG, "appGadgetLibPath: " + appGadgetLibPath);

                    if (!appGadgetLibPath.exists()) {
                        // 复制so到/data/user/0/packageName/app_libs/
                        ConfigUtils.copyFile(ConfigUtils.getBadgetDataPath() + ABI, applib);
                        // 在 /data/user/0/packageName/app_libs/目录下生成libfrida_gadget.config.so
                        // "path": "/data/local/tmp/packageName/hook.js",
                        ConfigUtils.saveAppGadgetConfig(base);
                    }

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "afterHookedMethod: ");
                //System.loadLibrary("frida_gadget");
                //loadTargetLibrary(this.getClass().getClassLoader());

                Context context = (Context) param.args[0];
                File appGadgetLibPath = ConfigUtils.getAppGadgetLibPath(context);
                // libfrida_gadget库不存在
                if (!appGadgetLibPath.exists()) {
                    return;
                }

                File hookJsFile = ConfigUtils.getBadgetJSPath(context.getPackageName());
                // 脚本不存在
                if (!hookJsFile.exists()) {
                    return;
                }

                // ClassLoader 加入库路径:/data/user/0/packageName/app_libs
                LoadLibraryUtil.loadSoFile(loadPackageParam.classLoader, ConfigUtils.getAppPrivLibDir(context));
                Log.d(TAG, "ClassLoader: " + context.getClassLoader());


                // ok
                // /data/user/0/com.network.xf100/app_libs/libfrida_gadget.so
                Log.d(TAG, "System.load: " + appGadgetLibPath.getPath());
                //System.load(appGadgetLibPath.getPath());

                // ok
                XposedHelpers.callMethod(Runtime.getRuntime(), "loadLibrary0", context.getClassLoader(), "frida_gadget");

                // error
                // java.lang.UnsatisfiedLinkError: LspModuleClassLoader couldn't find "libfrida_gadget.so"
                //System.loadLibrary("frida_gadget");
            }
        });


    }
}