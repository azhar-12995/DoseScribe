package com.azhar.dosescribe.ui.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(1500)
            val user = authRepository.currentUser
            if (user != null) {
                // Check if admin credentials (admin@a.com)
                val email = (user.email ?: "").lowercase()
                if (email == "admin@a.com") {
                    _uiEvent.emit(UiEvent.Navigate("admin_dashboard"))
                } else {
                    _uiEvent.emit(UiEvent.Navigate("dashboard"))
                }
            } else {
                _uiEvent.emit(UiEvent.Navigate("signin"))
            }
        }
    }
}
