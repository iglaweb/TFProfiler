package ru.igla.tfprofiler.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.models_list.ModelEntity
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor


fun Float.spToPx(displayMetrics: DisplayMetrics): Int =
    (this * displayMetrics.scaledDensity).round()

fun Context.dpF(dp: Float): Float = dp * resources.displayMetrics.density

@UiThread
fun ImageView.clearAndSetBitmapNoRefresh(bitmap: Bitmap?) {
    drawable?.let {
        val screenBitmap = (it as BitmapDrawable).bitmap
        screenBitmap?.recycle()
    }
    setImageBitmap(bitmap)
}

val File.extension: String
    get() = name.substringAfterLast('.', "")

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

inline fun startClickSafely(action: () -> Unit) {
    if (ClickTimeoutLock.canProceedClick()) {
        action()
    }
}

fun String.sentenceCase(): String {
    return lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

/**
 * A creator is used to inject the product ID into the ViewModel
 *
 *
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
@Suppress("UNCHECKED_CAST")
class ModelFactory(
    @field:NonNull @param:NonNull private val mApplication: Application,
    private val entity: ModelEntity
) :
    ViewModelProvider.NewInstanceFactory() {

    @NonNull
    override fun <T : ViewModel?> create(@NonNull modelClass: Class<T>): T {
        return modelClass.getConstructor(Application::class.java, ModelEntity::class.java)
            .newInstance(mApplication, entity)
        //return VideoRecognitionViewModel(mApplication, entity) as T
    }
}