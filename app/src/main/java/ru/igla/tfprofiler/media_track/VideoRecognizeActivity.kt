package ru.igla.tfprofiler.media_track

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.MediaRequest
import ru.igla.tfprofiler.models_list.NeuralModelsListFragment
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.inTransaction

class VideoRecognizeActivity : AppCompatActivity() {

    private val mToaster: Toaster by lazy { Toaster(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val mediaRequest: MediaRequest? =
            intent?.getParcelableExtra(NeuralModelsListFragment.MEDIA_ITEM)
        mediaRequest ?: return

        if (mediaRequest.mediaPath.isEmpty()) {
            mToaster.showToast("File path is empty. Finish activity")
            finish()
            return
        }

        val delegateRunRequest: DelegateRunRequest? =
            intent.getParcelableExtra(NeuralModelsListFragment.MODEL_OPTIONS)
        check(delegateRunRequest != null) { "Delegate is not provided" }

        setContentView(R.layout.activity_video_layout)
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                replace(
                    R.id.main_frame_layout, VideoRecognizeFragment().withArguments(
                        mediaRequest,
                        delegateRunRequest
                    )
                )
            }
        }
    }
}
