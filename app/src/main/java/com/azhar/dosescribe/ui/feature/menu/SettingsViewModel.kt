package com.azhar.dosescribe.ui.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _darkMode = MutableStateFlow(false)
    val darkMode = _darkMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _language = MutableStateFlow("English")
    val language = _language.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.darkModeFlow.collect { _darkMode.value = it }
        }
        viewModelScope.launch {
            preferencesManager.notificationsEnabledFlow.collect { _notificationsEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.languageFlow.collect { _language.value = it }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        viewModelScope.launch { preferencesManager.setDarkMode(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch { preferencesManager.setNotificationsEnabled(enabled) }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch { preferencesManager.setLanguage(lang) }
    }
}

