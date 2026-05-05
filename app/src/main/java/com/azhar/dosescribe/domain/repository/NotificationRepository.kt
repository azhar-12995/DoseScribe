package com.azhar.dosescribe.domain.repository

import com.azhar.dosescribe.data.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getUserNotifications(userId: String): Flow<Result<List<AppNotification>>>
    fun sendNotification(notification: AppNotification, targetUserIds: List<String>): Flow<Result<Unit>>
    fun markAsRead(userId: String, notificationId: String): Flow<Result<Unit>>
    fun getUnreadCount(userId: String): Flow<Result<Int>>
}

