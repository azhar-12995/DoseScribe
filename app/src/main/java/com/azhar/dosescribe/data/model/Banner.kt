package com.azhar.dosescribe.data.model

import com.google.firebase.Timestamp

data class Banner(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "", // URL or base64
    val linkType: String = "none", // "none", "lesson", "url"
    val targetId: String = "", // moduleId for lesson, URL for url
    val isActive: Boolean = true,
    val order: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)

