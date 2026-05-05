package com.azhar.dosescribe.ui.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.data.model.Lesson
import com.azhar.dosescribe.domain.repository.LessonsRepository
import com.azhar.dosescribe.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val lessonsRepository: LessonsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Lesson>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getLessons()
    }

    private fun getLessons() {
        lessonsRepository.getLessons().onEach { result ->
            result.onSuccess {
                _uiState.value = UiState.Success(it)
            }
            result.onFailure {
                _uiState.value = UiState.Error(it.message ?: "An unknown error occurred")
            }
        }.launchIn(viewModelScope)
    }
}
