package ru.igla.tfprofiler.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.annotation.AttrRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import ru.igla.tfprofiler.R


interface UrlLauncher {
    fun viewUrl(url: String)
}

class RealUrlLauncher(
    private val currentActivity: Context
) : UrlLauncher {

    override fun viewUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(resolveColor(R.attr.colorPrimary))
            .build()
        try {
            customTabsIntent.launchUrl(currentActivity, url.toUri())
        } catch (_: ActivityNotFoundException) {
            val chooser = Intent.createChooser(
                Intent(ACTION_VIEW)
                    .setData(url.toUri()), "View URL"
            )
            currentActivity.startActivity(chooser)
        }
    }

    private fun resolveColor(@AttrRes attr: Int): Int {
        val a = currentActivity.theme.obtainStyledAttributes(intArrayOf(attr))
        try {
            return a.getColor(0, 0)
        } finally {
            a.recycle()
        }
    }
}