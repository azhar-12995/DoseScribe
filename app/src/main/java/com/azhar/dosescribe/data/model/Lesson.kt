package com.azhar.dosescribe.data.model

import com.google.firebase.Timestamp

data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)