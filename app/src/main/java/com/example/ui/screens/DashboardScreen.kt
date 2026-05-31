package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SyllabusData
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val completedChapters by viewModel.completedChapters.collectAsStateWithLifecycle()
    val studyLogs by viewModel.studyLogs.collectAsStateWithLifecycle()

    val totalStudyMinutes = studyLogs.sumOf { it.durationMinutes }
    val totalHours = totalStudyMinutes / 60.0
    val targetHours = 8.0
    val progress = (totalHours / targetHours).coerceAtMost(1.0).toFloat()
    
    val coreSubjectsNames = listOf("Botany (উদ্ভিদবিজ্ঞান)", "Zoology (প্রাণিবিজ্ঞান)", "Chemistry 1st Paper (রসায়ন ১ম পত্র)", "Chemistry 2nd Paper (রসায়ন ২য় পত্র)", "Physics 1st Paper (পদার্থবিজ্ঞান ১ম পত্র)", "Physics 2nd Paper (পদার্থবিজ্ঞান ২য় পত্র)")
    val coreChapters = SyllabusData.subjects.filter { it.name in coreSubjectsNames }.flatMap { it.chapters }
    val completedCoreCount = coreChapters.count { completedChapters.contains(it.id) }
    val coreProgress = if (coreChapters.isNotEmpty()) completedCoreCount.toFloat() / coreChapters.size else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Overview Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("আজকের স্টাডি", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                        Text("${String.format("%.1f", totalHours)}", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Text(" ঘণ্টা", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(6.dp).clip(RoundedCornerShape(50)),
                        color = AccentGreen,
                        trackColor = BorderColor
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("৮ ঘণ্টার টার্গেট", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Text("${String.format("%.1f", progress * 100)}%", fontSize = 32.sp, color = AccentAmber, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
                    Text("বাকি আছে ${String.format("%.1f", (targetHours - totalHours).coerceAtLeast(0.0))} ঘণ্টা", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }

        // Daily Target Circular Ring
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Surface(color = Color(0xFF452A15), shape = RoundedCornerShape(50)) {
                        Text("Daily Progress", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Text("আজকের লড়াকু দিন!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(top = 12.dp))
                    Text("প্রতিদিন ৮ ঘণ্টা মন দিয়ে পড়াই আপনার কাঙ্ক্ষিত মেডিকেলের স্বপ্ন পূরণ করবে।", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
                }
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = BorderColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = AccentAmber,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = 2.dp)) {
                        Text("${String.format("%.1f", totalHours)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("ঘণ্টা", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }
            }
        }

        // Syllabus Completion
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("কোর সিলেবাস অগ্রগতি", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    }
                    Surface(color = Color(0xFF1E3A8A).copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
                        Text("${String.format("%.1f", coreProgress * 100)}%", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                
                Text("শুধু জীব, রসায়ন ও পদার্থ হিসাবের আওতায় (জিকে ও ইংরেজি মুক্ত)", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { coreProgress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                    color = AccentGreen,
                    trackColor = BorderColor
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("কমপ্লিট: ${completedCoreCount}টি অধ্যায়", fontSize = 12.sp, color = TextSecondary)
                    Text("অবশিষ্ট: ${coreChapters.size - completedCoreCount}টি অধ্যায়", fontSize = 12.sp, color = TextSecondary)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val botanyChapters = SyllabusData.subjects.find { it.name.contains("Botany") }?.chapters ?: emptyList()
                    val zoologyChapters = SyllabusData.subjects.find { it.name.contains("Zoology") }?.chapters ?: emptyList()
                    val bioChapters = botanyChapters + zoologyChapters
                    val completedBio = bioChapters.count { completedChapters.contains(it.id) }
                    
                    val chem1Chapters = SyllabusData.subjects.find { it.name.contains("Chemistry 1st") }?.chapters ?: emptyList()
                    val chem2Chapters = SyllabusData.subjects.find { it.name.contains("Chemistry 2nd") }?.chapters ?: emptyList()
                    val chemChapters = chem1Chapters + chem2Chapters
                    val completedChem = chemChapters.count { completedChapters.contains(it.id) }
                    
                    val phys1Chapters = SyllabusData.subjects.find { it.name.contains("Physics 1st") }?.chapters ?: emptyList()
                    val phys2Chapters = SyllabusData.subjects.find { it.name.contains("Physics 2nd") }?.chapters ?: emptyList()
                    val physChapters = phys1Chapters + phys2Chapters
                    val completedPhys = physChapters.count { completedChapters.contains(it.id) }

                    // To match user's screenshot visually if we can, or just dynamic values:
                    fun formatNum(num: Int) = String.format("%02d", num)

                    @Composable
                    fun SubjectCard(name: String, completed: Int, total: Int, modifier: Modifier) {
                        Column(
                            modifier = modifier
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            Text("${formatNum(completed)} / ${formatNum(total)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AccentBlue)
                        }
                    }

                    SubjectCard(name = "জীববিজ্ঞান", completed = completedBio, total = bioChapters.size, modifier = Modifier.weight(1f))
                    SubjectCard(name = "রসায়ন", completed = completedChem, total = chemChapters.size, modifier = Modifier.weight(1f))
                    SubjectCard(name = "পদার্থবিজ্ঞান", completed = completedPhys, total = physChapters.size, modifier = Modifier.weight(1f))
                }
            }
        }

        // Motivation
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AccentGreen, modifier = Modifier.padding(end = 16.dp))
                Column {
                    Text("আজকের মাস্টার সাজেশন", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("বোটানির 'কোষ ও এর গঠন' এবং কেমিস্ট্রির 'জৈব রসায়ন' থেকে বিগত ৫ বছরে ৭টির বেশি কোশ্চেন এসেছে। এগুলো আজকে ভালো করে রিভিশন দিন!", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp), lineHeight = 20.sp)
                }
            }
        }
    }
}
