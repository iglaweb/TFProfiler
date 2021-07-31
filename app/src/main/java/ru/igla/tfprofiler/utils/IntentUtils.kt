package ru.igla.tfprofiler.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import ru.igla.tfprofiler.core.Timber

/**
 * Created by lashkov on 30/06/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */


object IntentUtils {
    fun startActivitySafely(context: Context, intent: Intent?): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            false
        } catch (e: SecurityException) {
            Timber.e(e)
            false
        }
    }

    @JvmStatic
    fun startActivityForResultSafely(
        activity: Activity,
        requestCode: Int,
        intent: Intent
    ): Boolean {
        //handle security exception e.g. Huawei Y6 PRO java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.USAGE_ACCESS_SETTINGS
        return try {
            activity.startActivityForResult(intent, requestCode)
            true
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            false
        } catch (e: SecurityException) {
            Timber.e(e)
            false
        }
    }

    fun startFragmentForResultSafely(
        fragment: Fragment,
        requestCode: Int,
        intent: Intent
    ): Boolean {
        //handle security exception e.g. Huawei Y6 PRO java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.USAGE_ACCESS_SETTINGS
        return try {
            fragment.startActivityForResult(intent, requestCode)
            true
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            false
        } catch (e: SecurityException) {
            Timber.e(e)
            false
        }
    }

    private fun openGooglePlayProductDetails(context: Context, packageName: String): Boolean {
        val uri = Uri.parse("market://details?id=$packageName")
        return try {
            openGooglePlay(context, uri)
        } catch (e: ActivityNotFoundException) {
            openWebBrowser(context, "http://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun openGooglePlay(context: Context, uri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return startActivitySafely(context, intent)
    }

    fun openGooglePlayPage(applicationId: String, context: Context): Boolean {
        return openGooglePlayProductDetails(context, applicationId)
    }

    private fun openWebBrowser(context: Context, url: String?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return startActivitySafely(context, intent)
    }
}