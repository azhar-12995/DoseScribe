package com.azhar.dosescribe.ui.feature.menu

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _avatarId = MutableStateFlow(0)
    val avatarId = _avatarId.asStateFlow()

    private val _profileImageBase64 = MutableStateFlow("")
    val profileImageBase64 = _profileImageBase64.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = authRepository.currentUser
        _email.value = user?.email ?: ""

        // Load from Firestore
        authRepository.getUserProfile().onEach { result ->
            result.onSuccess { profile ->
                _firstName.value = profile.firstName.ifBlank {
                    profile.fullName.split(" ").getOrElse(0) { "" }
                }
                _lastName.value = profile.lastName.ifBlank {
                    profile.fullName.split(" ").getOrElse(1) { "" }
                }
                _email.value = profile.email.ifBlank { user?.email ?: "" }
                _avatarId.value = profile.avatarId
                _profileImageBase64.value = profile.profileImageBase64
            }
        }.launchIn(viewModelScope)
    }

    fun updateFirstName(name: String) { _firstName.value = name }
    fun updateLastName(name: String) { _lastName.value = name }
    fun updateAvatarId(id: Int) {
        _avatarId.value = id
        _profileImageBase64.value = "" // clear image when avatar selected
    }

    fun updateProfileImage(base64: String) {
        _profileImageBase64.value = base64
        _avatarId.value = -1 // -1 means using custom image
        // Save image to Firestore
        authRepository.updateProfileImage(base64).onEach { result ->
            result.onSuccess { _saveSuccess.value = true }
        }.launchIn(viewModelScope)
    }

    fun saveProfile() {
        authRepository.updateUserProfile(
            firstName = _firstName.value,
            lastName = _lastName.value,
            avatarId = _avatarId.value
        ).onEach { result ->
            result.onSuccess { _saveSuccess.value = true }
        }.launchIn(viewModelScope)
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentPassword = MutableStateFlow("")
    val currentPassword = _currentPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _passwordStrength = MutableStateFlow(0f)
    val passwordStrength = _passwordStrength.asStateFlow()

    private val _strengthLabel = MutableStateFlow("")
    val strengthLabel = _strengthLabel.asStateFlow()

    private val _strengthColor = MutableStateFlow(Color.Gray)
    val strengthColor = _strengthColor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun updateCurrentPassword(pwd: String) { _currentPassword.value = pwd }
    fun updateNewPassword(pwd: String) {
        _newPassword.value = pwd
        calculateStrength(pwd)
    }
    fun updateConfirmPassword(pwd: String) { _confirmPassword.value = pwd }
    fun clearMessage() { _message.value = null }

    private fun calculateStrength(password: String) {
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        val strength = score / 5f
        _passwordStrength.value = strength

        when {
            score <= 1 -> { _strengthLabel.value = "Weak"; _strengthColor.value = Color(0xFFC62828) }
            score <= 2 -> { _strengthLabel.value = "Fair"; _strengthColor.value = Color(0xFFFF8F00) }
            score <= 3 -> { _strengthLabel.value = "Good"; _strengthColor.value = Color(0xFFF9A825) }
            score <= 4 -> { _strengthLabel.value = "Strong"; _strengthColor.value = Color(0xFF2E7D32) }
            else -> { _strengthLabel.value = "Very Strong"; _strengthColor.value = Color(0xFF1B5E20) }
        }
    }

    fun changePassword() {
        _isLoading.value = true
        authRepository.changePassword(_currentPassword.value, _newPassword.value).onEach { result ->
            _isLoading.value = false
            result.onSuccess {
                _message.value = "Password updated successfully!"
                _currentPassword.value = ""
                _newPassword.value = ""
                _confirmPassword.value = ""
                _passwordStrength.value = 0f
            }
            result.onFailure {
                _message.value = it.message ?: "Failed to update password"
            }
        }.launchIn(viewModelScope)
    }

    fun sendResetEmail() {
        val email = authRepository.currentUser?.email ?: return
        authRepository.sendPasswordResetEmail(email).onEach { result ->
            result.onSuccess {
                _message.value = "Password reset link sent to $email"
            }
            result.onFailure {
                _message.value = it.message ?: "Failed to send reset email"
            }
        }.launchIn(viewModelScope)
    }
}

