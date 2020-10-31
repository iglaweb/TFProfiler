package ru.igla.tfprofiler.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.Timber
import kotlin.math.ceil
import kotlin.math.floor


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