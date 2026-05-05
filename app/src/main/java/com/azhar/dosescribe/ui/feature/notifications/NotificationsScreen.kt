package com.azhar.dosescribe.ui.feature.notifications

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.azhar.dosescribe.data.model.AppNotification
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val uid = authRepository.currentUser?.uid ?: return
        notificationRepository.getUserNotifications(uid).onEach { result ->
            result.onSuccess { _notifications.value = it }
        }.launchIn(viewModelScope)
    }

    fun markAsRead(notificationId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        notificationRepository.markAsRead(uid, notificationId).onEach {}.launchIn(viewModelScope)
    }
}

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Back", tint = Color.DarkGray) }
                    Text("Notifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                }
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.markAsRead(notification.id)
                            if (notification.lessonId.isNotBlank()) {
                                navController.navigate("lesson_steps/${notification.lessonId}")
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (notification.isRead) Color.White else BrandBlueLight),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(BrandBlue.copy(0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (notification.lessonId.isNotBlank()) Icons.Filled.MenuBook else Icons.Filled.Notifications,
                                    null, tint = BrandBlue, modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(notification.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                Spacer(Modifier.height(4.dp))
                                Text(notification.message, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    try { dateFormat.format(notification.sentAt.toDate()) } catch (e: Exception) { "" },
                                    style = MaterialTheme.typography.labelSmall, color = Color.Gray
                                )
                                if (notification.lessonId.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tap to open lesson →", style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (!notification.isRead) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(BrandBlue))
                            }
                        }
                    }
                }
                if (notifications.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.NotificationsNone, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No notifications yet", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

