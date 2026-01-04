package com.harrisonog.lightmarkdownreader.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

class FileRepositoryTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var fileRepository: FileRepository
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        mockUri = mockk(relaxed = true)

        every { context.contentResolver } returns contentResolver

        fileRepository = FileRepository(context)
    }

    @Test
    fun `readMarkdownFile returns success when file is read successfully`() {
        val fileContent = "# Hello World\nThis is a markdown file."
        val inputStream = ByteArrayInputStream(fileContent.toByteArray())

        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns fileContent.length.toLong()

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { contentResolver.openInputStream(mockUri) } returns inputStream

        val result = fileRepository.readMarkdownFile(mockUri)

        assertTrue(result.isSuccess)
        assertEquals(fileContent, result.getOrNull())
        verify { contentResolver.openInputStream(mockUri) }
    }

    @Test
    fun `readMarkdownFile returns failure when file is too large`() {
        val largeFileSize = 11 * 1024 * 1024L // 11MB

        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns largeFileSize

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor

        val result = fileRepository.readMarkdownFile(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileTooLargeException)
    }

    @Test
    fun `readMarkdownFile returns failure when file not found`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns 100L

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { contentResolver.openInputStream(mockUri) } throws FileNotFoundException("File not found")

        val result = fileRepository.readMarkdownFile(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundCustomException)
    }

    @Test
    fun `readMarkdownFile returns failure when permission denied`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns 100L

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { contentResolver.openInputStream(mockUri) } throws SecurityException("Permission denied")

        val result = fileRepository.readMarkdownFile(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FilePermissionException)
    }

    @Test
    fun `readMarkdownFile returns failure when file cannot be opened`() {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getLong(0) } returns 100L

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor
        every { contentResolver.openInputStream(mockUri) } returns null

        val result = fileRepository.readMarkdownFile(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotOpenableException)
    }

    @Test
    fun `getFileName returns correct file name`() {
        val expectedFileName = "test.md"
        val cursor = mockk<Cursor>(relaxed = true)

        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns expectedFileName

        every { contentResolver.query(mockUri, null, null, null, null) } returns cursor

        val fileName = fileRepository.getFileName(mockUri)

        assertEquals(expectedFileName, fileName)
    }

    @Test
    fun `getFileName returns Unknown when query fails`() {
        every { contentResolver.query(mockUri, null, null, null, null) } returns null

        val fileName = fileRepository.getFileName(mockUri)

        assertEquals("Unknown", fileName)
    }

    @Test
    fun `getFileName returns Unknown when exception occurs`() {
        every { contentResolver.query(mockUri, null, null, null, null) } throws Exception("Error")

        val fileName = fileRepository.getFileName(mockUri)

        assertEquals("Unknown", fileName)
    }
}
