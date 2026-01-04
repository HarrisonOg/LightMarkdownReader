package com.harrisonog.lightmarkdownreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.harrisonog.lightmarkdownreader.ui.screens.ReaderScreen
import com.harrisonog.lightmarkdownreader.ui.theme.LightMarkdownReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LightMarkdownReaderTheme {
                ReaderScreen(
                    onPickFile = {
                        // File picker will be implemented in next step
                    }
                )
            }
        }
    }
}