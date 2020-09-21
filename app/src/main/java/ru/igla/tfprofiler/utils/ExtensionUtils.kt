package ru.igla.tfprofiler.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.Timber
import kotlin.math.ceil
import kotlin.math.floor

fun allPermissionsGranted(grantResults: IntArray) = grantResults.all {
    it == PackageManager.PERMISSION_GRANTED
}

fun Int.toDp(displayMetrics: DisplayMetrics): Float = this.toFloat() / displayMetrics.density
fun Int.toSp(displayMetrics: DisplayMetrics): Float = this.toFloat() / displayMetrics.scaledDensity
fun Float.spToPx(displayMetrics: DisplayMetrics): Int =
    (this * displayMetrics.scaledDensity).round()

fun Float.dpToPx(displayMetrics: DisplayMetrics): Int =
    (this * displayMetrics.density).round()

@UiThread
fun ImageView.clearAndSetBitmapNoRefresh(bitmap: Bitmap?) {
    drawable?.let {
        val screenBitmap = (it as BitmapDrawable).bitmap
        screenBitmap?.recycle()
    }
    setImageBitmap(bitmap)
}

private fun Float.round(): Int = (if (this < 0) ceil(this + 0.5f) else floor(this + 0.5f)).toInt()


inline fun <T> runMeasureTimeMs(tag: String, block: () -> T): T {
    val start = DateUtils.getCurrentDateInMs()
    val ret = block()
    logI {
        val elapsed = DateUtils.getCurrentDateInMs() - start
        "TIME: $tag $elapsed ms"
    }
    return ret
}

inline fun logI(e: () -> String) {
    if (BuildConfig.DEBUG) {
        Timber.i(e())
    }
}

inline fun logD(e: () -> String) {
    if (BuildConfig.DEBUG) {
        Timber.d(e())
    }
}

/***
 * https://discuss.kotlinlang.org/t/performant-and-elegant-iterator-over-custom-collection/2962/6
 */
inline fun <E> List<E>.forEachNoIterator(block: (E) -> Unit) {
    var index = 0
    val size = size
    while (index < size) {
        block(get(index))
        index++
    }
}

inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

/**
 * Created by igor-lashkov on 25/10/2017.
 */

private const val TAG = "TFProfiler-android"
fun Activity.hideKeyboard(flags: Int = InputMethodManager.HIDE_NOT_ALWAYS): Boolean {
    currentFocus?.let {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        return inputMethodManager?.hideSoftInputFromWindow(it.windowToken, flags) ?: false
    }
    return false
}

fun View.hideKeyboard(flags: Int = InputMethodManager.HIDE_NOT_ALWAYS): Boolean {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    return inputMethodManager?.hideSoftInputFromWindow(windowToken, flags) ?: false
}

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun Any.toast(context: Context) {
    Toast.makeText(context, this.toString(), Toast.LENGTH_LONG).show()
}

internal fun Context.dp(dp: Float): Double = dpF(dp).toDouble()
fun Context.dpToPx(dp: Float): Int = Math.round(dpF(dp))
fun Context.dpF(dp: Float): Float = dp * resources.displayMetrics.density

fun <T> lazyNonSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun <T> MutableLiveData<T>.sendValueIfNew(newValue: T) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        if (this.value != newValue) value = newValue
    } else {
        postValue(newValue)
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}