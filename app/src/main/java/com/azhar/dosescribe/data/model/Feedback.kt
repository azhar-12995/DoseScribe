package com.azhar.dosescribe.data.model

import com.google.firebase.Timestamp

data class Feedback(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val message: String = "",
    val reply: String = "",
    val status: String = "pending", // "pending", "replied"
    val createdAt: Timestamp = Timestamp.now()
)

