package com.harrisonog.lightmarkdownreader.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harrisonog.lightmarkdownreader.R
import com.harrisonog.lightmarkdownreader.data.RecentFile
import com.harrisonog.lightmarkdownreader.ui.components.MarkdownContent
import com.harrisonog.lightmarkdownreader.ui.components.ReaderTopBar
import com.harrisonog.lightmarkdownreader.util.TimeFormatter
import com.harrisonog.lightmarkdownreader.viewmodel.ErrorType
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderUiState
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderViewModel

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel = viewModel(),
    onPickFile: () -> Unit,
    onShare: () -> Unit = {},
    onClose: () -> Unit = {},
    onRecentFileClick: (Uri) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = (uiState as? ReaderUiState.Success)?.fileName ?: stringResource(R.string.markdown_reader),
                onOpenFile = onPickFile,
                onShare = if (uiState is ReaderUiState.Success) {
                    onShare
                } else null,
                onClose = if (uiState is ReaderUiState.Success) {
                    onClose
                } else null
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ReaderUiState.Empty -> EmptyState(
                recentFiles = recentFiles,
                onPickFile = onPickFile,
                onRecentFileClick = onRecentFileClick,
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
                errorType = state.errorType,
                message = state.message,
                onPickFile = onPickFile,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EmptyState(
    recentFiles: List<RecentFile>,
    onPickFile: () -> Unit,
    onRecentFileClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recent files section
        AnimatedVisibility(
            visible = recentFiles.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.recently_read),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = recentFiles,
                        key = { it.uri }
                    ) { recentFile ->
                        RecentFileItem(
                            recentFile = recentFile,
                            onClick = { onRecentFileClick(Uri.parse(recentFile.uri)) },
                            context = context,
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(),
                                fadeOutSpec = spring(),
                                placementSpec = spring()
                            )
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = recentFiles.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedVisibility(
            visible = recentFiles.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            // Show centered message when no recent files
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_file_opened),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        // Open file button at bottom
        Button(
            onClick = onPickFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.open_markdown_file))
        }
    }
}

@Composable
fun RecentFileItem(
    recentFile: RecentFile,
    onClick: () -> Unit,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recentFile.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = TimeFormatter.formatRelativeTime(context, recentFile.lastOpenedTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    errorType: ErrorType,
    message: String,
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMessage = when (errorType) {
        ErrorType.FILE_NOT_FOUND -> stringResource(R.string.error_file_not_found)
        ErrorType.PERMISSION_DENIED -> stringResource(R.string.error_permission_denied)
        ErrorType.FILE_TOO_LARGE -> stringResource(R.string.error_file_too_large)
        ErrorType.COULD_NOT_OPEN -> stringResource(R.string.error_could_not_open)
        ErrorType.UNKNOWN -> stringResource(R.string.error_unknown)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = errorMessage,
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
