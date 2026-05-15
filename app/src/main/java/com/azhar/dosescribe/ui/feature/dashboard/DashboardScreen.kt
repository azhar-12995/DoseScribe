package com.azhar.dosescribe.ui.feature.dashboard

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.azhar.dosescribe.R
import com.azhar.dosescribe.data.model.Banner
import com.azhar.dosescribe.ui.feature.lessons.LessonProgressViewModel
import com.azhar.dosescribe.ui.util.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ── Brand colours ──────────────────────────────────────────────────────
private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueDark = Color(0xFF076D9C)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val CardGreen = Color(0xFF2E9E6E)
private val CardPurple = Color(0xFF7B5EBF)
private val CardOrange = Color(0xFFF0A030)
private val CardTeal = Color(0xFF0B78B6)
private val SurfaceBg = Color(0xFFF5F7FA)

// ── Data class for a lesson card ───────────────────────────────────────
data class ModuleInfo(
    val id: String,
    val title: String,
    val description: String,
    val progress: Float,
    val progressColor: Color,
    val icon: ImageVector,
    val iconBg: Color
)

val allModules = listOf(
    ModuleInfo("auxiliary_labels", "Auxiliary Labels", "Proper labeling of medication containers.", 0.0f, BrandBlue,
        Icons.AutoMirrored.Filled.Label, BrandBlue),
    ModuleInfo("checklist", "Checklist", "Verification procedures for dispensing.", 0.0f, CardGreen, Icons.Filled.Checklist, CardGreen),
    ModuleInfo("compounding_calculations", "Compounding Calculations", "Mathematical calculations for compounding.", 0.0f, CardPurple, Icons.Filled.Calculate, CardPurple),
    ModuleInfo("chemo_dose_adjustments", "Chemo Dose Adjustments", "Chemotherapy dosage modification protocols.", 0.0f, CardOrange, Icons.Filled.Science, CardOrange),
    ModuleInfo("appropriateness_review", "Appropriateness Review", "Evaluating medication order appropriateness.", 0.0f, CardTeal, Icons.Filled.RateReview, CardTeal),
    ModuleInfo("counseling", "Counseling", "Patient counseling techniques and protocols.", 0.0f, BrandBlue, Icons.Filled.RecordVoiceOver, BrandBlue),
    ModuleInfo("drug_label", "Drug Label", "Reading and interpreting drug labels.", 0.0f, CardGreen, Icons.Filled.Description, CardGreen),
    ModuleInfo("hepatic_dose_adjustment", "Hepatic Dose Adjustment", "Dosage adjustments for liver impairment.", 0.0f, CardPurple, Icons.Filled.LocalHospital, CardPurple),
    ModuleInfo("electrolyte_replacement", "Electrolyte Replacement", "Electrolyte replacement therapy protocols.", 0.0f, CardOrange, Icons.Filled.WaterDrop, CardOrange),
    ModuleInfo("high_alert_medications", "High Alert Medications", "Identification and handling of high-alert drugs.", 0.0f, Color(0xFFC62828), Icons.Filled.Warning, Color(0xFFC62828)),
    ModuleInfo("pediatric_dose_adjustment", "Pediatric Dose Adjustment", "Dosage calculations for pediatric patients.", 0.0f, CardTeal, Icons.Filled.ChildCare, CardTeal),
    ModuleInfo("lab_interpretation", "Lab Interpretation", "Interpreting laboratory test results.", 0.0f, BrandBlue, Icons.Filled.Biotech, BrandBlue),
    ModuleInfo("iv_to_oral_switch", "IV to Oral Switch", "Converting IV medications to oral forms.", 0.0f, CardGreen, Icons.Filled.SwapHoriz, CardGreen),
    ModuleInfo("parts_of_prescription", "Parts of Prescription", "Understanding prescription components.", 0.0f, CardPurple, Icons.Filled.Receipt, CardPurple),
    ModuleInfo("narcotic_controlled_medications", "Narcotic & Controlled Medications", "Handling narcotic and controlled substances.", 0.0f, CardOrange, Icons.Filled.Security, CardOrange),
    ModuleInfo("renal_dose_adjustment", "Renal Dose Adjustment", "Dosage adjustments for kidney impairment.", 0.0f, CardTeal, Icons.Filled.MonitorHeart, CardTeal),
    ModuleInfo("vancomycin_dose_adjustment", "Vancomycin Dose Adjustment", "Vancomycin therapeutic dose monitoring.", 0.0f, BrandBlue, Icons.Filled.Medication, BrandBlue),
    ModuleInfo("tdm_therapeutic_dose_adjustment", "TDM Therapeutic Dose Adjustment", "Therapeutic drug monitoring protocols.", 0.0f, CardGreen, Icons.Filled.Analytics, CardGreen)
)

