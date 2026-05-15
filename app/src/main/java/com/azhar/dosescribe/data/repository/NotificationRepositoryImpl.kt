package com.azhar.dosescribe.data.repository

import com.azhar.dosescribe.data.model.AppNotification
import com.azhar.dosescribe.domain.repository.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NotificationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getUserNotifications(userId: String): Flow<Result<List<AppNotification>>> = callbackFlow {
        firestore.collection("users").document(userId).collection("notifications")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(notifications))
            }
        awaitClose { }
    }

    override fun sendNotification(notification: AppNotification, targetUserIds: List<String>): Flow<Result<Unit>> = callbackFlow {
        val batch = firestore.batch()
        val notifWithTimestamp = notification.copy(sentAt = Timestamp.now())

        for (uid in targetUserIds) {
            val docRef = firestore.collection("users").document(uid).collection("notifications").document()
            batch.set(docRef, notifWithTimestamp.copy(id = docRef.id, targetUserId = uid))
        }

        // Also save to admin's notifications_sent collection for tracking
        val sentRef = firestore.collection("notifications_sent").document()
        batch.set(sentRef, mapOf(
            "id" to sentRef.id,
            "title" to notification.title,
            "message" to notification.message,
            "targetUserIds" to targetUserIds,
            "lessonId" to notification.lessonId,
            "sentAt" to Timestamp.now()
        ))

        batch.commit()
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun markAsRead(userId: String, notificationId: String): Flow<Result<Unit>> = callbackFlow {
        firestore.collection("users").document(userId).collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun getUnreadCount(userId: String): Flow<Result<Int>> = callbackFlow {
        firestore.collection("users").document(userId).collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                trySend(Result.success(snapshot?.size() ?: 0))
            }
        awaitClose { }
    }
}

