package com.harrisonog.lightmarkdownreader.data

import android.net.Uri

data class MarkdownFile(
    val uri: Uri,
    val name: String,
    val content: String
)