// ── Drawer menu items ─────────────────────────────────────────────────
data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val drawerMenuItems = listOf(
    DrawerMenuItem("Home", Icons.Filled.Home, "dashboard"),
    DrawerMenuItem("Lessons", Icons.AutoMirrored.Filled.MenuBook, "lessons"),
    DrawerMenuItem("Progress", Icons.AutoMirrored.Filled.TrendingUp, "progress"),
    DrawerMenuItem("Certificates", Icons.Filled.EmojiEvents, "certificates"),
    DrawerMenuItem("Tests", Icons.Filled.Quiz, "tests"),
    DrawerMenuItem("Notifications", Icons.Filled.Notifications, "notifications"),
    DrawerMenuItem("Feedback", Icons.Filled.Feedback, "feedback"),
    DrawerMenuItem("Settings", Icons.Filled.Settings, "settings"),
    DrawerMenuItem("Account", Icons.Filled.Person, "account"),
)

// ── Avatar helper ──────────────────────────────────────────────────────
val pharmacistAvatarIcons = listOf(
    Icons.Filled.Person,
    Icons.Filled.LocalPharmacy,
    Icons.Filled.Science,
    Icons.Filled.HealthAndSafety,
    Icons.Filled.MedicalServices,
    Icons.Filled.Medication
)

