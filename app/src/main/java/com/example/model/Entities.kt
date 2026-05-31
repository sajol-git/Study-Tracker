package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chapterId: String,
    val durationMinutes: Int,
    val dateString: String,
    val sessionType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "completed_chapters")
data class CompletedChapter(
    @PrimaryKey val chapterId: String,
    val timestamp: Long = System.currentTimeMillis()
)
