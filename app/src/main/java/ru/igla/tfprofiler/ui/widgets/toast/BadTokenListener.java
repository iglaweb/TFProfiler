package ru.igla.tfprofiler.ui.widgets.toast;

import androidx.annotation.NonNull;
import android.widget.Toast;

/**
 * @author drakeet
 */
public interface BadTokenListener {
    void onBadTokenCaught(@NonNull Toast toast);
}
