package com.azhar.dosescribe.data.repository

import com.azhar.dosescribe.data.model.Lesson
import com.azhar.dosescribe.domain.repository.LessonsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LessonsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : LessonsRepository {

    override fun getLessons(): Flow<Result<List<Lesson>>> = callbackFlow {
        val listener = firestore.collection("lessons")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lessons = snapshot.toObjects<Lesson>()
                    trySend(Result.success(lessons))
                } else {
                    trySend(Result.failure(Exception("No lessons found")))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getLessonById(lessonId: String): Flow<Result<Lesson>> = callbackFlow {
        val listener = firestore.collection("lessons").document(lessonId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val lesson = snapshot.toObject<Lesson>()
                    if (lesson != null) {
                        trySend(Result.success(lesson.copy(id = snapshot.id)))
                    } else {
                        trySend(Result.failure(Exception("Lesson not found")))
                    }
                } else {
                    trySend(Result.failure(Exception("Lesson not found")))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun markLessonAsCompleted(lessonId: String): Flow<Result<Unit>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.failure(Exception("User not logged in")))
            return@callbackFlow
        }

        val completedLesson = hashMapOf(
            "lessonId" to lessonId,
            "completedAt" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userId)
            .collection("completedLessons").document(lessonId)
            .set(completedLesson)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { e -> trySend(Result.failure(e)) }

        awaitClose { }
    }

    override fun isLessonCompleted(lessonId: String): Flow<Result<Boolean>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.failure(Exception("User not logged in")))
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .collection("completedLessons").document(lessonId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                trySend(Result.success(snapshot != null && snapshot.exists()))
            }
        awaitClose { listener.remove() }
    }
}