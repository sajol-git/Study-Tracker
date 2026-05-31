package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.model.Subject
import com.example.model.SyllabusData
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun SyllabusScreen(viewModel: MainViewModel, navController: NavController) {
    val completedChapters by viewModel.completedChapters.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("অধ্যায় ডিরেক্টরি", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("গুরুত্ব অনুযায়ী স্টার (★) যুক্ত", fontSize = 12.sp, color = TextSecondary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SyllabusData.subjects) { subject ->
                SubjectItem(subject, completedChapters, navController)
            }
        }
    }
}

@Composable
fun SubjectItem(subject: Subject, completedChapters: Set<String>, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    val icon = when {
        subject.name.contains("Botany") -> Icons.Filled.Eco // Using Eco for Botany
        subject.name.contains("Zoology") -> Icons.Filled.FilterCenterFocus // Approximation of DNA or Bug
        subject.name.contains("Chemistry") -> Icons.Filled.Science // Beaker
        subject.name.contains("Physics") -> Icons.Filled.Bolt // Lightning
        subject.name.contains("English") -> Icons.Filled.SortByAlpha
        else -> Icons.Filled.HelpOutline
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(subject.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("মোট অধ্যায়: ${subject.chapters.size} টি", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    subject.chapters.forEach { chapter ->
                        val isCompleted = completedChapters.contains(chapter.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCompleted) Color(0xFF064E3B).copy(alpha = 0.3f) else Color.Transparent)
                                .border(1.dp, if (isCompleted) AccentGreen.copy(alpha=0.5f) else BorderColor.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("session/${chapter.id}") }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(chapter.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isCompleted) AccentGreen else TextPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row {
                                        repeat(chapter.rating) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Text(chapter.description, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Outlined.PlayCircle, contentDescription = "Start Session", tint = AccentBlue, modifier = Modifier.padding(start=16.dp).size(28.dp))
                        }
                    }
                }
            }
        }
    }
}
