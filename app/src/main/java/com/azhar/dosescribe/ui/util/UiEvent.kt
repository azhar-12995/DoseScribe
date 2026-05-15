package com.azhar.dosescribe.ui.util

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
}
