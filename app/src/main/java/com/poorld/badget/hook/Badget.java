package com.poorld.badget.hook;

import static com.poorld.badget.utils.ConfigUtils.DBAGET_PKG_NAME;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.poorld.badget.MainActivity;
import com.poorld.badget.entity.ConfigEntity;
import com.poorld.badget.utils.CommonUtils;
import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.LoadLibraryUtil;

import java.io.File;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 2023/07/05 by teenyda
 */
public class Badget implements IXposedHookLoadPackage {

    public static final String TAG = "Badget#";

    private ConfigEntity mConfig;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: " + loadPackageParam.packageName);

        if (DBAGET_PKG_NAME.equals(loadPackageParam.packageName)) {

            Class<?> appClazz = loadPackageParam.classLoader.loadClass("com.poorld.badget.app.MyApp");
            XposedHelpers.setStaticBooleanField(appClazz, "isModuleActive", true);

            return;
        }

        if (mConfig == null) {
            if (ConfigUtils.mConfigCache == null) {
                ConfigUtils.initConfig();
            }
            mConfig = ConfigUtils.mConfigCache;
        }

        Log.d(TAG, "mConfig: " + mConfig);

        if (mConfig == null) {
            return;
        }
        if (!mConfig.isEnabled()) {
            return;
        }

        Map<String, ConfigEntity.PkgConfig> pkgConfigs = mConfig.getPkgConfigs();
        ConfigEntity.PkgConfig pkgConfig = pkgConfigs.get(loadPackageParam.packageName);

        if (pkgConfig == null) {
            return;
        }
        if (!pkgConfig.isEnabled()) {
            Log.d(TAG, loadPackageParam.packageName + " is not enabled!!!");
            return;
        }

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "beforeHookedMethod: ");
                try {
                    Context base = (Context) param.args[0];

                    /**
                     * from
                     *      /data/local/tmp/badget/arm64-v8a/
                     * to
                     *      /data/user/0/packageName/app_libs/
                     */
                    String applibDir = ConfigUtils.getAppPrivLibDir(base);
                    String ABI = android.os.Process.is64Bit() ? ConfigUtils.ABI_V8A : ConfigUtils.ABI_V7A;
                    // check /data/user/0/packageName/app_libs/librandom.so
                    File appGadgetLib = ConfigUtils.getAppGadgetLibPath(base, pkgConfig.getSoName());
                    Log.d(TAG, "appGadgetLib: " + appGadgetLib);
                    if (!appGadgetLib.exists()) {
                        String gadgetLibName = ConfigUtils.getGadgetLibName(pkgConfig.getSoName());
                        CommonUtils.copyFile(ConfigUtils.getBadgetDataPath() + ABI, applibDir, gadgetLibName);
                        // save librandom.config.so
                        ConfigUtils.saveAppGadgetConfig(base, pkgConfig);
                    }

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "afterHookedMethod: ");

                Context context = (Context) param.args[0];

                File appGadgetLibPath = ConfigUtils.getAppGadgetLibPath(context, pkgConfig.getSoName());
                // librandom.so库不存在
                if (!appGadgetLibPath.exists()) {
                    return;
                }

                File appGadgetConfigLibPath = ConfigUtils.getAppGadgetConfigPath(context, pkgConfig.getSoName());
                // librandom.config.so库不存在
                if (!appGadgetConfigLibPath.exists()) {
                    return;
                }

                //File hookJsFile = ConfigUtils.getBadgetJSPath(context.getPackageName());
                File hookJsFile = new File(pkgConfig.getJsPath());
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
                XposedHelpers.callMethod(Runtime.getRuntime(), "loadLibrary0", context.getClassLoader(), /*"frida_gadget"*/ pkgConfig.getSoName());

                // error
                // java.lang.UnsatisfiedLinkError: LspModuleClassLoader couldn't find "libfrida_gadget.so"
                //System.loadLibrary("frida_gadget");
            }
        });


    }
}