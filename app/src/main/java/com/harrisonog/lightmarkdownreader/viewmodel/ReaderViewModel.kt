package com.harrisonog.lightmarkdownreader.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.lightmarkdownreader.data.FileNotFoundCustomException
import com.harrisonog.lightmarkdownreader.data.FileNotOpenableException
import com.harrisonog.lightmarkdownreader.data.FilePermissionException
import com.harrisonog.lightmarkdownreader.data.FileRepository
import com.harrisonog.lightmarkdownreader.data.FileTooLargeException
import com.harrisonog.lightmarkdownreader.data.RecentFile
import com.harrisonog.lightmarkdownreader.data.RecentFilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Empty)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()

    fun loadFile(uri: Uri, fileRepository: FileRepository, recentFilesRepository: RecentFilesRepository) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading

            fileRepository.readMarkdownFile(uri).fold(
                onSuccess = { content ->
                    val fileName = fileRepository.getFileName(uri)
                    _uiState.value = ReaderUiState.Success(
                        content = content,
                        fileName = fileName
                    )

                    // Add to recent files after successful load
                    recentFilesRepository.addRecentFile(uri, fileName)
                    loadRecentFiles(recentFilesRepository)
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

    fun loadRecentFiles(recentFilesRepository: RecentFilesRepository) {
        viewModelScope.launch {
            _recentFiles.value = recentFilesRepository.getRecentFiles()
        }
    }

    fun removeRecentFile(uri: Uri, recentFilesRepository: RecentFilesRepository) {
        viewModelScope.launch {
            recentFilesRepository.removeRecentFile(uri)
            loadRecentFiles(recentFilesRepository)
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
