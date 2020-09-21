package ru.igla.tfprofiler.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.utils.sendValueIfNew


class SharedViewModel(application: Application) : AndroidViewModel(application) {
    val modelsLiveData = MutableLiveData<ListReportEntity>()

    fun setModelData(entity: ListReportEntity) {
        modelsLiveData.sendValueIfNew(entity)
    }
}