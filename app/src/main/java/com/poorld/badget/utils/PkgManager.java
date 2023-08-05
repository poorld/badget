package com.poorld.badget.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.Log;


import com.poorld.badget.entity.ItemAppEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PkgManager {

    public static List<ItemAppEntity> getUserApp(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = queryApp(context, mainIntent, 0);
        return toItemAppEntitys(context, resolveInfos).stream().filter(app -> !app.isSystemApp()).collect(Collectors.toList());
    }

    public static List<ItemAppEntity> getSystemAndUserApp(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = queryApp(context, mainIntent, 0);
        return toItemAppEntitys(context, resolveInfos);
    }

    // <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    public static List<ResolveInfo> queryApp(Context context, Intent intent, int flags) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, flags);
        //List<PackageInfo> installedPackages = packageManager.getInstalledPackages(flags);
        return resolveInfos;
    }

    public static List<ItemAppEntity> filterApp(List<ItemAppEntity> apps, String searchText) {
        if (apps != null && !searchText.isEmpty()) {
            List<ItemAppEntity> list1 = apps.stream().filter(itemAppEntity -> itemAppEntity.getAppName().contains(searchText)).collect(Collectors.toList());
            List<ItemAppEntity> list2 = apps.stream().filter(itemAppEntity -> itemAppEntity.getPackageName().contains(searchText)).collect(Collectors.toList());
            ArraySet<ItemAppEntity> nonRepetitiveSet = new ArraySet<>();
            nonRepetitiveSet.addAll(list1);
            nonRepetitiveSet.addAll(list2);
            return new ArrayList<>(nonRepetitiveSet);
        }
        return null;
    }

    public static List<ItemAppEntity> toItemAppEntitys(Context context, List<ResolveInfo> resolveInfos) {

        return resolveInfos.stream().map((Function<ResolveInfo, ItemAppEntity>) resolveInfo -> {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = resolveInfo.loadLabel(context.getPackageManager()).toString();
            Drawable drawable = resolveInfo.loadIcon(context.getPackageManager());
            boolean isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // test
            //if ("com.tencent.mm".equals(packageName)) {
            //    ItemAppEntity itemAppEntity = new ItemAppEntity(packageName, appName, drawable, isSystemApp);
            //    itemAppEntity.setHookEnabled(true);
            //    return itemAppEntity;
            //}
            return new ItemAppEntity(packageName, appName, drawable, isSystemApp);
        }).collect(Collectors.toList());
    }

    public static ItemAppEntity getItemAppEntity(Context context,String packageName) {
        PackageManager packageManager = context.getPackageManager();

        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            //String packageName = packageInfo.packageName;
            String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            return new ItemAppEntity(packageName, appName, icon, isSystemApp);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
