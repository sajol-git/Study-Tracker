package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SyllabusData
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val studyLogs by viewModel.studyLogs.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("দৈনিক") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("স্টাডি ট্র্যাকিং লগ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F2937))
                    .padding(2.dp)
            ) {
                listOf("দৈনিক", "সাপ্তাহিক", "মাসিক").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CardBg else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(tab, fontSize = 12.sp, color = if (isSelected) AccentBlue else TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        val totalMinutes = studyLogs.sumOf { it.durationMinutes }
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$selectedTab টার্গেট অর্জন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("মোট ${String.format("%.1f", totalMinutes / 60.0)} ঘণ্টা", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                }

                // Real bar chart data from studyLogs
                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp).padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Compute last 7 days data
                    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    val historyMap = studyLogs.groupBy { it.dateString }
                    val cal = Calendar.getInstance()
                    
                    val days = mutableListOf<String>()
                    val heights = mutableListOf<Float>()
                    
                    val dayFormat = SimpleDateFormat("E", Locale.getDefault())
                    
                    for (i in 6 downTo 0) {
                        val d = cal.clone() as Calendar
                        d.add(Calendar.DAY_OF_YEAR, -i)
                        val dateKey = sdf.format(d.time)
                        val dayLabel = if (i == 0) "আজ" else dayFormat.format(d.time)
                        
                        val logsForDay = historyMap[dateKey] ?: emptyList()
                        val hours = logsForDay.sumOf { it.durationMinutes } / 60f
                        
                        days.add(dayLabel)
                        heights.add(hours)
                    }

                    val maxVal = heights.maxOrNull()?.takeIf { it > 0 } ?: 1f
                    
                    heights.forEachIndexed { index, value ->
                        val ratio = (value / maxVal).coerceIn(0f, 1f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Text(String.format(Locale.getDefault(), "%.1f", value), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFF2DD4BF), Color(0xFF3B82F6))
                            )
                            val bgBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFF1F2937), Color(0xFF1F2937)) // Dark background behind
                            )
                            Box(modifier = Modifier.fillMaxWidth(0.5f).weight(1f, fill=false).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(bgBrush))
                                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(ratio).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(brush))
                            }
                            Text(days[index], fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }

        Text("স্টাডি সেশন হিস্ট্রি (ইংরেজি ও জিকে সহ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(studyLogs) { log ->
                val chapterData = SyllabusData.getChapterById(log.chapterId)
                val subjectName = chapterData?.first?.name ?: "অজানা সাবজেক্ট"
                val shortSubject = subjectName.substringAfter("(").substringBefore(")")
                val isCore = listOf("Botany", "Zoology", "Chemistry", "Physics").any { subjectName.contains(it) }
                val tagText = if (isCore) "কোর সাবজেক্ট" else "অন্যান্য"
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(chapterData?.second?.name ?: "অধ্যায়", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF1D4ED8).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text(tagText, color = Color(0xFF93C5FD), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("$shortSubject • ${log.dateString}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${log.durationMinutes} মি.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                            Spacer(Modifier.height(4.dp))
                            Text(log.sessionType, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ভাইব কোডার ইন্টিগ্রেশন", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text("এই অ্যাপ্লিকেশনের পে-লোড রিয়েল-টাইম ডাটা মডেল", fontSize = 10.sp, color = TextSecondary)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("APP_STATE_PAYLOAD.json", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("Copy", fontSize = 11.sp, color = AccentBlue)
                }
                Text(
                    text = "{\n  \"status\": \"production_ready\",\n  \"models_used\": \"RoomDB, Firebase, Jetpack Compose\"\n}",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                )
            }
        }
    }
}
