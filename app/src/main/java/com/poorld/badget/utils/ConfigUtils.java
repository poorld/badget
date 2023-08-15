package com.poorld.badget.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.poorld.badget.entity.ConfigEntity;
import com.poorld.badget.entity.InteractionType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigUtils {

    private static final String TAG = "Badget#ConfigUtils";

    public static final String DBAGET_PKG_NAME = "com.poorld.badget";
    public static final String FRIDA_GADGET_LIB = "libfrida_gadget.so";
    public static final String FRIDA_GADGET_CONFIG_LIB = "libfrida_gadget.config.so";

    private static final String DATA_LOCAL_TMP = "/data/local/tmp/";
    private static final String BADGET_DATA_PATH = "/data/local/tmp/badget/";

    public static final String HOOK_JS = "hook.js";

    public static final String ABI_V8A = "arm64-v8a";
    public static final String ABI_V7A = "armeabi-v7a";

    //public static Map<String, ConfigEntity.java> configCache = new HashMap<>();
    public static ConfigEntity mConfigCache;

    private static Gson gson = new Gson();
    public static final String FILE_NAME_BADGET_CONFIG = "badget.json";

    // assets资源目录下的badget目录
    public static final String ASSETS_BADGET_PATH = "badget";

    // /data/local/tmp/badget/
    public static String getDataTmpPath() {
        return DATA_LOCAL_TMP;
    }

    // /data/local/tmp/badget/
    public static String getBadgetDataPath() {
        return BADGET_DATA_PATH;
    }

    public static String getBadgetConfigPath() {
        return new File(getBadgetDataPath(), FILE_NAME_BADGET_CONFIG).getPath();
    }

    // /data/local/tmp/badget/packageName/
    public static File getBadgetPackagePath(String packageName) {
        return new File(getBadgetDataPath(), packageName);
    }

    // /data/local/tmp/badget/packageName/hook.js
    public static File getBadgetJSPath(String packageName) {
        File badgetPackagePath = new File(getBadgetDataPath(), packageName);
        return new File(badgetPackagePath, HOOK_JS);
    }

    // /data/user/0/packageName/app_libs/
    public static String getAppPrivLibDir(Context context) {
        File appDir = context.getDir("libs", Context.MODE_PRIVATE);
        return appDir.getPath();
    }


    // /data/user/0/packageName/app_libs/libfrida_gadget.so
    public static File getAppGadgetLibPath(Context context,String soName) {
        File appDir = context.getDir("libs", Context.MODE_PRIVATE);
        return new File(appDir, /*FRIDA_GADGET_LIB*/ getGadgetLibName(soName));
    }

    // /data/user/0/packageName/app_libs/libfrida_gadget.config.so
    public static File getAppGadgetConfigPath(Context context, String soName) {
        File appDir = context.getDir("libs", Context.MODE_PRIVATE);
        return new File(appDir, /*FRIDA_GADGET_CONFIG_LIB*/getGadgetConfigLibName(soName));
    }

    public static String getRandomName() {
        return CommonUtils.randomString(5);
    }

    public static String getGadgetLibName(String randomName) {
        return "lib" + randomName + ".so";
    }

    public static String getGadgetConfigLibName(String randomName) {
        return "lib" + randomName + ".config.so";
    }

    // save to /data/user/0/packageName/app_libs/libfrida_gadget.config.so
    public static boolean saveAppGadgetConfig(Context context, ConfigEntity.PkgConfig pkgConfig) {
        Log.d(TAG, "saveAppGadgetConfig: pkgConfig=" + pkgConfig);
        String gadgetConfigJson = null;
        switch (pkgConfig.getType()) {
            case Listen:
                // objection -g Gadget explore
                // 后续更新可设置 ip:port
                gadgetConfigJson = getJSConfigForListen("127.0.0.1", 27042, true);
                break;
            case Connect:
                // 后续更新可设置 ip:port
                gadgetConfigJson = getJSConfigForConnect("127.0.0.1", 27052);
                break;
            case Script:
                // 测试通过
                gadgetConfigJson = getJSConfigForScript(pkgConfig.getJsPath());
                break;
            case ScriptDirectory:
                // 测试通过
                gadgetConfigJson = getJSConfigForScriptDirectory(context.getPackageName());
                break;
        }
        Log.d(TAG, "gadgetConfigJson: " + gadgetConfigJson);
        if (gadgetConfigJson == null) {
            return false;
        }
        return CommonUtils.saveFile(gadgetConfigJson, getAppGadgetConfigPath(context, pkgConfig.getSoName()).getPath());
    }


    public static boolean checkBadgetSoExists() {
        File badgetDir = new File(getBadgetDataPath());
        Log.d(TAG, "file: " + Arrays.toString(badgetDir.list()));

        if (!badgetDir.exists()) {
            return false;
        }

        File abiv8Dir = new File(badgetDir, ABI_V8A);
        if (!new File(abiv8Dir, FRIDA_GADGET_LIB).exists()) {
            return false;
        }

        File abiv7Dir = new File(badgetDir, ABI_V7A);
        if (!new File(abiv7Dir, FRIDA_GADGET_LIB).exists()) {
            return false;
        }

        return true;
    }

    public static boolean checkBadgetConfigExists() {
        File pkgConfigFile = new File(getBadgetConfigPath());
        return pkgConfigFile.exists();
    }




    /**
     * see {@link com.poorld.badget.entity.InteractionType#Script}
     * {
     *   "interaction": {
     *     "type": "script",
     *     "path": "/data/local/tmp/packageName/hook.js",
     *     "on_change": "reload"
     *   }
     * }
     */
    public static String getJSConfigForScript(String jsPath) {
        JSONObject wrap = new JSONObject();
        JSONObject interaction = new JSONObject();
        try {
            interaction.put("type", "script");
            interaction.put("path", jsPath);
            interaction.put("on_change", "reload");
            wrap.putOpt("interaction", interaction);
            Log.d(TAG, "getGadgetConfigJson: " + wrap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = wrap.toString();
        return json.replaceAll("\\\\", "");
    }

    /**
     * see {@link com.poorld.badget.entity.InteractionType#Listen}
     * {
     *   "interaction": {
     *     "type": "listen",
     *     "address": "127.0.0.1",
     *     "port": 27042,
     *     "on_port_conflict": "fail",
     *     "on_load": "wait"
     *   }
     * }
     */
    public static String getJSConfigForListen(String address, int port, boolean wait) {
        JSONObject wrap = new JSONObject();
        JSONObject interaction = new JSONObject();
        try {
            interaction.put("type", "listen");
            interaction.put("address", address);
            interaction.put("port", port);
            interaction.put("on_port_conflict", "fail");
            interaction.put("on_load", wait ? "wait" : "resume");
            wrap.putOpt("interaction", interaction);
            Log.d(TAG, "getGadgetConfigJson: " + wrap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = wrap.toString();
        return json.replaceAll("\\\\", "");
    }

    /**
     * see {@link com.poorld.badget.entity.InteractionType#Connect}
     * {
     *   "interaction": {
     *     "type": "connect",
     *     "address": "127.0.0.1",
     *     "port": 27052
     *   }
     * }
     */
    public static String getJSConfigForConnect(String address, int port) {
        JSONObject wrap = new JSONObject();
        JSONObject interaction = new JSONObject();
        try {
            interaction.put("type", "connect");
            interaction.put("address", address);
            interaction.put("port", port);
            wrap.putOpt("interaction", interaction);
            Log.d(TAG, "getGadgetConfigJson: " + wrap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = wrap.toString();
        return json.replaceAll("\\\\", "");
    }

    /**
     * see {@link com.poorld.badget.entity.InteractionType#ScriptDirectory}
     * {
     *   "interaction": {
     *     "type": "script-directory",
     *     "path": "/data/local/tmp/badget/packageName/"
     *   }
     * }
     */
    public static String getJSConfigForScriptDirectory(String packageName) {
        JSONObject wrap = new JSONObject();
        JSONObject interaction = new JSONObject();
        try {
            interaction.put("type", "script-directory");
            interaction.put("path", getBadgetJSPath(packageName).getParent());
            wrap.putOpt("interaction", interaction);
            Log.d(TAG, "getGadgetConfigJson: " + wrap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = wrap.toString();
        return json.replaceAll("\\\\", "");
    }

    public static int firstInit(Context applicationContext) {

        // /data/user/0/com.poorld.badget/app_cache
        File appCache = applicationContext.getDir("cache", Context.MODE_PRIVATE);
        Log.d(TAG, "appCachePath: " + appCache.getPath());

        /**
         * from
         *      assets/badget/badget.zip
         * to
         *      /data/user/0/com.poorld.badget/app_cache/badget.zip
         */
        CommonUtils.copyAssetsFile(applicationContext, ConfigUtils.ASSETS_BADGET_PATH, appCache.getPath());

        /**
         * unzip /data/user/0/com.poorld.badget/app_cache/badget.zip
         */
        String zipFilePath = new File(appCache.getPath(), "badget.zip").getPath();
        CommonUtils.unzip(zipFilePath, true);
        List<String> cmds = new ArrayList<>();

        /**
         * copy from
         *      /data/user/0/com.poorld.badget/app_cache
         * to
         *      /data/local/tmp/
         *
         * chmod 777 /data/local/tmp/badget
         *
         * delete /data/user/0/com.poorld.badget/app_cache
         */
        cmds.add("mkdir " + ConfigUtils.getBadgetDataPath());
        cmds.add(String.format("cp -r %s/* %s", appCache.getPath(), ConfigUtils.DATA_LOCAL_TMP));
        cmds.add("chmod -R 777 " + ConfigUtils.BADGET_DATA_PATH);
        cmds.add("rm -rf " + appCache.getPath());

        /***
         * create
         *       /data/local/tmp/badget/badget.json
         */
        String badgetConfigPath = getBadgetConfigPath();
        File pkgConfigFile = new File(badgetConfigPath);
        if (!pkgConfigFile.exists()) {
            cmds.add("touch " + badgetConfigPath);
            cmds.add("chmod 777 " + badgetConfigPath);
        }

        ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
        if (result.result == 0) {
            Log.d(TAG, "create file success!");
        } else {
            Log.d(TAG, "create file failed!");
            //throw new RuntimeException();
        }
        return result.result;
    }



    public static void initConfig() {

        Log.d(TAG, "initConfig: ");
        Log.d(TAG, "mConfigCache: "+mConfigCache);

        if (mConfigCache != null) {
            Log.d(TAG, "config has init.");
            return;
        }

        File pkgConfigFile = new File(getBadgetConfigPath());
        if (!pkgConfigFile.exists()) {
            //try {
            //    Log.d(TAG, "first init.");
            //    pkgConfigFile.createNewFile();
            //} catch (IOException e) {
            //    Log.d(TAG, "initConfig IOException " + e.getMessage());
            //}

            List<String> cmds = new ArrayList<>();
            cmds.add("touch " + getBadgetConfigPath());
            cmds.add("chmod 777 " + getBadgetConfigPath());
            ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
            if (result.result == 0) {
                Log.d(TAG, "create file success!");
            } else {
                Log.d(TAG, "create file failed!");
                throw new RuntimeException();
            }
            mConfigCache = new ConfigEntity();
            mConfigCache.setEnabled(true);
            updatePkgConfig();
            return;
        }


        String conf = CommonUtils.readFile(pkgConfigFile);
        if (!TextUtils.isEmpty(conf)) {
            try {
                mConfigCache = gson.fromJson(conf, ConfigEntity.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        if (mConfigCache == null) {
            mConfigCache = new ConfigEntity();
            mConfigCache.setEnabled(true);
            updatePkgConfig();
        }
        Log.d(TAG, "mConfigCache: " + mConfigCache);
    }

    public static void updatePkgConfig() {
        CommonUtils.saveFile(gson.toJson(mConfigCache), getBadgetConfigPath());
    }

    public static ConfigEntity.PkgConfig getPkgConfig(String packageName) {
        if (mConfigCache == null) {
            return null;
        }
        Map<String, ConfigEntity.PkgConfig> configMap = mConfigCache.getPkgConfigs();
        ConfigEntity.PkgConfig pkgConfig = configMap.get(packageName);
        if (pkgConfig == null) {
            pkgConfig = new ConfigEntity.PkgConfig();
            pkgConfig.setPkgName(packageName);
            // 设置默认交互模式
            pkgConfig.setType(InteractionType.Script);
            mConfigCache.addPkgConfigs(packageName, pkgConfig);
        }
        return pkgConfig;
    }

    public static Map<String, ConfigEntity.PkgConfig> getPkgConfigs() {
        if (mConfigCache == null) {
            return null;
        }
        return mConfigCache.getPkgConfigs();
    }

    public static File[] getDirScripts(String packageName) {
        File badgetPackagePath = getBadgetPackagePath(packageName);
        File[] files = badgetPackagePath.listFiles(file -> file.getName().endsWith(".js"));
        Log.d(TAG, "getDirScripts: " + files);
        if (files != null && files.length > 0) {
            Log.d(TAG, "getDirScripts: files.length " + files.length);
            return files;
        }
        return null;
    }

}
