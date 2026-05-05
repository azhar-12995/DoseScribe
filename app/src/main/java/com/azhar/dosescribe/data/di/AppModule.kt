package com.azhar.dosescribe.data.di

import android.content.Context
import com.azhar.dosescribe.data.preferences.PreferencesManager
import com.azhar.dosescribe.data.repository.AuthRepositoryImpl
import com.azhar.dosescribe.data.repository.BannerRepositoryImpl
import com.azhar.dosescribe.data.repository.FeedbackRepositoryImpl
import com.azhar.dosescribe.data.repository.LessonsRepositoryImpl
import com.azhar.dosescribe.data.repository.NotificationRepositoryImpl
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.domain.repository.BannerRepository
import com.azhar.dosescribe.domain.repository.FeedbackRepository
import com.azhar.dosescribe.domain.repository.LessonsRepository
import com.azhar.dosescribe.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideLessonsRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): LessonsRepository {
        return LessonsRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideBannerRepository(
        firestore: FirebaseFirestore
    ): BannerRepository {
        return BannerRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFeedbackRepository(
        firestore: FirebaseFirestore
    ): FeedbackRepository {
        return FeedbackRepositoryImpl(firestore)
    }
}
