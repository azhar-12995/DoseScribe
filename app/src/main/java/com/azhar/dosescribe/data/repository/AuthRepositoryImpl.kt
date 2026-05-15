package com.azhar.dosescribe.data.repository

import com.azhar.dosescribe.data.model.User
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun signIn(email: String, password: String): Flow<Result<Unit>> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    val errorMsg = when (task.exception) {
                        is FirebaseAuthInvalidUserException ->
                            "No account found with this email. Please sign up first."
                        is FirebaseAuthInvalidCredentialsException ->
                            "Incorrect password. Please try again."
                        else -> task.exception?.message ?: "Sign in failed"
                    }
                    trySend(Result.failure(Exception(errorMsg)))
                }
            }
        awaitClose { }
    }

    override fun signUp(fullName: String, email: String, password: String): Flow<Result<Unit>> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            val parts = fullName.trim().split(" ", limit = 2)
                            val firstName = parts.getOrElse(0) { "" }
                            val lastName = parts.getOrElse(1) { "" }

                            val userProfile = User(
                                uid = user.uid,
                                fullName = fullName,
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                role = "customer"
                            )
                            firestore.collection("users").document(user.uid)
                                .set(userProfile)
                                .addOnSuccessListener { trySend(Result.success(Unit)) }
                                .addOnFailureListener { e -> trySend(Result.failure(e)) }
                        }
                    } else {
                        trySend(Result.failure(Exception("User not created")))
                    }
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Sign up failed")))
                }
            }
        awaitClose { }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = callbackFlow {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    trySend(Result.failure(task.exception ?: Exception("Failed to send password reset email")))
                }
            }
        awaitClose { }
    }

    override fun getUserProfile(): Flow<Result<User>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("Not logged in")))
            close()
            return@callbackFlow
        }
        firestore.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    val user = doc.toObject(User::class.java) ?: User(
                        uid = uid,
                        fullName = auth.currentUser?.displayName ?: "",
                        email = auth.currentUser?.email ?: ""
                    )
                    trySend(Result.success(user))
                }
            }
        awaitClose { }
    }

    override fun getUserRole(): Flow<Result<String>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("Not logged in")))
            close()
            return@callbackFlow
        }
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "customer"
                trySend(Result.success(role))
            }
            .addOnFailureListener { e -> trySend(Result.failure(e)) }
        awaitClose { }
    }

    override fun updateUserProfile(firstName: String, lastName: String, avatarId: Int): Flow<Result<Unit>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("Not logged in")))
            close()
            return@callbackFlow
        }
        val fullName = "$firstName $lastName".trim()
        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "fullName" to fullName,
            "avatarId" to avatarId
        )
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { e -> trySend(Result.failure(e)) }
        awaitClose { }
    }

    override fun updateProfileImage(base64Image: String): Flow<Result<Unit>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("Not logged in")))
            close()
            return@callbackFlow
        }
        firestore.collection("users").document(uid)
            .update("profileImageBase64", base64Image)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { e -> trySend(Result.failure(e)) }
        awaitClose { }
    }

    override fun changePassword(currentPassword: String, newPassword: String): Flow<Result<Unit>> = callbackFlow {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            trySend(Result.failure(Exception("Not logged in")))
            close()
            return@callbackFlow
        }
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { trySend(Result.success(Unit)) }
                    .addOnFailureListener { e -> trySend(Result.failure(e)) }
            }
            .addOnFailureListener { trySend(Result.failure(Exception("Current password is incorrect"))) }
        awaitClose { }
    }

    override fun getAllUsers(): Flow<Result<List<User>>> = callbackFlow {
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                trySend(Result.success(users))
            }
        awaitClose { }
    }
}