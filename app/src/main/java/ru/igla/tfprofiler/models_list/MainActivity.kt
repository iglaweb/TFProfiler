package ru.igla.tfprofiler.models_list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.utils.inTransaction


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)
        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                replace(R.id.content, MainViewPagerFragment())
            }
        }
    }
}
