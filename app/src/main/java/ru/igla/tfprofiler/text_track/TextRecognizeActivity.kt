package ru.igla.tfprofiler.text_track

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.ExtraTextRequest
import ru.igla.tfprofiler.models_list.NeuralModelsListFragment
import ru.igla.tfprofiler.utils.inTransaction

class TextRecognizeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaRequest: ExtraTextRequest? =
            intent?.getParcelableExtra(NeuralModelsListFragment.MEDIA_ITEM)
        check(mediaRequest != null) { "MediaRequest is not provided" }

        val delegateRunRequest: DelegateRunRequest? =
            intent?.getParcelableExtra(NeuralModelsListFragment.MODEL_OPTIONS)
        check(delegateRunRequest != null) { "Delegate is not provided" }

        setContentView(R.layout.activity_text_layout)
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                replace(
                    R.id.main_frame_layout, TextRecognizeFragment().withArguments(
                        mediaRequest,
                        delegateRunRequest
                    )
                )
            }
        }
    }
}
