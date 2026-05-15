package com.azhar.dosescribe.ui.feature.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhar.dosescribe.data.model.Lesson
import com.azhar.dosescribe.domain.repository.LessonsRepository
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

data class LessonDetailState(
    val lesson: Lesson? = null,
    val isCompleted: Boolean = false
)

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val lessonsRepository: LessonsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LessonDetailState>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    init {
        getLesson()
        isLessonCompleted()
    }

    private fun getLesson() {
        lessonsRepository.getLessonById(lessonId).onEach { result ->
            result.onSuccess {
                _uiState.value = UiState.Success((_uiState.value as? UiState.Success)?.data?.copy(lesson = it) ?: LessonDetailState(lesson = it))
            }
            result.onFailure {
                _uiState.value = UiState.Error(it.message ?: "An unknown error occurred")
            }
        }.launchIn(viewModelScope)
    }

    private fun isLessonCompleted() {
        lessonsRepository.isLessonCompleted(lessonId).onEach { result ->
            result.onSuccess {
                _uiState.value = UiState.Success((_uiState.value as? UiState.Success)?.data?.copy(isCompleted = it) ?: LessonDetailState(isCompleted = it))
            }
            result.onFailure {
                // Don't show error, just assume not completed
            }
        }.launchIn(viewModelScope)
    }

    fun onMarkAsCompletedClick() {
        viewModelScope.launch {
            lessonsRepository.markLessonAsCompleted(lessonId).onEach { result ->
                result.onSuccess {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Lesson marked as completed"))
                }
                result.onFailure {
                    _uiEvent.emit(UiEvent.ShowSnackbar(it.message ?: "An unknown error occurred"))
                }
            }.launchIn(viewModelScope)
        }
    }
}
