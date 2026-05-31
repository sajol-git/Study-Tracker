package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppDatabase
import com.example.model.StudyLog
import com.example.model.StudyRepository
import com.example.model.SyllabusData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyRepository(database.studyDao())
    }

    val completedChapters: StateFlow<Set<String>> = repository.completedChapters
        .map { list -> list.map { it.chapterId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val studyLogs: StateFlow<List<StudyLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadInitialDataIfEmpty() {
        viewModelScope.launch {
            // Check auth state or initialize
        }
    }

    fun addStudyLog(chapterId: String, durationMinutes: Int, sessionType: String) {
        viewModelScope.launch {
            val dateString = SimpleDateFormat("dd MMM, yyyy", Locale("bn", "BD")).format(Date())
            repository.insertLog(StudyLog(
                chapterId = chapterId,
                durationMinutes = durationMinutes,
                dateString = dateString,
                sessionType = sessionType
            ))
            // Auto complete chapter if studied for a while (simplification)
            if (durationMinutes >= 25) {
                repository.markChapterCompleted(chapterId)
            }
        }
    }

    fun syncToCloud(userId: String, onComplete: (Boolean) -> Unit) {
        try {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)
            
            // Push logs
            val logsRef = userRef.child("studyLogs")
            studyLogs.value.forEach { log ->
                val logId = log.id.toString()
                logsRef.child(logId).setValue(log)
            }

            // Push completed chapters
            val chaptersRef = userRef.child("completedChapters")
            val chaptersList = completedChapters.value.toList()
            chaptersRef.setValue(chaptersList).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false)
        }
    }

    fun syncFromCloud(userId: String, onComplete: (Boolean) -> Unit) {
        try {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)
            
            userRef.child("studyLogs").get().addOnSuccessListener { snapshot ->
                viewModelScope.launch {
                    val logs = snapshot.children.mapNotNull { it.getValue(StudyLog::class.java) }
                    logs.forEach { repository.insertLog(it) }
                }
            }.addOnFailureListener {
                onComplete(false)
            }

            userRef.child("completedChapters").get().addOnSuccessListener { snapshot ->
                viewModelScope.launch {
                    val chaptersList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    chaptersList.forEach { repository.markChapterCompleted(it) }
                    onComplete(true)
                }
            }.addOnFailureListener {
                onComplete(false)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false)
        }
    }
}
