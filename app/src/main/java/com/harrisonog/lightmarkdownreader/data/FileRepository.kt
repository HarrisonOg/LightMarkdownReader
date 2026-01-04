package com.harrisonog.lightmarkdownreader.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.IOException

class FileRepository(private val context: Context) {

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
    }

    fun readMarkdownFile(uri: Uri): Result<String> {
        return try {
            // Check file size first
            val fileSize = getFileSize(uri)
            if (fileSize > MAX_FILE_SIZE_BYTES) {
                return Result.failure(FileTooLargeException("File size exceeds 10MB limit"))
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                Result.success(stream.bufferedReader().use { it.readText() })
            } ?: Result.failure(FileNotOpenableException("Could not open file"))
        } catch (e: SecurityException) {
            Result.failure(FilePermissionException("Permission denied to read this file", e))
        } catch (e: FileNotFoundException) {
            Result.failure(FileNotFoundCustomException("File not found or no longer accessible", e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFileName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

// Custom exceptions for better error messages
class FileNotFoundCustomException(message: String, cause: Throwable? = null) : Exception(message, cause)
class FilePermissionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class FileTooLargeException(message: String) : Exception(message)
class FileNotOpenableException(message: String) : Exception(message)
