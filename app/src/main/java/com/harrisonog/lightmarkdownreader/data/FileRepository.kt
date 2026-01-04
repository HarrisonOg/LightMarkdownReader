package com.harrisonog.lightmarkdownreader.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

class FileRepository(private val context: Context) {

    fun readMarkdownFile(uri: Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                Result.success(stream.bufferedReader().use { it.readText() })
            } ?: Result.failure(Exception("Could not open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFileName(uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "Unknown"
    }
}
