package com.azhar.dosescribe.domain.repository

import com.azhar.dosescribe.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun signIn(email: String, password: String): Flow<Result<Unit>>
    fun signUp(fullName: String, email: String, password: String): Flow<Result<Unit>>
    fun signOut()
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun getUserProfile(): Flow<Result<User>>
    fun getUserRole(): Flow<Result<String>>
    fun updateUserProfile(firstName: String, lastName: String, avatarId: Int): Flow<Result<Unit>>
    fun updateProfileImage(base64Image: String): Flow<Result<Unit>>
    fun changePassword(currentPassword: String, newPassword: String): Flow<Result<Unit>>
    fun getAllUsers(): Flow<Result<List<User>>>
}