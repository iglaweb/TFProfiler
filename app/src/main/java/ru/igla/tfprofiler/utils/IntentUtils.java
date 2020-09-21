package ru.igla.tfprofiler.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import ru.igla.tfprofiler.core.Timber;


/**
 * Created by lashkov on 30/06/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
public final class IntentUtils {

    private IntentUtils() {
    }

    public static boolean startActivitySafely(@NonNull Context context, Intent intent) {
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Timber.e(e);
            return false;
        }
    }

    public static boolean startActivityForResultSafely(@NonNull Activity activity, int requestCode, @NonNull Intent intent) {
        //handle security exception e.g. Huawei Y6 PRO java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.USAGE_ACCESS_SETTINGS
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(e);
            return false;
        }
    }

    public static boolean startFragmentForResultSafely(@NonNull Fragment fragment, int requestCode, @NonNull Intent intent) {
        //handle security exception e.g. Huawei Y6 PRO java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.USAGE_ACCESS_SETTINGS
        try {
            fragment.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(e);
            return false;
        }
    }

    private static boolean openGooglePlayProductDetails(final Context context, final String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        try {
            return openGooglePlay(context, uri);
        } catch (ActivityNotFoundException e) {
            return openWebBrowser(context, "http://play.google.com/store/apps/details?id=" + packageName);
        }
    }

    private static boolean openGooglePlay(final Context context, Uri uri) throws ActivityNotFoundException {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return startActivitySafely(context, intent);
    }

    public static boolean openGooglePlayPage(final @NonNull String applicationId, final @NonNull Context context) {
        return openGooglePlayProductDetails(context, applicationId);
    }

    public static boolean openWebBrowser(final Context context, final String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return startActivitySafely(context, intent);
    }

    public static boolean isIntentAvailable(@NonNull Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            List<ResolveInfo> list = packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            return !list.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return 0;
    }
}
