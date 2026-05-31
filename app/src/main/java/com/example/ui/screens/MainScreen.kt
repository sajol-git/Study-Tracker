package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.SyllabusData
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = DarkBg,
        topBar = { TopHeader(viewModel) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("syllabus") { SyllabusScreen(viewModel, navController) }
            composable("session/{chapterId}") { backStackEntry -> 
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "botany_ch1"
                SessionScreen(viewModel, navController, chapterId)
            }
            composable("session") { SessionScreen(viewModel, navController, "botany_ch1") }
            composable("analytics") { AnalyticsScreen(viewModel) }
            composable("dev") { DevScreen() }
        }
    }
}

@Composable
fun TopHeader(viewModel: MainViewModel) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var showSyncDialog by remember { mutableStateOf(false) }

    if (showSyncDialog) {
        SyncDialog(
            onDismiss = { showSyncDialog = false },
            viewModel = viewModel
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
            currentDate = SimpleDateFormat("dd MMM, yyyy", Locale("bn", "BD")).format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
    ) {
        // Back/Close Icon
        IconButton(
            onClick = { /* Handle Close */ },
            modifier = Modifier.padding(top = 16.dp, start = 8.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF2DD4BF), Color(0xFF0EA5E9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Column {
                    Text("MED-PREP", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentGreen))
                        Text("টার্গেট: ঢাকা মেডিকেল", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGreen, letterSpacing = 0.sp)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { showSyncDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.CloudSync, contentDescription = "Sync", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(currentTime, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(currentDate, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                }
            }
        }
        Divider(color = BorderColor, thickness = 1.dp)
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = CardBg,
        contentColor = TextSecondary,
        tonalElevation = 0.dp,
        modifier = Modifier.background(CardBg)
    ) {
        val items = listOf(
            NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, "dashboard"),
            NavItem("সিলেবাস", Icons.Filled.MenuBook, "syllabus"),
            NavItem("সেশন", Icons.Filled.HourglassEmpty, "session"),
            NavItem("বিশ্লেষণ", Icons.Filled.Analytics, "analytics"),
            NavItem("কোড", Icons.Filled.Code, "dev")
        )

        items.forEach { item ->
            val isSelected = currentRoute?.startsWith(item.route) == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentBlue,
                    selectedTextColor = AccentBlue,
                    indicatorColor = AccentBlue.copy(alpha = 0.2f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)
