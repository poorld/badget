package com.poorld.badget.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ConfigUtils {

    private static final String TAG = "Badget#ConfigUtils";

    public static final String FRIDA_GADGET_LIB = "libfrida_gadget.so";
    public static final String FRIDA_GADGET_CONFIG_LIB = "libfrida_gadget.config.so";

    private static final String BADGET_DATA_PATH = "/data/local/tmp/badget/";

    public static final String HOOK_JS = "hook.js";

    // /data/local/tmp/badget/
    public static String getBadgetDataPath() {
        return BADGET_DATA_PATH;
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
    public static File getAppGadgetLibPath(Context context) {
        File appDir = context.getDir("libs", Context.MODE_PRIVATE);
        return new File(appDir, FRIDA_GADGET_LIB);
    }

    // /data/user/0/packageName/app_libs/libfrida_gadget.config.so
    public static File getAppGadgetConfigPath(Context context) {
        File appDir = context.getDir("libs", Context.MODE_PRIVATE);
        return new File(appDir, FRIDA_GADGET_CONFIG_LIB);
    }

    // save to /data/user/0/packageName/app_libs/libfrida_gadget.config.so
    public static boolean saveAppGadgetConfig(Context context) {
        Log.d(TAG, "saveAppGadgetConfig: ");
        String gadgetConfigJson = getGadgetConfigJson(context.getPackageName());
        Log.d(TAG, "gadgetConfigJson: " + gadgetConfigJson);
        return saveFile(gadgetConfigJson, getAppGadgetConfigPath(context).getPath());
    }



    /**
     * {
     *   "interaction": {
     *     "type": "script",
     *     "path": "/data/local/tmp/packageName/hook.js",
     *     "on_change": "reload"
     *   }
     * }
     */
    public static String getGadgetConfigJson(String packageName) {
        JSONObject wrap = new JSONObject();
        JSONObject interaction = new JSONObject();
        try {
            interaction.put("type", "script");
            interaction.put("path", getBadgetJSPath(packageName).getPath());
            interaction.put("on_change", "reload");
            wrap.putOpt("interaction", interaction);
            Log.d(TAG, "getGadgetConfigJson: " + wrap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = wrap.toString();
        return json.replaceAll("\\\\", "");
    }


    private static boolean saveFile(String config, String toFile) {
        Log.d(TAG, "saveConfig to " + toFile);
        try {

            FileOutputStream fileOutput = new FileOutputStream(toFile);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byteOut.write(config.getBytes());
            // 从内存到写入到具体文件
            fileOutput.write(byteOut.toByteArray());
            // 关闭文件流
            byteOut.close();
            fileOutput.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    /**
     * @param fromFiles 指定的下载目录
     * @param toFile    应用的包路径
     * @return int
     */
    public static int copyFile(String fromFiles, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFiles);
        //如同判断SD卡是否存在或者文件是否存在,如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        if (currentFiles == null) {
            Log.d("soFile---", "未获取到文件");
            return -1;
        }
        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                //如果当前项为子目录 进行递归
                copyFile(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");
            } else {
                //如果当前项为文件则进行文件拷贝
                int id = doCopy(currentFiles[i].getPath(), toFile + File.separator + currentFiles[i].getName());

            }
        }
        return 0;
    }

    /**
     * 要复制的目录下的所有非子目录(文件夹)文件拷贝
     *
     * @param fromFiles 指定的下载目录
     * @param toFile    应用的包路径
     * @return int
     */
    private static int doCopy(String fromFiles, String toFile) {
        Log.d(TAG, "复制文件到" + toFile);
        try {
            FileInputStream fileInput = new FileInputStream(fromFiles);
            FileOutputStream fileOutput = new FileOutputStream(toFile);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 1];
            int len = -1;
            while ((len = fileInput.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fileOutput.write(byteOut.toByteArray());
            // 关闭文件流
            byteOut.close();
            fileOutput.close();
            fileInput.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }
}
