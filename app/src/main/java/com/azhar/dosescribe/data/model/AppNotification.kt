package com.azhar.dosescribe.data.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val targetUserId: String = "", // empty = all users
    val lessonId: String = "", // optional linked lesson
    val isRead: Boolean = false,
    val sentAt: Timestamp = Timestamp.now()
)

