package com.azhar.dosescribe.ui.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.data.model.Feedback
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.domain.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("")

    private val _submitted = MutableStateFlow(false)
    val submitted = _submitted.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val user = authRepository.currentUser
        _userEmail.value = user?.email ?: ""

        authRepository.getUserProfile().onEach { result ->
            result.onSuccess { profile ->
                _userEmail.value = profile.email.ifBlank { user?.email ?: "" }
                _userName.value = profile.fullName.ifBlank { "${profile.firstName} ${profile.lastName}".trim() }
            }
        }.launchIn(viewModelScope)
    }

    fun submitFeedback(message: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val feedback = Feedback(
            userId = uid,
            userName = _userName.value,
            userEmail = _userEmail.value,
            message = message
        )
        feedbackRepository.submitFeedback(feedback).onEach { result ->
            result.onSuccess { _submitted.value = true }
        }.launchIn(viewModelScope)
    }
}

