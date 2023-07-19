package com.poorld.badget.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LoadLibraryUtil {
    private static final String TAG = "badget_xposed ";
    private static File lastSoDir = null;


    /**
     * 清除so路径，实际上是设置一个无效的path，用户测试无so库的情况
     *
     * @param classLoader
     */
    public static void clearSoPath(ClassLoader classLoader) {
        try {
            final String testDirNoSo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/duqian/";
            new File(testDirNoSo).mkdirs();
            installNativeLibraryPath(classLoader, testDirNoSo);
        } catch (Throwable throwable) {
            Log.e(TAG, "dq clear path error" + throwable.toString());
            throwable.printStackTrace();
        }
    }

    public static synchronized boolean installNativeLibraryPath(ClassLoader classLoader, String folderPath) throws Throwable {
        return installNativeLibraryPath(classLoader, new File(folderPath));
    }

    public static synchronized boolean installNativeLibraryPath(ClassLoader classLoader, File folder)
            throws Throwable {
        Log.d(TAG, "installNativeLibraryPath: ");
        if (classLoader == null || folder == null || !folder.exists()) {
            Log.e(TAG, "classLoader or folder is illegal " + folder);
            return false;
        }
        final int sdkInt = Build.VERSION.SDK_INT;
        final boolean aboveM = (sdkInt == 25 && getPreviousSdkInt() != 0) || sdkInt > 25;
        if (aboveM) {
            try {
                V25.install(classLoader, folder);
            } catch (Throwable throwable) {
                try {
                    V23.install(classLoader, folder);
                } catch (Throwable throwable1) {
                    V14.install(classLoader, folder);
                }
            }
        } else if (sdkInt >= 23) {
            try {
                V23.install(classLoader, folder);
            } catch (Throwable throwable) {
                V14.install(classLoader, folder);
            }
        } else if (sdkInt >= 14) {
            V14.install(classLoader, folder);
        }
        lastSoDir = folder;
        return true;
    }

    private static final class V23 {
        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);

            Field nativeLibraryDirectories = ReflectUtil.findField(dexPathList, "nativeLibraryDirectories");
            List<File> libDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);

            //去重
            if (libDirs == null) {
                libDirs = new ArrayList<>(2);
            }
            final Iterator<File> libDirIt = libDirs.iterator();
            while (libDirIt.hasNext()) {
                final File libDir = libDirIt.next();
                if (folder.equals(libDir) || folder.equals(lastSoDir)) {
                    libDirIt.remove();
                    Log.d(TAG, "dq libDirIt.remove() " + folder.getAbsolutePath());
                    break;
                }
            }

            libDirs.add(0, folder);
            Field systemNativeLibraryDirectories =
                    ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> systemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);

            //判空
            if (systemLibDirs == null) {
                systemLibDirs = new ArrayList<>(2);
            }
            Log.d(TAG, "dq systemLibDirs,size=" + systemLibDirs.size());

            Method makePathElements = ReflectUtil.findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            libDirs.addAll(systemLibDirs);

            Object[] elements = (Object[]) makePathElements.invoke(dexPathList, libDirs, null, suppressedExceptions);
            Field nativeLibraryPathElements = ReflectUtil.findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.setAccessible(true);
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }

    /**
     * 把自定义的native库path插入nativeLibraryDirectories最前面，即使安装包libs目录里面有同名的so，也优先加载指定路径的外部so
     */
    private static final class V25 {
        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);
            Field nativeLibraryDirectories = ReflectUtil.findField(dexPathList, "nativeLibraryDirectories");

            List<File> libDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            //去重
            if (libDirs == null) {
                libDirs = new ArrayList<>(2);
            }
            final Iterator<File> libDirIt = libDirs.iterator();
            while (libDirIt.hasNext()) {
                final File libDir = libDirIt.next();
                if (folder.equals(libDir) || folder.equals(lastSoDir)) {
                    libDirIt.remove();
                    Log.d(TAG, "dq libDirIt.remove()" + folder.getAbsolutePath());
                    break;
                }
            }
            libDirs.add(0, folder);
            //system/lib
            Field systemNativeLibraryDirectories = ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> systemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);

            //判空
            if (systemLibDirs == null) {
                Log.d(TAG, "systemLibDirs == null");
                systemLibDirs = new ArrayList<>(2);
            }
            //test
            //libDirs.add(new File("/data/local/tmp/xcube/armeabi-v7a"));

            Method makePathElements = ReflectUtil.findMethod(dexPathList, "makePathElements", List.class);
            libDirs.addAll(systemLibDirs);
            Log.d(TAG, "libDirs: " + libDirs.toString());
            Object[] elements = (Object[]) makePathElements.invoke(dexPathList, libDirs);
            Field nativeLibraryPathElements = ReflectUtil.findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.setAccessible(true);
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }


    private static final class V14 {
        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);

            ReflectUtil.expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{folder});
        }
    }

    /**
     * fuck部分机型删了该成员属性，兼容
     *
     * @return 被厂家删了返回1，否则正常读取
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static int getPreviousSdkInt() {
        try {
            return Build.VERSION.PREVIEW_SDK_INT;
        } catch (Throwable ignore) {
        }
        return 1;
    }


    public static void loadSoFile(ClassLoader classLoader, String soPath) {
        try {
            Log.d(TAG, "copy libs and config to : " + soPath);
            //copyFile(fromPath, toPath.getAbsolutePath());
            installNativeLibraryPath(classLoader, soPath);
        } catch (Throwable throwable) {
            Log.e("loadSoFile", "loadSoFile error " + throwable.getMessage());
        }
    }



    /**
     * 判断immqy so 文件是否存在
     *
     * @param name "libimmqy" so库
     * @return boolean
     */
    private static boolean isLoadSoFiles(String name, File dirs) {
        boolean getSoLib = false;
        File[] currentFiles;
        currentFiles = dirs.listFiles();
        if (currentFiles == null) {
            return false;
        }
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].getName().contains(name)) {
                getSoLib = true;
            }
        }
        return getSoLib;
    }

}