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
import com.harrisonog.lightmarkdownreader.ui.screens.ReaderScreen
import com.harrisonog.lightmarkdownreader.ui.theme.LightMarkdownReaderTheme
import com.harrisonog.lightmarkdownreader.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ReaderViewModel by viewModels()
    private lateinit var fileRepository: FileRepository

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission to access the file
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Save the last opened file URI
            saveLastOpenedFile(it)

            // Load the file
            viewModel.loadFile(it, fileRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fileRepository = FileRepository(applicationContext)

        setContent {
            LightMarkdownReaderTheme {
                ReaderScreen(
                    viewModel = viewModel,
                    onPickFile = {
                        openFilePicker()
                    }
                )
            }
        }

        // Load last opened file if available
        loadLastOpenedFile()
    }

    private fun openFilePicker() {
        openDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
    }

    private fun saveLastOpenedFile(uri: Uri) {
        val prefs = getSharedPreferences("markdown_reader_prefs", MODE_PRIVATE)
        prefs.edit().putString("last_opened_file", uri.toString()).apply()
    }

    private fun loadLastOpenedFile() {
        val prefs = getSharedPreferences("markdown_reader_prefs", MODE_PRIVATE)
        val lastFileUri = prefs.getString("last_opened_file", null)

        lastFileUri?.let { uriString ->
            val uri = Uri.parse(uriString)
            // Check if we still have permission to access the file
            lifecycleScope.launch {
                try {
                    contentResolver.openInputStream(uri)?.close()
                    viewModel.loadFile(uri, fileRepository)
                } catch (e: Exception) {
                    // Permission lost or file no longer exists, clear the preference
                    prefs.edit().remove("last_opened_file").apply()
                }
            }
        }
    }
}