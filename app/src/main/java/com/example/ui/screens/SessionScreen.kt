package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.model.SyllabusData
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(viewModel: MainViewModel, navController: NavController, chapterId: String) {
    val chapterInfo = SyllabusData.getChapterById(chapterId)
    if (chapterInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chapter not found", color = TextPrimary)
        }
        return
    }

    val (subject, chapter) = chapterInfo

    var sessionMode by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(sessionMode == null) }
    var isRunning by remember { mutableStateOf(false) }
    var timeElapsedInSeconds by remember { mutableStateOf(0) }
    var sessionTargetSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            timeElapsedInSeconds++
            if (sessionMode == "পোমোডোরো সেশন" && timeElapsedInSeconds >= sessionTargetSeconds) {
                isRunning = false
                viewModel.addStudyLog(chapter.id, timeElapsedInSeconds / 60, sessionMode!!)
                // Could ring a bell here
            }
        }
    }

    if (showDialog) {
        SessionConfigDialog(
            chapterTitle = chapter.name,
            onDismiss = {
                if (sessionMode == null) navController.popBackStack() 
                showDialog = false 
            },
            onSelectMode = { mode ->
                sessionMode = mode
                timeElapsedInSeconds = 0
                sessionTargetSeconds = if (mode == "পোমোডোরো সেশন") 25 * 60 else Int.MAX_VALUE
                isRunning = true
                showDialog = false
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isLandscape = maxWidth > maxHeight

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SessionInfo(sessionMode, subject.name, chapter.name)
                        Spacer(modifier = Modifier.height(32.dp))
                        SessionActions(isRunning, { isRunning = !isRunning }) {
                            isRunning = false
                            if (timeElapsedInSeconds > 60) {
                                viewModel.addStudyLog(chapter.id, timeElapsedInSeconds / 60, sessionMode ?: "স্বাভাবিক সেশন")
                            }
                            navController.popBackStack()
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SessionTimerDisplay(sessionMode, timeElapsedInSeconds, sessionTargetSeconds)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    SessionInfo(sessionMode, subject.name, chapter.name)
                    SessionTimerDisplay(sessionMode, timeElapsedInSeconds, sessionTargetSeconds)
                    SessionActions(isRunning, { isRunning = !isRunning }) {
                        isRunning = false
                        if (timeElapsedInSeconds > 60) {
                            viewModel.addStudyLog(chapter.id, timeElapsedInSeconds / 60, sessionMode ?: "স্বাভাবিক সেশন")
                        }
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun SessionInfo(sessionMode: String?, subjectName: String, chapterName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = AccentGreen.copy(alpha = 0.1f), 
            shape = RoundedCornerShape(50), 
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.2f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Text(sessionMode ?: "সেশন", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(subjectName.uppercase(), fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(chapterName, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun SessionTimerDisplay(sessionMode: String?, timeElapsedInSeconds: Int, sessionTargetSeconds: Int) {
    Box(
        modifier = Modifier
            .size(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val progress = if (sessionMode == "পোমোডোরো সেশন" && sessionTargetSeconds > 0) {
            (timeElapsedInSeconds.toFloat() / sessionTargetSeconds).coerceIn(0f, 1f)
        } else {
            (timeElapsedInSeconds % 3600) / 3600f // Resets every hour visually for other modes
        }
        val timerColor = if (sessionMode == "পোমোডোরো সেশন") AccentBlue else AccentAmber

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = BorderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = 360f * (if (sessionMode == "পোমোডোরো সেশন") (1f - progress) else progress),
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayTime = if (sessionMode == "পোমোডোরো সেশন") {
                maxOf(0, sessionTargetSeconds - timeElapsedInSeconds)
            } else {
                timeElapsedInSeconds
            }
            val minutes = displayTime / 60
            val seconds = displayTime % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                "চলছে...", 
                fontSize = 10.sp, 
                color = timerColor, 
                letterSpacing = 4.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SessionActions(isRunning: Boolean, onToggle: () -> Unit, onStop: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRunning) "থামুন" else "শুরু করুন", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha=0.2f)),
            border = BorderStroke(1.dp, AccentRed.copy(alpha=0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Filled.StopCircle, contentDescription = null, tint = AccentRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("শেষ করুন", color = AccentRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SessionConfigDialog(chapterTitle: String, onDismiss: () -> Unit, onSelectMode: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(AccentAmber.copy(alpha=0.1f)), contentAlignment=Alignment.Center) {
                    Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = AccentAmber)
                }
                Text(chapterTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                Text("সেশনের ধরণ নির্বাচন করুন", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SessionModeOption("স্বাভাবিক সেশন", "স্বাভাবিকভাবে স্টপ না করা পর্যন্ত সময় কাউন্ট হবে", Icons.Filled.Timer, AccentBlue) { onSelectMode("স্বাভাবিক সেশন") }
                    SessionModeOption("পোমোডোরো সেশন", "২৫ মিনিট পড়াশোনা + ৫ মিনিট বিশ্রামের সাইকেল", Icons.Filled.BusinessCenter, AccentGreen) { onSelectMode("পোমোডোরো সেশন") }
                    SessionModeOption("ডিপ ওয়ার্ক", "নিবিড় মনোযোগ দিয়ে একটানা সময় ট্র্যাকিং", Icons.Filled.Lock, AccentAmber) { onSelectMode("ডিপ ওয়ার্ক") }
                }
            }
        }
    }
}

@Composable
fun SessionModeOption(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1F2937))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 9.sp, color = TextSecondary)
        }
    }
}
