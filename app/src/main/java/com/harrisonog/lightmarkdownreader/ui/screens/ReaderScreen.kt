package com.harrisonog.lightmarkdownreader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harrisonog.lightmarkdownreader.R
import com.harrisonog.lightmarkdownreader.ui.components.MarkdownContent
import com.harrisonog.lightmarkdownreader.ui.components.ReaderTopBar
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderUiState
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderViewModel

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel = viewModel(),
    onPickFile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = (uiState as? ReaderUiState.Success)?.fileName ?: stringResource(R.string.markdown_reader),
                onOpenFile = onPickFile,
                onShare = if (uiState is ReaderUiState.Success) {
                    { /* Share logic will be implemented later */ }
                } else null
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ReaderUiState.Empty -> EmptyState(
                onPickFile = onPickFile,
                modifier = Modifier.padding(padding)
            )
            is ReaderUiState.Loading -> LoadingState(
                modifier = Modifier.padding(padding)
            )
            is ReaderUiState.Success -> MarkdownContent(
                markdown = state.content,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            )
            is ReaderUiState.Error -> ErrorState(
                message = state.message,
                onPickFile = onPickFile,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EmptyState(
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_file_opened),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onPickFile) {
                Text(stringResource(R.string.open_markdown_file))
            }
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(
    message: String,
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_format, message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onPickFile) {
                Text(stringResource(R.string.try_another_file))
            }
        }
    }
}
