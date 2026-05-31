package com.example.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudyLog::class, CompletedChapter::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medprep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class StudyRepository(private val studyDao: StudyDao) {
    val allLogs = studyDao.getAllLogs()
    val completedChapters = studyDao.getCompletedChapters()

    suspend fun insertLog(log: StudyLog) {
        studyDao.insertLog(log)
    }

    suspend fun markChapterCompleted(chapterId: String) {
        studyDao.markChapterCompleted(CompletedChapter(chapterId = chapterId))
    }
}
