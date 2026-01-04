package com.harrisonog.lightmarkdownreader.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentFilesRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "markdown_reader_prefs"
        private const val KEY_RECENT_FILES = "recent_files_json"
        private const val MAX_RECENT_FILES = 6
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Get the list of recent files, ordered by most recent first
     */
    fun getRecentFiles(): List<RecentFile> {
        val json = prefs.getString(KEY_RECENT_FILES, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<RecentFile>>() {}.type
            gson.fromJson<List<RecentFile>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            // If deserialization fails, return empty list and clear corrupted data
            clearRecentFiles()
            emptyList()
        }
    }

    /**
     * Add a file to recent files. If the file already exists, update its timestamp.
     * Maintains a maximum of 6 files, removing the oldest when necessary.
     */
    fun addRecentFile(uri: Uri, fileName: String) {
        val currentFiles = getRecentFiles().toMutableList()
        val uriString = uri.toString()
        val currentTimestamp = System.currentTimeMillis()

        // Remove existing entry if present (we'll add it back with updated timestamp)
        currentFiles.removeAll { it.uri == uriString }

        // Add new entry at the beginning (most recent)
        val newFile = RecentFile(
            uri = uriString,
            fileName = fileName,
            lastOpenedTimestamp = currentTimestamp
        )
        currentFiles.add(0, newFile)

        // Keep only the most recent MAX_RECENT_FILES
        val trimmedFiles = currentFiles.take(MAX_RECENT_FILES)

        saveRecentFiles(trimmedFiles)
    }

    /**
     * Remove a specific file from recent files by URI
     */
    fun removeRecentFile(uri: Uri) {
        val currentFiles = getRecentFiles().toMutableList()
        val uriString = uri.toString()

        currentFiles.removeAll { it.uri == uriString }

        saveRecentFiles(currentFiles)
    }

    /**
     * Clear all recent files
     */
    fun clearRecentFiles() {
        prefs.edit().remove(KEY_RECENT_FILES).apply()
    }

    /**
     * Save the recent files list to SharedPreferences
     */
    private fun saveRecentFiles(files: List<RecentFile>) {
        val json = gson.toJson(files)
        prefs.edit().putString(KEY_RECENT_FILES, json).apply()
    }
}
