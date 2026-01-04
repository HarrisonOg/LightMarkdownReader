package com.harrisonog.lightmarkdownreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.harrisonog.lightmarkdownreader.data.FileRepository
import com.harrisonog.lightmarkdownreader.data.RecentFilesRepository
import com.harrisonog.lightmarkdownreader.ui.screens.ReaderScreen
import com.harrisonog.lightmarkdownreader.ui.theme.LightMarkdownReaderTheme
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ReaderViewModel by viewModels()
    private lateinit var fileRepository: FileRepository
    private lateinit var recentFilesRepository: RecentFilesRepository
    private var currentFileUri: Uri? = null

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission to access the file
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Save the current file URI for sharing
            currentFileUri = it

            // Load the file
            viewModel.loadFile(it, fileRepository, recentFilesRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fileRepository = FileRepository(applicationContext)
        recentFilesRepository = RecentFilesRepository(applicationContext)

        setContent {
            LightMarkdownReaderTheme {
                ReaderScreen(
                    viewModel = viewModel,
                    onPickFile = {
                        openFilePicker()
                    },
                    onShare = {
                        shareCurrentFile()
                    },
                    onClose = {
                        viewModel.closeFile()
                    },
                    onRecentFileClick = { uri ->
                        openRecentFile(uri)
                    }
                )
            }
        }

        // Load recent files list
        viewModel.loadRecentFiles(recentFilesRepository)

        // Load last opened file if available
        loadLastOpenedFile()
    }

    private fun openFilePicker() {
        openDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
    }

    private fun openRecentFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Check if we still have permission to access the file
                contentResolver.openInputStream(uri)?.close()

                // Save the current file URI for sharing
                currentFileUri = uri

                // Load the file
                viewModel.loadFile(uri, fileRepository, recentFilesRepository)
            } catch (e: Exception) {
                // Permission lost or file no longer exists, remove from recent files
                viewModel.removeRecentFile(uri, recentFilesRepository)
            }
        }
    }

    private fun shareCurrentFile() {
        currentFileUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }

    private fun loadLastOpenedFile() {
        // Get the most recent file from the recent files repository
        val recentFiles = recentFilesRepository.getRecentFiles()

        if (recentFiles.isNotEmpty()) {
            val mostRecentFile = recentFiles.first()
            val uri = Uri.parse(mostRecentFile.uri)

            // Check if we still have permission to access the file
            lifecycleScope.launch {
                try {
                    contentResolver.openInputStream(uri)?.close()
                    currentFileUri = uri
                    viewModel.loadFile(uri, fileRepository, recentFilesRepository)
                } catch (e: Exception) {
                    // Permission lost or file no longer exists, it will be removed by ViewModel
                    viewModel.removeRecentFile(uri, recentFilesRepository)
                }
            }
        }
    }
}