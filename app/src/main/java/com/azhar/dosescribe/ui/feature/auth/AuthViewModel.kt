package com.azhar.dosescribe.ui.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.domain.repository.AuthRepository
import com.azhar.dosescribe.ui.util.UiEvent
import com.azhar.dosescribe.ui.util.UiState
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
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()
    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()
    private val _gender = MutableStateFlow("")
    val gender = _gender.asStateFlow()
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName = _fullName.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    // Validation error states
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _firstNameError = MutableStateFlow<String?>(null)
    val firstNameError = _firstNameError.asStateFlow()

    private val _lastNameError = MutableStateFlow<String?>(null)
    val lastNameError = _lastNameError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
        _emailError.value = null
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _passwordError.value = null
    }

    fun onFullNameChange(fullName: String) {
        _fullName.value = fullName
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        _confirmPasswordError.value = null
    }

    private fun validateSignIn(): Boolean {
        var valid = true
        if (_email.value.isBlank()) {
            _emailError.value = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Invalid email format"
            valid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Password is required"
            valid = false
        }
        return valid
    }

    private fun validateSignUp(): Boolean {
        var valid = true
        if (_firstName.value.isBlank()) {
            _firstNameError.value = "First name required"
            valid = false
        }
        if (_lastName.value.isBlank()) {
            _lastNameError.value = "Last name required"
            valid = false
        }
        if (_email.value.isBlank()) {
            _emailError.value = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Invalid email format"
            valid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Password is required"
            valid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
            valid = false
        }
        if (_confirmPassword.value.isBlank()) {
            _confirmPasswordError.value = "Please confirm password"
            valid = false
        } else if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value = "Passwords do not match"
            valid = false
        }
        return valid
    }

    fun onSignInClick() {
        if (!validateSignIn()) return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            authRepository.signIn(_email.value, _password.value).onEach { result ->
                result.onSuccess {
                    _uiState.value = UiState.Success(Unit)
                    // Check if admin credentials (admin@a.com)
                    val loginEmail = _email.value.trim().lowercase()
                    if (loginEmail == "admin@a.com") {
                        _uiEvent.emit(UiEvent.Navigate("admin_dashboard"))
                    } else {
                        _uiEvent.emit(UiEvent.Navigate("dashboard"))
                    }
                }
                result.onFailure {
                    _uiState.value = UiState.Error(it.message ?: "An unknown error occurred")
                    _uiEvent.emit(UiEvent.ShowSnackbar(it.message ?: "An unknown error occurred"))
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onSignUpClick() {
        if (!validateSignUp()) return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val fullName = "${_firstName.value} ${_lastName.value}".trim()
            authRepository.signUp(fullName, _email.value, _password.value).onEach { result ->
                result.onSuccess {
                    _uiState.value = UiState.Success(Unit)
                    _uiEvent.emit(UiEvent.Navigate("dashboard"))
                }
                result.onFailure {
                    _uiState.value = UiState.Error(it.message ?: "An unknown error occurred")
                    _uiEvent.emit(UiEvent.ShowSnackbar(it.message ?: "An unknown error occurred"))
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onForgotPasswordClick() {
        viewModelScope.launch {
            if (_email.value.isBlank()) {
                _emailError.value = "Enter your email first"
                _uiEvent.emit(UiEvent.ShowSnackbar("Please enter your email address"))
                return@launch
            }
            _uiState.value = UiState.Loading
            authRepository.sendPasswordResetEmail(_email.value).onEach { result ->
                result.onSuccess {
                    _uiState.value = UiState.Success(Unit)
                    _uiEvent.emit(UiEvent.ShowSnackbar("Password reset link sent to your email"))
                }
                result.onFailure {
                    _uiState.value = UiState.Error(it.message ?: "An unknown error occurred")
                    _uiEvent.emit(UiEvent.ShowSnackbar(it.message ?: "An unknown error occurred"))
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onFirstNameChange(newValue: String) {
        _firstName.value = newValue
        _firstNameError.value = null
    }

    fun onLastNameChange(newValue: String) {
        _lastName.value = newValue
        _lastNameError.value = null
    }

    fun onGenderChange(newValue: String) {
        _gender.value = newValue
    }

    fun onGoogleSignInClick() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowSnackbar("Google Sign-In clicked"))
        }
    }
}
