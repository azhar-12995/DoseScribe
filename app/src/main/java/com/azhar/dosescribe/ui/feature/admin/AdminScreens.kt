package com.azhar.dosescribe.ui.feature.admin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.azhar.dosescribe.data.model.AppNotification
import com.azhar.dosescribe.data.model.Banner
import com.azhar.dosescribe.data.model.Feedback
import com.azhar.dosescribe.data.model.User
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.domain.repository.BannerRepository
import com.azhar.dosescribe.domain.repository.FeedbackRepository
import com.azhar.dosescribe.domain.repository.NotificationRepository
import com.azhar.dosescribe.ui.feature.dashboard.allModules
import com.azhar.dosescribe.ui.feature.lessons.McqQuestion
import com.azhar.dosescribe.ui.feature.lessons.getMcqsForModule
import com.azhar.dosescribe.ui.util.UiEvent
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

// ── Brand colours ──────────────────────────────────────────────────
private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)
private val CardGreen = Color(0xFF2E9E6E)
private val CardPurple = Color(0xFF7B5EBF)
private val CardOrange = Color(0xFFF0A030)
private val CardRed = Color(0xFFC62828)

// ── Data class for full user progress ─────────────────────────────
data class UserModuleProgress(
    val moduleId: String,
    val completedSteps: List<Int> = emptyList(),
    val preAnswers: List<Int> = emptyList(),
    val postAnswers: List<Int> = emptyList()
)

