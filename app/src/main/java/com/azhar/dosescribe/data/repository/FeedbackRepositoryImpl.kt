package com.azhar.dosescribe.data.repository

import com.azhar.dosescribe.data.model.Feedback
import com.azhar.dosescribe.domain.repository.FeedbackRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FeedbackRepository {

    override fun submitFeedback(feedback: Feedback): Flow<Result<Unit>> = callbackFlow {
        val docRef = firestore.collection("feedback").document()
        val feedbackWithId = feedback.copy(id = docRef.id, createdAt = Timestamp.now())
        docRef.set(feedbackWithId)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun getAllFeedback(): Flow<Result<List<Feedback>>> = callbackFlow {
        firestore.collection("feedback")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val feedbackList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Feedback::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(feedbackList))
            }
        awaitClose { }
    }

    override fun replyToFeedback(feedbackId: String, reply: String): Flow<Result<Unit>> = callbackFlow {
        firestore.collection("feedback").document(feedbackId)
            .update(mapOf("reply" to reply, "status" to "replied"))
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }
}

