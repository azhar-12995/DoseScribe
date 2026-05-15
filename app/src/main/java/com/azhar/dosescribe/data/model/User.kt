package com.azhar.dosescribe.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val avatarId: Int = 0, // 0 = default, 1-6 = pharmacist avatars
    val profileImageBase64: String = "", // base64 encoded profile image
    val role: String = "customer", // "customer" or "admin"
    val createdAt: Timestamp = Timestamp.now()
)