// ════════════════════════════════════════════════════════════════════
//  ADMIN DASHBOARD VIEW MODEL
// ════════════════════════════════════════════════════════════════════
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bannerRepository: BannerRepository,
    private val notificationRepository: NotificationRepository,
    private val feedbackRepository: FeedbackRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners = _banners.asStateFlow()

    private val _feedback = MutableStateFlow<List<Feedback>>(emptyList())
    val feedback = _feedback.asStateFlow()

    private val _allNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()

    init {
        loadUsers()
        loadBanners()
        loadFeedback()
        loadAllNotifications()
    }

    private fun loadUsers() {
        authRepository.getAllUsers().onEach { result ->
            result.onSuccess { _users.value = it.filter { u -> u.role != "admin" && u.email.lowercase() != "admin@a.com" } }
        }.launchIn(viewModelScope)
    }

    private fun loadBanners() {
        bannerRepository.getBanners().onEach { result ->
            result.onSuccess { _banners.value = it }
        }.launchIn(viewModelScope)
    }

    private fun loadFeedback() {
        feedbackRepository.getAllFeedback().onEach { result ->
            result.onSuccess { _feedback.value = it }
        }.launchIn(viewModelScope)
    }

    private fun loadAllNotifications() {
        firestore.collection("notifications")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _allNotifications.value = list
            }
    }

    fun addBanner(banner: Banner) {
        bannerRepository.addBanner(banner).onEach {}.launchIn(viewModelScope)
    }

    fun deleteBanner(bannerId: String) {
        bannerRepository.deleteBanner(bannerId).onEach {}.launchIn(viewModelScope)
    }

    fun toggleBannerActive(banner: Banner) {
        val updated = banner.copy(isActive = !banner.isActive)
        bannerRepository.updateBanner(updated).onEach {}.launchIn(viewModelScope)
    }

    fun sendNotification(title: String, message: String, targetUserIds: List<String>, lessonId: String = "") {
        val notification = AppNotification(
            title = title,
            message = message,
            lessonId = lessonId,
            sentAt = Timestamp.now()
        )
        notificationRepository.sendNotification(notification, targetUserIds).onEach {}.launchIn(viewModelScope)
    }

    fun replyToFeedback(feedbackItem: Feedback, reply: String) {
        feedbackRepository.replyToFeedback(feedbackItem.id, reply).onEach { result ->
            result.onSuccess {
                val notification = AppNotification(
                    title = "Feedback Reply",
                    message = reply,
                    sentAt = Timestamp.now()
                )
                notificationRepository.sendNotification(notification, listOf(feedbackItem.userId))
                    .onEach {}.launchIn(viewModelScope)
            }
        }.launchIn(viewModelScope)
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiEvent.emit(UiEvent.Navigate("signin"))
        }
    }

    fun getUserProgress(userId: String, onResult: (Map<String, Any>) -> Unit) {
        firestore.collection("users").document(userId).collection("progress")
            .get()
            .addOnSuccessListener { snapshot ->
                val progressMap = mutableMapOf<String, Any>()
                for (doc in snapshot.documents) {
                    val steps = (doc.get("completedSteps") as? List<*>)?.size ?: 0
                    progressMap[doc.id] = steps
                }
                onResult(progressMap)
            }
    }

    fun getUserFullProgress(userId: String, onResult: (List<UserModuleProgress>) -> Unit) {
        firestore.collection("users").document(userId).collection("progress")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    UserModuleProgress(
                        moduleId = doc.id,
                        completedSteps = (doc.get("completedSteps") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                        preAnswers = (doc.get("preAnswers") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                        postAnswers = (doc.get("postAnswers") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                    )
                }
                onResult(list)
            }
    }

    fun getAllUsersProgress(onResult: (Map<String, List<UserModuleProgress>>) -> Unit) {
        val result = mutableMapOf<String, List<UserModuleProgress>>()
        val userList = _users.value
        if (userList.isEmpty()) { onResult(result); return }
        var remaining = userList.size
        userList.forEach { user ->
            getUserFullProgress(user.uid) { progress ->
                result[user.uid] = progress
                remaining--
                if (remaining <= 0) onResult(result)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN DASHBOARD SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val banners by viewModel.banners.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                }
                else -> Unit
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.onSignOutClick() }) {
                    Text("Logout", color = CardRed)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = BrandBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Admin Dashboard", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("DoseScribe Management", color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard("Users", "${users.size}", Icons.Filled.People, BrandBlue, Modifier.weight(1f))
                AdminStatCard("Lessons", "${allModules.size}", Icons.Filled.MenuBook, CardGreen, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard("Feedback", "${feedback.size}", Icons.Filled.Feedback, CardOrange, Modifier.weight(1f))
                AdminStatCard("Banners", "${banners.size}", Icons.Filled.ViewCarousel, CardPurple, Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            Text("Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))

            AdminNavCard("All Users & Progress", "View users, progress, MCQ results, certificates", Icons.Filled.People, BrandBlue) { navController.navigate("admin_users") }
            AdminNavCard("Banner Management", "Upload, enable/disable promotional banners", Icons.Filled.ViewCarousel, CardPurple) { navController.navigate("admin_banners") }
            AdminNavCard("Send Notifications", "Send to one, multiple, or all users", Icons.Filled.Notifications, CardOrange) { navController.navigate("admin_notifications") }
            AdminNavCard("Sent Notifications", "View all previously sent notifications", Icons.Filled.History, Color(0xFF455A64)) { navController.navigate("admin_sent_notifications") }
            AdminNavCard("Feedback & Suggestions", "View and reply to user feedback", Icons.Filled.Feedback, CardGreen) { navController.navigate("admin_feedback") }
            AdminNavCard("Analytics & Export", "Pre/Post analytics, MCQ, simulation, export", Icons.Filled.Analytics, BrandBlue) { navController.navigate("admin_analytics") }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun AdminNavCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN USERS SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminUsersScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()
    val userProgressMap = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(users) { users.forEach { user -> viewModel.getUserProgress(user.uid) { pd -> userProgressMap[user.uid] = pd.count { (_, v) -> ((v as? Number)?.toInt() ?: 0) >= 5 } } } }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SimpleAdminTopBar("All Users (${users.size})") { navController.popBackStack() }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(users) { user ->
                    Card(Modifier.fillMaxWidth().clickable { navController.navigate("admin_user_detail/${user.uid}") }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(CircleShape).background(BrandBlueLight), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = BrandBlue, modifier = Modifier.size(24.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.fullName.ifBlank { "${user.firstName} ${user.lastName}".trim().ifBlank { "No Name" } }, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                Text("UID: ${user.uid.take(12)}…", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                Spacer(Modifier.height(4.dp))
                                val cl = userProgressMap[user.uid] ?: 0
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(progress = { if (allModules.isNotEmpty()) cl / allModules.size.toFloat() else 0f }, modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)), color = if (cl == allModules.size) CardGreen else BrandBlue, trackColor = BrandBlueLight)
                                    Spacer(Modifier.width(8.dp))
                                    Text("$cl/${allModules.size}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
                        }
                    }
                }
                if (users.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No registered users yet", color = Color.Gray) } } }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN USER DETAIL SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminUserDetailScreen(navController: NavController, userId: String, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()
    val user = users.find { it.uid == userId }
    var progressList by remember { mutableStateOf<List<UserModuleProgress>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Progress", "Pre-Q", "Post-Q", "MCQ Results", "Simulation", "Certificates")
    LaunchedEffect(userId) { viewModel.getUserFullProgress(userId) { progressList = it } }
    val mcqs = remember { allModules.associate { it.id to getMcqsForModule(it.id) } }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SimpleAdminTopBar(user?.fullName?.ifBlank { user.email } ?: "User Detail") { navController.popBackStack() }

            Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = BrandBlue)) {
                Column(Modifier.padding(16.dp)) {
                    Text(user?.fullName?.ifBlank { "${user.firstName} ${user.lastName}".trim() } ?: "", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(4.dp))
                    Text(user?.email ?: "", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("User ID: ${user?.uid ?: ""}", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    val totalSteps = progressList.sumOf { it.completedSteps.size }
                    val completedModules = progressList.count { it.completedSteps.size == 5 }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$completedModules/${allModules.size} Modules", color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                        Text("$totalSteps/${allModules.size * 5} Steps", color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { totalSteps / (allModules.size * 5f) }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = Color.White, trackColor = Color.White.copy(0.3f))
                }
            }

            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = BrandBlue, edgePadding = 8.dp, divider = { HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(0.3f)) }) {
                tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }) }
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedTab) {
                    0 -> items(allModules) { module ->
                        val mp = progressList.find { it.moduleId == module.id }
                        val steps = mp?.completedSteps?.size ?: 0; val progress = steps / 5f
                        val stepLabels = listOf("Pre-Q", "Learn", "Sim", "Post-Q", "Results")
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(module.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = if (progress >= 1f) CardGreen else BrandBlue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = if (progress >= 1f) CardGreen else BrandBlue, trackColor = BrandBlueLight)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    stepLabels.forEachIndexed { idx, label ->
                                        val done = mp?.completedSteps?.contains(idx) == true
                                        Surface(shape = RoundedCornerShape(4.dp), color = if (done) CardGreen.copy(0.15f) else Color.Gray.copy(0.1f)) {
                                            Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (done) CardGreen else Color.Gray, fontWeight = if (done) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> items(allModules) { module -> AnswerSection("Pre-Q: ${module.title}", progressList.find { it.moduleId == module.id }?.preAnswers, mcqs[module.id]) }
                    2 -> items(allModules) { module -> AnswerSection("Post-Q: ${module.title}", progressList.find { it.moduleId == module.id }?.postAnswers, mcqs[module.id]) }
                    3 -> items(allModules) { module ->
                        val mp = progressList.find { it.moduleId == module.id }; val qs = mcqs[module.id] ?: emptyList()
                        val preAns = mp?.preAnswers ?: emptyList(); val postAns = mp?.postAnswers ?: emptyList()
                        val preC = preAns.zip(qs).count { (a, q) -> a == q.correctIndex }; val postC = postAns.zip(qs).count { (a, q) -> a == q.correctIndex }
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(module.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    ScoreCol("Pre-Test", if (preAns.isNotEmpty()) "$preC/${qs.size}" else "—", if (preAns.isNotEmpty() && qs.isNotEmpty()) "${preC * 100 / qs.size}%" else "", BrandBlue)
                                    ScoreCol("Post-Test", if (postAns.isNotEmpty()) "$postC/${qs.size}" else "—", if (postAns.isNotEmpty() && qs.isNotEmpty()) "${postC * 100 / qs.size}%" else "", CardGreen)
                                    val diff = postC - preC; val hasData = preAns.isNotEmpty() && postAns.isNotEmpty()
                                    ScoreCol("Change", if (hasData) "${if (diff > 0) "+" else ""}$diff" else "—", "", if (hasData && diff > 0) CardGreen else if (hasData && diff < 0) CardRed else Color.Gray)
                                }
                            }
                        }
                    }
                    4 -> items(allModules) { module ->
                        val done = progressList.find { it.moduleId == module.id }?.completedSteps?.contains(2) == true
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text(module.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis); Text("Simulation (Step 3)", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                                Surface(shape = RoundedCornerShape(6.dp), color = if (done) CardGreen.copy(0.12f) else CardOrange.copy(0.12f)) { Text(if (done) "✓ Completed" else "Not Done", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (done) CardGreen else CardOrange, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                    5 -> {
                        val completed = allModules.filter { m -> progressList.any { it.moduleId == m.id && it.completedSteps.size == 5 } }
                        if (completed.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No certificates earned yet", color = Color.Gray) } } }
                        items(completed) { module ->
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).clip(CircleShape).background(CardGreen.copy(0.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.EmojiEvents, null, tint = CardGreen, modifier = Modifier.size(20.dp)) }
                                    Spacer(Modifier.width(12.dp))
                                    Column { Text(module.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)); Text("Certificate Earned ✓", style = MaterialTheme.typography.labelSmall, color = CardGreen) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ScoreCol(label: String, value: String, pct: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color)
        if (pct.isNotEmpty()) Text(pct, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable private fun AnswerSection(title: String, answers: List<Int>?, questions: List<McqQuestion>?) {
    val qs = questions ?: emptyList(); val ans = answers ?: emptyList()
    if (ans.isEmpty()) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(12.dp)) { Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)); Text("Not attempted", style = MaterialTheme.typography.labelSmall, color = Color.Gray) } }
    } else {
        var expanded by remember { mutableStateOf(false) }
        val correct = ans.zip(qs).count { (a, q) -> a == q.correctIndex }
        Card(Modifier.fillMaxWidth().clickable { expanded = !expanded }, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f), maxLines = 2)
                    Surface(shape = RoundedCornerShape(6.dp), color = BrandBlue.copy(0.12f)) { Text("$correct/${qs.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(4.dp))
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    qs.forEachIndexed { idx, q ->
                        val ua = ans.getOrNull(idx); val ok = ua == q.correctIndex
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("Q${idx + 1}: ${q.question}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = Color.DarkGray)
                            Spacer(Modifier.height(2.dp))
                            Row {
                                Text("Answer: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(q.options.getOrElse(ua ?: -1) { "No answer" }, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = if (ok) CardGreen else CardRed)
                                Text(if (ok) " ✓" else " ✗", style = MaterialTheme.typography.labelSmall, color = if (ok) CardGreen else CardRed)
                            }
                            if (!ok) Text("Correct: ${q.options.getOrElse(q.correctIndex) { "" }}", style = MaterialTheme.typography.labelSmall, color = CardGreen)
                            if (idx < qs.lastIndex) HorizontalDivider(Modifier.padding(top = 4.dp), color = Color.LightGray.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN BANNERS SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminBannersScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val banners by viewModel.banners.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }; var newImageUrl by remember { mutableStateOf("") }; var newLinkType by remember { mutableStateOf("none") }; var newTargetId by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("Add Banner", fontWeight = FontWeight.Bold) }, text = {
            Column {
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newImageUrl, onValueChange = { newImageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                Text("Link Type:", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("none", "lesson", "url").forEach { type -> FilterChip(selected = newLinkType == type, onClick = { newLinkType = type }, label = { Text(type.replaceFirstChar { it.uppercase() }) }) } }
                if (newLinkType != "none") { Spacer(Modifier.height(8.dp)); OutlinedTextField(value = newTargetId, onValueChange = { newTargetId = it }, label = { Text(if (newLinkType == "lesson") "Module ID" else "URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
            }
        }, confirmButton = {
            TextButton(onClick = { viewModel.addBanner(Banner(title = newTitle, imageUrl = newImageUrl, linkType = newLinkType, targetId = newTargetId, order = banners.size)); newTitle = ""; newImageUrl = ""; newLinkType = "none"; newTargetId = ""; showAddDialog = false }, enabled = newTitle.isNotBlank() && newImageUrl.isNotBlank()) { Text("Add", color = BrandBlue) }
        }, dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } })
    }

    Scaffold(containerColor = SurfaceBg, floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = BrandBlue) { Icon(Icons.Filled.Add, null, tint = Color.White) } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SimpleAdminTopBar("Banner Management") { navController.popBackStack() }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(banners) { banner ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(BrandBlueLight), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Image, null, tint = BrandBlue) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) { Text(banner.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)); Text("Link: ${banner.linkType}", style = MaterialTheme.typography.bodySmall, color = Color.Gray); if (banner.targetId.isNotBlank()) Text("Target: ${banner.targetId}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = banner.isActive, onCheckedChange = { viewModel.toggleBannerActive(banner) }, colors = SwitchDefaults.colors(checkedTrackColor = CardGreen)); Spacer(Modifier.width(4.dp)); Text(if (banner.isActive) "Active" else "Disabled", style = MaterialTheme.typography.labelSmall, color = if (banner.isActive) CardGreen else Color.Gray) }
                                IconButton(onClick = { viewModel.deleteBanner(banner.id) }) { Icon(Icons.Filled.Delete, null, tint = CardRed) }
                            }
                        }
                    }
                }
                if (banners.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No banners. Tap + to add.", color = Color.Gray) } } }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN SEND NOTIFICATIONS SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminNotificationsScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()
    var title by remember { mutableStateOf("") }; var message by remember { mutableStateOf("") }; var lessonLink by remember { mutableStateOf("") }
    var sendToAll by remember { mutableStateOf(true) }; var selectedUserIds by remember { mutableStateOf(setOf<String>()) }; var sent by remember { mutableStateOf(false) }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SimpleAdminTopBar("Send Notification") { navController.popBackStack() }
            Column(Modifier.padding(16.dp)) {
                Text("Compose Notification", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it; sent = false }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = message, onValueChange = { message = it; sent = false }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = lessonLink, onValueChange = { lessonLink = it }, label = { Text("Lesson Link (optional module ID)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(12.dp))
                Text("Send To:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = sendToAll, onClick = { sendToAll = true }); Text("All Users", modifier = Modifier.clickable { sendToAll = true })
                    Spacer(Modifier.width(20.dp))
                    RadioButton(selected = !sendToAll, onClick = { sendToAll = false }); Text("Selected Users", modifier = Modifier.clickable { sendToAll = false })
                }
                if (!sendToAll) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(8.dp).heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                            users.forEach { user ->
                                Row(Modifier.fillMaxWidth().clickable { selectedUserIds = if (user.uid in selectedUserIds) selectedUserIds - user.uid else selectedUserIds + user.uid }.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = user.uid in selectedUserIds, onCheckedChange = { selectedUserIds = if (it) selectedUserIds + user.uid else selectedUserIds - user.uid })
                                    Spacer(Modifier.width(8.dp))
                                    Column { Text(user.fullName.ifBlank { user.email }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)); Text(user.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { val targets = if (sendToAll) users.map { it.uid } else selectedUserIds.toList(); if (targets.isNotEmpty() && title.isNotBlank() && message.isNotBlank()) { viewModel.sendNotification(title, message, targets, lessonLink.trim()); sent = true; title = ""; message = ""; lessonLink = "" } }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(12.dp), enabled = title.isNotBlank() && message.isNotBlank() && !sent) {
                    Icon(Icons.Filled.Send, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (sent) "✓ Notification Sent!" else "Send Notification", fontSize = 16.sp)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN SENT NOTIFICATIONS SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminSentNotificationsScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val notifications by viewModel.allNotifications.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SimpleAdminTopBar("Sent Notifications (${notifications.size})") { navController.popBackStack() }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                                Text(try { dateFormat.format(notif.sentAt.toDate()) } catch (_: Exception) { "" }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(4.dp)); Text(notif.message, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            if (notif.lessonId.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text("📎 Linked: ${notif.lessonId}", style = MaterialTheme.typography.labelSmall, color = BrandBlue) }
                        }
                    }
                }
                if (notifications.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No notifications sent yet", color = Color.Gray) } } }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN FEEDBACK SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminFeedbackScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val feedback by viewModel.feedback.collectAsState()
    var replyDialog by remember { mutableStateOf<Feedback?>(null) }; var replyText by remember { mutableStateOf("") }

    replyDialog?.let { fb ->
        AlertDialog(onDismissRequest = { replyDialog = null }, title = { Text("Reply to ${fb.userName.ifBlank { fb.userEmail }}", fontWeight = FontWeight.Bold) }, text = {
            Column { Text("\"${fb.message}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = replyText, onValueChange = { replyText = it }, label = { Text("Your reply") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
        }, confirmButton = { TextButton(onClick = { viewModel.replyToFeedback(fb, replyText); replyText = ""; replyDialog = null }, enabled = replyText.isNotBlank()) { Text("Send Reply", color = BrandBlue) } }, dismissButton = { TextButton(onClick = { replyDialog = null }) { Text("Cancel") } })
    }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SimpleAdminTopBar("Feedback (${feedback.size})") { navController.popBackStack() }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(feedback) { fb ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(fb.userName.ifBlank { fb.userEmail }, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                Surface(shape = RoundedCornerShape(6.dp), color = if (fb.status == "replied") CardGreen.copy(0.12f) else CardOrange.copy(0.12f)) { Text(fb.status.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (fb.status == "replied") CardGreen else CardOrange, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(Modifier.height(4.dp)); Text(fb.userEmail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp)); Text(fb.message, style = MaterialTheme.typography.bodyMedium)
                            if (fb.reply.isNotBlank()) { Spacer(Modifier.height(8.dp)); Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = BrandBlueLight) { Column(Modifier.padding(10.dp)) { Text("Admin Reply:", style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontWeight = FontWeight.Bold); Text(fb.reply, style = MaterialTheme.typography.bodySmall) } } }
                            Spacer(Modifier.height(8.dp)); TextButton(onClick = { replyDialog = fb; replyText = "" }) { Text(if (fb.reply.isBlank()) "Reply" else "Reply Again", color = BrandBlue, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
                if (feedback.isEmpty()) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No feedback yet", color = Color.Gray) } } }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  ADMIN ANALYTICS & EXPORT SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun AdminAnalyticsScreen(navController: NavController, viewModel: AdminDashboardViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState(); val context = LocalContext.current
    var allProgress by remember { mutableStateOf<Map<String, List<UserModuleProgress>>>(emptyMap()) }; var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(users) { if (users.isNotEmpty()) viewModel.getAllUsersProgress { allProgress = it; isLoading = false } else isLoading = false }
    val mcqs = remember { allModules.associate { it.id to getMcqsForModule(it.id) } }

    val totalPreAttempts = allProgress.values.flatten().count { it.preAnswers.isNotEmpty() }
    val totalPostAttempts = allProgress.values.flatten().count { it.postAnswers.isNotEmpty() }
    val totalSimCompleted = allProgress.values.flatten().count { it.completedSteps.contains(2) }
    val totalModulesCompleted = allProgress.values.flatten().count { it.completedSteps.size == 5 }

    val allPreScores = mutableListOf<Int>(); val allPostScores = mutableListOf<Int>()
    allProgress.values.flatten().forEach { mp -> val qs = mcqs[mp.moduleId] ?: emptyList(); if (qs.isNotEmpty()) { if (mp.preAnswers.isNotEmpty()) allPreScores.add(mp.preAnswers.zip(qs).count { (a, q) -> a == q.correctIndex } * 100 / qs.size); if (mp.postAnswers.isNotEmpty()) allPostScores.add(mp.postAnswers.zip(qs).count { (a, q) -> a == q.correctIndex } * 100 / qs.size) } }
    val avgPre = if (allPreScores.isNotEmpty()) allPreScores.average().toInt() else 0; val avgPost = if (allPostScores.isNotEmpty()) allPostScores.average().toInt() else 0

    val wrongCounts = mutableMapOf<String, Int>()
    allProgress.values.flatten().forEach { mp -> val qs = mcqs[mp.moduleId] ?: emptyList(); listOf(mp.preAnswers, mp.postAnswers).forEach { answers -> answers.forEachIndexed { qIdx, ans -> val q = qs.getOrNull(qIdx); if (q != null && ans != q.correctIndex) { val key = "${allModules.find { it.id == mp.moduleId }?.title?.take(20) ?: mp.moduleId}-Q${qIdx + 1}"; wrongCounts[key] = (wrongCounts[key] ?: 0) + 1 } } } }
    val topWrong = wrongCounts.entries.sortedByDescending { it.value }.take(5)

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SimpleAdminTopBar("Analytics & Export") { navController.popBackStack() }
            if (isLoading) { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandBlue) } }
            else Column(Modifier.padding(16.dp)) {
                Text("Cumulative Analytics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)); Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { AnalyticStatCard("Users", "${users.size}", BrandBlue, Modifier.weight(1f)); AnalyticStatCard("Modules Done", "$totalModulesCompleted", CardGreen, Modifier.weight(1f)) }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { AnalyticStatCard("Pre-Tests", "$totalPreAttempts", CardPurple, Modifier.weight(1f)); AnalyticStatCard("Post-Tests", "$totalPostAttempts", CardOrange, Modifier.weight(1f)) }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { AnalyticStatCard("Simulations", "$totalSimCompleted", BrandBlue, Modifier.weight(1f)); AnalyticStatCard("Avg Improve", "${avgPost - avgPre}%", if (avgPost > avgPre) CardGreen else CardRed, Modifier.weight(1f)) }

                Spacer(Modifier.height(20.dp)); Text("Pre vs Post Performance", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Avg Pre-Test", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("$avgPre%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = BrandBlue) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("→", style = MaterialTheme.typography.titleLarge, color = Color.Gray) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Avg Post-Test", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("$avgPost%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = CardGreen) }
                        }
                        Spacer(Modifier.height(8.dp)); val imp = avgPost - avgPre
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Surface(shape = RoundedCornerShape(8.dp), color = if (imp >= 0) CardGreen.copy(0.12f) else CardRed.copy(0.12f)) { Text("${if (imp >= 0) "↑" else "↓"} ${kotlin.math.abs(imp)}% ${if (imp >= 0) "Improvement" else "Decline"}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (imp >= 0) CardGreen else CardRed) }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp)); Text("Simulation Analytics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Total Attempts", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("$totalSimCompleted", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = BrandBlue) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { val r = if (users.isNotEmpty() && allModules.isNotEmpty()) (totalSimCompleted * 100) / (users.size * allModules.size) else 0; Text("Completion Rate", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("$r%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CardGreen) }
                    }
                }

                if (topWrong.isNotEmpty()) { Spacer(Modifier.height(20.dp)); Text("Most Wrong Questions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(8.dp))
                    topWrong.forEach { (key, count) -> Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(key, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(1f)); Surface(shape = RoundedCornerShape(4.dp), color = CardRed.copy(0.12f)) { Text("$count wrong", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = CardRed, fontWeight = FontWeight.Bold) } } } }
                }

                Spacer(Modifier.height(20.dp)); Text("Per Module Completion", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(8.dp))
                allModules.forEach { module -> val c = allProgress.values.count { list -> list.any { it.moduleId == module.id && it.completedSteps.size == 5 } }; Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(module.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis); Spacer(Modifier.width(8.dp)); Text("$c/${users.size}", style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontWeight = FontWeight.Bold) } } }

                Spacer(Modifier.height(24.dp))
                Button(onClick = { exportFullData(context, users, allProgress, mcqs) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = CardGreen), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Filled.Download, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Export All Data to CSV", fontSize = 16.sp) }
                Spacer(Modifier.height(8.dp)); Text("Exports: User ID, Name, Email, Lesson, Pre/Post scores & answers, Module completion, Simulation, Cumulative score.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

private fun exportFullData(context: Context, users: List<User>, allProgress: Map<String, List<UserModuleProgress>>, mcqs: Map<String, List<McqQuestion>>) {
    try {
        val sb = StringBuilder()
        sb.appendLine("User ID,Full Name,Email,Lesson,Pre-Test Score,Pre-Test Answers,Post-Test Score,Post-Test Answers,Steps Completed,Module Complete,Simulation Done,Cumulative Score %")
        users.forEach { user ->
            val progress = allProgress[user.uid] ?: emptyList()
            if (progress.isEmpty()) { sb.appendLine("${user.uid},${esc(user.fullName)},${user.email},No Progress,N/A,,N/A,,0/5,No,No,N/A") }
            else allModules.forEach { module ->
                val mp = progress.find { it.moduleId == module.id }; val qs = mcqs[module.id] ?: emptyList()
                val preC = if (mp != null && mp.preAnswers.isNotEmpty()) mp.preAnswers.zip(qs).count { (a, q) -> a == q.correctIndex } else 0
                val postC = if (mp != null && mp.postAnswers.isNotEmpty()) mp.postAnswers.zip(qs).count { (a, q) -> a == q.correctIndex } else 0
                val preS = if (qs.isNotEmpty() && mp?.preAnswers?.isNotEmpty() == true) "${preC * 100 / qs.size}%" else "N/A"
                val postS = if (qs.isNotEmpty() && mp?.postAnswers?.isNotEmpty() == true) "${postC * 100 / qs.size}%" else "N/A"
                val stepsCount = mp?.completedSteps?.size ?: 0
                val cumS = if (qs.isNotEmpty() && (mp?.preAnswers?.isNotEmpty() == true || mp?.postAnswers?.isNotEmpty() == true)) { val t = preC + postC; "${t * 100 / (qs.size * 2)}%" } else "N/A"
                sb.appendLine("${user.uid},${esc(user.fullName)},${user.email},${esc(module.title)},$preS,\"${mp?.preAnswers?.joinToString(";") ?: ""}\",$postS,\"${mp?.postAnswers?.joinToString(";") ?: ""}\",$stepsCount/5,${if (stepsCount == 5) "Yes" else "No"},${if (mp?.completedSteps?.contains(2) == true) "Yes" else "No"},$cumS")
            }
        }
        val file = java.io.File(context.cacheDir, "dosescribe_full_export.csv"); file.writeText(sb.toString())
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.startActivity(Intent.createChooser(intent, "Export DoseScribe Data"))
    } catch (e: Exception) { e.printStackTrace() }
}

private fun esc(s: String): String = s.replace(",", " ").replace("\"", "'")

@Composable
private fun AnalyticStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = color); Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
    }
}

@Composable
private fun SimpleAdminTopBar(title: String, onBackClick: () -> Unit) {
    Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.DarkGray) }
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
        }
    }
}
