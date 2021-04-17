package ru.igla.tfprofiler.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DbReportDelegateItem::class,
        DbModelReportItem::class,
        DbModelItem::class
    ], version = 3
)
@TypeConverters(
    ColorSpaceTypeConverter::class,
    ModelTypeConverter::class,
    DeviceConverter::class,
    ModelFormatConverter::class,
    InputShapeTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "tfprofiler-database"
            )
                // Wipes and rebuilds instead of migrating
                // if no Migration object.
                // Migration is not part of this practical.
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract val modelReportsDao: ModelReportsDao
    abstract val modelsDao: ModelsDao
}