package com.harrisonog.lightmarkdownreader.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.lightmarkdownreader.data.FileNotFoundCustomException
import com.harrisonog.lightmarkdownreader.data.FileNotOpenableException
import com.harrisonog.lightmarkdownreader.data.FilePermissionException
import com.harrisonog.lightmarkdownreader.data.FileRepository
import com.harrisonog.lightmarkdownreader.data.FileTooLargeException
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
                    _uiState.value = ReaderUiState.Error(
                        errorType = when (error) {
                            is FileNotFoundCustomException -> ErrorType.FILE_NOT_FOUND
                            is FilePermissionException -> ErrorType.PERMISSION_DENIED
                            is FileTooLargeException -> ErrorType.FILE_TOO_LARGE
                            is FileNotOpenableException -> ErrorType.COULD_NOT_OPEN
                            else -> ErrorType.UNKNOWN
                        },
                        message = error.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun closeFile() {
        _uiState.value = ReaderUiState.Empty
    }
}

sealed class ReaderUiState {
    object Empty : ReaderUiState()
    object Loading : ReaderUiState()
    data class Success(val content: String, val fileName: String) : ReaderUiState()
    data class Error(val errorType: ErrorType, val message: String) : ReaderUiState()
}

enum class ErrorType {
    FILE_NOT_FOUND,
    PERMISSION_DENIED,
    FILE_TOO_LARGE,
    COULD_NOT_OPEN,
    UNKNOWN
}
