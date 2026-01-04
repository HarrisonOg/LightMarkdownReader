package com.harrisonog.lightmarkdownreader.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.lightmarkdownreader.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Empty)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadFile(uri: Uri, repository: FileRepository) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading

            repository.readMarkdownFile(uri).fold(
                onSuccess = { content ->
                    _uiState.value = ReaderUiState.Success(
                        content = content,
                        fileName = repository.getFileName(uri)
                    )
                },
                onFailure = { error ->
                    _uiState.value = ReaderUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class ReaderUiState {
    object Empty : ReaderUiState()
    object Loading : ReaderUiState()
    data class Success(val content: String, val fileName: String) : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
}
