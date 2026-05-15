package com.azhar.dosescribe.ui.feature.lessons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.util.UiEvent
import com.azhar.dosescribe.ui.util.UiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    navController: NavController,
    lessonId: String,
    viewModel: LessonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is UiState.Success -> Text(text = state.data.lesson?.title ?: "")
                        else -> Text(text = "")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        state.data.lesson?.let { lesson ->
                            // TODO: Add Image
                            Text(text = lesson.content)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onMarkAsCompletedClick() },
                                enabled = !state.data.isCompleted
                            ) {
                                Text(if (state.data.isCompleted) "Completed" else "Mark as Completed")
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(text = state.message)
                }
            }
        }
    }
}
