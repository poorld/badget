package com.poorld.badget.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;


import com.poorld.badget.entity.ItemAppEntity;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PkgManager {

    public static List<ItemAppEntity> getUserApp(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = queryApp(context, mainIntent, 0);
        return toItemAppEntitys(context, resolveInfos);
    }

    public static List<ResolveInfo> getSystemApp() {
        return null;
    }

    public static List<ResolveInfo> queryApp(Context context, Intent intent, int flags) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, flags);
        //List<PackageInfo> installedPackages = packageManager.getInstalledPackages(flags);
        return resolveInfos;
    }

    public static List<ItemAppEntity> toItemAppEntitys(Context context, List<ResolveInfo> resolveInfos) {

        List<ItemAppEntity> appEntities = resolveInfos.stream().map((Function<ResolveInfo, ItemAppEntity>) resolveInfo -> {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = resolveInfo.loadLabel(context.getPackageManager()).toString();
            Drawable drawable = resolveInfo.loadIcon(context.getPackageManager());
            return new ItemAppEntity(packageName, appName, drawable);
        }).collect(Collectors.toList());
        return appEntities;
    }
}
