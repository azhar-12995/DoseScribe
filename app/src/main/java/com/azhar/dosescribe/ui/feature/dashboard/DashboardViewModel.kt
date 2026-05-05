package com.azhar.dosescribe.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.data.model.Banner
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.domain.repository.BannerRepository
import com.azhar.dosescribe.domain.repository.NotificationRepository
import com.azhar.dosescribe.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bannerRepository: BannerRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _avatarId = MutableStateFlow(0)
    val avatarId = _avatarId.asStateFlow()

    private val _profileImageBase64 = MutableStateFlow("")
    val profileImageBase64 = _profileImageBase64.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners = _banners.asStateFlow()

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications = _unreadNotifications.asStateFlow()

    init {
        loadUserProfile()
        loadBanners()
        loadUnreadCount()
    }

    fun loadUserProfile() {
        // First set displayName as fallback
        val user = authRepository.currentUser
        _userName.value = user?.displayName ?: "User"

        // Then load full profile from Firestore
        authRepository.getUserProfile().onEach { result ->
            result.onSuccess { profile ->
                val name = when {
                    profile.firstName.isNotBlank() -> profile.firstName
                    profile.fullName.isNotBlank() -> profile.fullName.split(" ").first()
                    else -> user?.displayName ?: "User"
                }
                _userName.value = name
                _avatarId.value = profile.avatarId
                _profileImageBase64.value = profile.profileImageBase64
            }
        }.launchIn(viewModelScope)
    }

    private fun loadBanners() {
        bannerRepository.getBanners().onEach { result ->
            result.onSuccess { _banners.value = it }
        }.launchIn(viewModelScope)
    }

    private fun loadUnreadCount() {
        val uid = authRepository.currentUser?.uid ?: return
        notificationRepository.getUnreadCount(uid).onEach { result ->
            result.onSuccess { _unreadNotifications.value = it }
        }.launchIn(viewModelScope)
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiEvent.emit(UiEvent.Navigate("signin"))
        }
    }
}
