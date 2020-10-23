package ru.igla.tfprofiler.db

import androidx.room.Embedded
import androidx.room.Relation

data class ModelReportWithDelegates(
    @Embedded val modelReportItem: DbModelReportItem,
    @Relation(
        parentColumn = "id_model_report",
        entityColumn = "model_report_id"
    )
    val reportDelegateItems: List<DbReportDelegateItem>,

    @Relation(
        parentColumn = "model_id",
        entityColumn = "id_model"
    )
    val modelItem: DbModelItem
)