package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<StudyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudyLog)

    @Query("SELECT * FROM completed_chapters")
    fun getCompletedChapters(): Flow<List<CompletedChapter>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markChapterCompleted(completedChapter: CompletedChapter)
}