// ════════════════════════════════════════════════════════════════════════
//  DASHBOARD SCREEN
// ════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val avatarId by viewModel.avatarId.collectAsState()
    val profileImageBase64 by viewModel.profileImageBase64.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val unreadCount by viewModel.unreadNotifications.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    @Suppress("UNUSED_VARIABLE")
    val progressVersion = progressVm.version
    val overallProgress = progressVm.getOverallProgress()
    val completedCount = progressVm.getCompletedLessonCount()
    val inProgressModules = progressVm.getInProgressModules()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.onSignOutClick() }) {
                    Text("Logout", color = Color(0xFFC62828))
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
                else -> Unit
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp), // slightly wider
                        drawerShape = androidx.compose.ui.graphics.RectangleShape,
                        windowInsets = WindowInsets(0, 0, 0, 0) // Remove default insets for full background
                    ) {
                        DrawerContent(
                            selectedRoute = "dashboard",
                            completedCount = completedCount,
                            totalLessons = allModules.size,
                            onItemClick = { route ->
                                scope.launch { drawerState.close() }
                                if (route != "dashboard") navController.navigate(route)
                            },
                            onLogoutClick = {
                                scope.launch { drawerState.close() }
                                showLogoutDialog = true
                            }
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(containerColor = SurfaceBg) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Top Bar ──
                        TopGreetingBar(
                            userName = userName,
                            avatarId = avatarId,
                            profileImageBase64 = profileImageBase64,
                            unreadCount = unreadCount,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onProfileClick = { navController.navigate("account") },
                            onNotificationClick = { navController.navigate("notifications") }
                        )

                        // ── Learning Progress Card ──
                        LearningProgressCard(
                            progress = overallProgress,
                            completedCount = completedCount,
                            totalLessons = allModules.size,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )

                        // ── Trending Lessons (auto-sliding pager, full width) ──
                        Text(
                            "🔥 Trending Lessons",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                        val trendingModules = listOf(allModules[0], allModules[3], allModules[9], allModules[5])
                        TrendingLessonsPager(
                            modules = trendingModules,
                            onClick = { module -> navController.navigate("lesson_steps/${module.id}") }
                        )

                        // ── Quick Simulation Card (redesigned - clinical style) ──
                        QuickSimulationCardRedesigned(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        // ── Lessons Header ──
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lessons", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            TextButton(onClick = { navController.navigate("lessons") }) {
                                Text("View All", color = BrandBlue)
                            }
                        }

                        // ── Lesson Cards ──
                        // Show at least 4 lessons: in-progress first, then next not-started ones
                        val nonCompletedModules = allModules.filter { !progressVm.isModuleCompleted(it.id) }
                        val displayModules = nonCompletedModules.take(4).let { initial ->
                            if (initial.size < 4) {
                                // If fewer than 4 non-completed, pad with completed ones from end
                                val remaining = allModules.filter { mod -> mod !in initial }.take(4 - initial.size)
                                initial + remaining
                            } else initial
                        }
                        displayModules.forEach { module ->
                            val moduleProgress = progressVm.getProgress(module.id)
                            LessonCardPortrait(
                                module = module,
                                progress = moduleProgress,
                                onClick = { navController.navigate("lesson_steps/${module.id}") },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  BANNER CAROUSEL (auto-slide + swipe + dots)
// ════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(banners: List<Banner>, onBannerClick: (Banner) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState, banners.size) {
        while (true) {
            delay(4000)
            if (banners.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier.fillMaxSize().clickable { onBannerClick(banner) },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.6f)), startY = 100f)
                        )
                    )
                    Text(
                        banner.title,
                        modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .size(if (index == pagerState.currentPage) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.currentPage) BrandBlue else Color.LightGray)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TRENDING LESSONS PAGER (auto-slide, full width)
// ════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrendingLessonsPager(modules: List<ModuleInfo>, onClick: (ModuleInfo) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { modules.size })

    LaunchedEffect(pagerState, modules.size) {
        while (true) {
            delay(5000)
            if (modules.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % modules.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            TrendingLessonCard(module = modules[page], onClick = { onClick(modules[page]) })
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), horizontalArrangement = Arrangement.Center) {
            repeat(modules.size) { index ->
                Box(
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .size(if (index == pagerState.currentPage) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.currentPage) BrandBlue else Color.LightGray)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  DRAWER CONTENT
// ════════════════════════════════════════════════════════════════════════
@Composable
fun DrawerContent(
    selectedRoute: String,
    completedCount: Int,
    totalLessons: Int,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight().background(Color.White)) {
        // Updated Header UI to be more "proper" and full-width
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrandBlue
        ) {
            Column(modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "DOSESCRIBE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Learn, Simulate, Improve",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(28.dp))
                
                Text(
                    "$completedCount/$totalLessons Lessons Completed",
                    color = Color.White.copy(0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (totalLessons > 0) completedCount.toFloat() / totalLessons else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        drawerMenuItems.forEach { item ->
            val isSelected = item.route == selectedRoute
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = null, tint = if (isSelected) BrandBlue else Color.Gray) },
                label = { Text(item.label, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) BrandBlue else Color.DarkGray) },
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = BrandBlueLight, unselectedContainerColor = Color.Transparent)
            )
        }

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFC62828)) },
            label = { Text("Logout", color = Color(0xFFC62828)) },
            selected = false, onClick = onLogoutClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TOP GREETING BAR (with notification bell + badge)
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun TopGreetingBar(
    userName: String, avatarId: Int, profileImageBase64: String, unreadCount: Int,
    onMenuClick: () -> Unit, onProfileClick: () -> Unit, onNotificationClick: () -> Unit
) {
    Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(BrandBlueLight).clickable { onProfileClick() }, contentAlignment = Alignment.Center) {
                if (profileImageBase64.isNotBlank()) {
                    val imageBytes = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Profile", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    }
                } else {
                    val avatarIcon = pharmacistAvatarIcons.getOrElse(avatarId) { Icons.Filled.Person }
                    Icon(avatarIcon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Hi, $userName", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.width(4.dp))
                    Text("👋", fontSize = 16.sp)
                }
                Text(text = "Pharmacist", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onNotificationClick) {
                BadgedBox(badge = {
                    if (unreadCount > 0) Badge(containerColor = Color(0xFFC62828)) { Text("$unreadCount", color = Color.White, fontSize = 10.sp) }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.DarkGray)
                }
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.DarkGray)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  LEARNING PROGRESS CARD
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun LearningProgressCard(progress: Float, completedCount: Int, totalLessons: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(130.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BrandBlue)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("Learning Progress", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            Text("$completedCount/$totalLessons Lessons Completed", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodySmall)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(progress * 100).toInt()}% Keep Going", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    Text("Your Goal progress", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = Color.White, trackColor = Color.White.copy(alpha = 0.25f))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  RESUME LEARNING CARD
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun ResumeLearningCard(lessonName: String, progress: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)).background(BrandBlueLight), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Book, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(lessonName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = BrandBlue, trackColor = BrandBlueLight)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resume Learning", color = BrandBlue, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.East, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TRENDING LESSON CARD (used in pager, full width)
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun TrendingLessonCard(module: ModuleInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(module.iconBg.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(module.iconBg.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(module.icon, null, tint = module.iconBg, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = CardOrange.copy(alpha = 0.15f)) {
                        Text("🔥 Trending", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = CardOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(module.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(module.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Start Learning", color = BrandBlue, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.East, null, tint = BrandBlue, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  QUICK SIMULATION CARD (REDESIGNED - Clinical Case Style)
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun QuickSimulationCardRedesigned(modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Brush.horizontalGradient(listOf(BrandBlue, CardTeal, CardGreen))))

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF3E0)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Assignment, null, tint = CardOrange, modifier = Modifier.size(28.dp)) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clinical Simulation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Practice real-world pharmacy cases", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SimFeatureChip(Icons.Filled.Person, "Patient Case")
                    SimFeatureChip(Icons.Filled.Receipt, "Prescription")
                    SimFeatureChip(Icons.Filled.Science, "Lab Results")
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Case Simulation", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SimFeatureChip(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = BrandBlueLight) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

// ════════════════════════════════════════════════════════════════════════
//  LESSON CARD - PORTRAIT
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun LessonCardPortrait(module: ModuleInfo, progress: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(module.iconBg.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(module.icon, contentDescription = null, tint = module.iconBg, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(module.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            CircularProgressRing(progress = progress, color = module.progressColor, size = 46.dp, strokeWidth = 4.dp)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  CIRCULAR PROGRESS RING
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun CircularProgressRing(progress: Float, color: Color, size: Dp = 48.dp, strokeWidth: Dp = 4.dp) {
    val trackColor = color.copy(alpha = 0.12f)
    val pct = "${(progress * 100).toInt()}%"
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
            drawArc(color = color, startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
        }
        Text(pct, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = color)
    }
}