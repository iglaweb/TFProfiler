package ru.igla.tfprofiler.ui.widgets.toast;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import ru.igla.tfprofiler.utils.StringUtils;


/**
 * Simple util for showing Toast. Do not duplicate Toast, if it is already running.
 */
public final class Toaster {

    @NonNull
    private final Context mContext;

    @Nullable
    private ToastCompat mToast;

    @Nullable
    private String mCurrentText;

    public Toaster(@NonNull Context context) {
        this.mContext = context;
    }

    private boolean isActivityAlive() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            return !activity.isFinishing();
        }
        return true;
    }

    private void showToast(String text, int length) {
        if (!isActivityAlive()) {
            return;
        }

        View toastView = mToast == null ? null : mToast.getView();

        if (mToast == null || (toastView != null && !toastView.isShown())) {
            makeAndShowNewToast(text, length);
        } else if (!isSameToast(text)) {
            mToast.cancel();
            makeAndShowNewToast(text, length);
        }
    }

    private void makeAndShowNewToast(String text, int length) {
        mToast = ToastCompat.makeText(mContext, text, length);
        mToast.show();
        mCurrentText = text;
    }

    private boolean isSameToast(String text) {
        return mToast != null &&
                !StringUtils.isNullOrEmpty(mCurrentText) &&
                mCurrentText.equals(text);
    }

    /**
     * Show Toast with "long" duration.
     */
    public void showToast(@StringRes int stringId) {
        final String text = mContext.getResources().getString(stringId);
        showToast(text);
    }

    /**
     * Show Toast with "long" duration.
     */
    public void showToast(String text) {
        showToast(text, Toast.LENGTH_LONG);
    }

    /**
     * Show Toast with "short" duration.
     */
    public void showShortToast(String text) {
        showToast(text, Toast.LENGTH_SHORT);
    }

    public void cancel() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
