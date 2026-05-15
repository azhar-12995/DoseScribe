package com.azhar.dosescribe.data.repository

import com.azhar.dosescribe.data.model.Banner
import com.azhar.dosescribe.domain.repository.BannerRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BannerRepositoryImpl(
    private val firestore: FirebaseFirestore
) : BannerRepository {

    override fun getBanners(): Flow<Result<List<Banner>>> = callbackFlow {
        firestore.collection("banners")
            .whereEqualTo("isActive", true)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val banners = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Banner::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(banners))
            }
        awaitClose { }
    }

    override fun addBanner(banner: Banner): Flow<Result<Unit>> = callbackFlow {
        val docRef = firestore.collection("banners").document()
        val bannerWithId = banner.copy(id = docRef.id)
        docRef.set(bannerWithId)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun updateBanner(banner: Banner): Flow<Result<Unit>> = callbackFlow {
        firestore.collection("banners").document(banner.id)
            .set(banner)
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun deleteBanner(bannerId: String): Flow<Result<Unit>> = callbackFlow {
        firestore.collection("banners").document(bannerId)
            .delete()
            .addOnSuccessListener { trySend(Result.success(Unit)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }
}

