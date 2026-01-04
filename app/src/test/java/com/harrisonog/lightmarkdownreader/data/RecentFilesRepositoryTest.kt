package com.harrisonog.lightmarkdownreader.data

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class RecentFilesRepositoryTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: RecentFilesRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)

        every { context.getSharedPreferences("markdown_reader_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        every { context.contentResolver } returns contentResolver

        repository = RecentFilesRepository(context)
    }

    @Test
    fun `getRecentFiles returns empty list when no files stored`() {
        every { sharedPreferences.getString("recent_files_json", null) } returns null

        val result = repository.getRecentFiles()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRecentFiles returns stored files`() {
        val json = """
            [
                {"uri":"content://test1","fileName":"file1.md","lastOpenedTimestamp":1000},
                {"uri":"content://test2","fileName":"file2.md","lastOpenedTimestamp":2000}
            ]
        """.trimIndent()
        every { sharedPreferences.getString("recent_files_json", null) } returns json

        val result = repository.getRecentFiles()

        assertEquals(2, result.size)
        assertEquals("content://test1", result[0].uri)
        assertEquals("file1.md", result[0].fileName)
        assertEquals(1000L, result[0].lastOpenedTimestamp)
    }

    @Test
    fun `addRecentFile adds new file to empty list`() {
        every { sharedPreferences.getString("recent_files_json", null) } returns null
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test"
        val jsonSlot = slot<String>()

        repository.addRecentFile(uri, "test.md")

        verify { editor.putString("recent_files_json", capture(jsonSlot)) }
        assertTrue(jsonSlot.captured.contains("test.md"))
        assertTrue(jsonSlot.captured.contains("content://test"))
    }

    @Test
    fun `addRecentFile maintains max 6 files limit`() {
        val json = """
            [
                {"uri":"content://test1","fileName":"file1.md","lastOpenedTimestamp":1000},
                {"uri":"content://test2","fileName":"file2.md","lastOpenedTimestamp":2000},
                {"uri":"content://test3","fileName":"file3.md","lastOpenedTimestamp":3000},
                {"uri":"content://test4","fileName":"file4.md","lastOpenedTimestamp":4000},
                {"uri":"content://test5","fileName":"file5.md","lastOpenedTimestamp":5000},
                {"uri":"content://test6","fileName":"file6.md","lastOpenedTimestamp":6000}
            ]
        """.trimIndent()
        every { sharedPreferences.getString("recent_files_json", null) } returns json
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test7"

        repository.addRecentFile(uri, "file7.md")

        // Verify the old repository was recreated (implying save was called)
        verify { editor.putString("recent_files_json", any()) }

        // Get the saved files and verify max 6
        val savedJson = slot<String>()
        verify { editor.putString("recent_files_json", capture(savedJson)) }

        // Count occurrences of "fileName" to count how many files are in the JSON
        val fileCount = savedJson.captured.split("\"fileName\"").size - 1
        assertEquals(6, fileCount)
    }

    @Test
    fun `addRecentFile adds new file at the beginning`() {
        val json = """
            [
                {"uri":"content://test1","fileName":"file1.md","lastOpenedTimestamp":1000}
            ]
        """.trimIndent()
        every { sharedPreferences.getString("recent_files_json", null) } returns json
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test2"

        repository.addRecentFile(uri, "file2.md")

        val savedJson = slot<String>()
        verify { editor.putString("recent_files_json", capture(savedJson)) }

        // New file should appear before old file in JSON
        val test2Index = savedJson.captured.indexOf("test2")
        val test1Index = savedJson.captured.indexOf("test1")
        assertTrue(test2Index < test1Index)
    }

    @Test
    fun `addRecentFile updates timestamp for duplicate file`() {
        val json = """
            [
                {"uri":"content://test1","fileName":"file1.md","lastOpenedTimestamp":1000},
                {"uri":"content://test2","fileName":"file2.md","lastOpenedTimestamp":2000}
            ]
        """.trimIndent()
        every { sharedPreferences.getString("recent_files_json", null) } returns json
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test1"

        repository.addRecentFile(uri, "file1.md")

        val savedJson = slot<String>()
        verify { editor.putString("recent_files_json", capture(savedJson)) }

        // Should still have 2 files, not 3
        val fileCount = savedJson.captured.split("\"fileName\"").size - 1
        assertEquals(2, fileCount)

        // test1 should now be first (most recent)
        val test1Index = savedJson.captured.indexOf("test1")
        val test2Index = savedJson.captured.indexOf("test2")
        assertTrue(test1Index < test2Index)
    }

    @Test
    fun `removeRecentFile removes specified file`() {
        val json = """
            [
                {"uri":"content://test1","fileName":"file1.md","lastOpenedTimestamp":1000},
                {"uri":"content://test2","fileName":"file2.md","lastOpenedTimestamp":2000}
            ]
        """.trimIndent()
        every { sharedPreferences.getString("recent_files_json", null) } returns json
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test1"

        repository.removeRecentFile(uri)

        val savedJson = slot<String>()
        verify { editor.putString("recent_files_json", capture(savedJson)) }

        // Should only have 1 file now
        val fileCount = savedJson.captured.split("\"fileName\"").size - 1
        assertEquals(1, fileCount)

        // Should not contain test1
        assertTrue(!savedJson.captured.contains("test1"))
        assertTrue(savedJson.captured.contains("test2"))
    }

    @Test
    fun `clearRecentFiles removes all files`() {
        repository.clearRecentFiles()

        verify { editor.remove("recent_files_json") }
        verify { editor.apply() }
    }

    // Note: validateAndCleanupRecentFiles and migration tests require Robolectric
    // or instrumented tests since they use Uri.parse() which is an Android framework method.
    // These tests are moved to androidTest or skipped in unit tests.
}
