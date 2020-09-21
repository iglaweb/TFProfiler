package ru.igla.tfprofiler.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ModelReportsDao {

    @Transaction
    @Query("SELECT * FROM $DELEGATE_REPORTS_TABLE")
    fun getReports(): LiveData<List<DbReportDelegateItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReports(delegateItem: List<DbReportDelegateItem>): List<Long>

    @Transaction
    @Query("SELECT * FROM $MODEL_REPORTS_TABLE")
    fun getReportWithDelegates(): LiveData<List<ModelReportWithDelegates>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReport(delegateItem: DbModelReportItem): Long

    @Query("DELETE FROM $MODEL_REPORTS_TABLE WHERE id_model_report = :reportId")
    fun deleteReportById(reportId: Long)

    @Query("DELETE FROM $DELEGATE_REPORTS_TABLE WHERE id_report_delegate = :delegateId")
    fun deleteReportDelegateById(delegateId: Long)
}