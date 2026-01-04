package com.harrisonog.lightmarkdownreader.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.harrisonog.lightmarkdownreader.data.FileRepository
import com.harrisonog.lightmarkdownreader.data.RecentFile
import com.harrisonog.lightmarkdownreader.data.RecentFilesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelRecentFilesTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReaderViewModel
    private lateinit var fileRepository: FileRepository
    private lateinit var recentFilesRepository: RecentFilesRepository
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ReaderViewModel()
        fileRepository = mockk(relaxed = true)
        recentFilesRepository = mockk(relaxed = true)
        mockUri = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial recentFiles state is empty list`() = runTest {
        viewModel.recentFiles.test {
            val initialState = awaitItem()
            assertTrue(initialState.isEmpty())
        }
    }

    @Test
    fun `loadRecentFiles updates recentFiles state`() = runTest {
        val mockRecentFiles = listOf(
            RecentFile("content://test1", "file1.md", 1000L),
            RecentFile("content://test2", "file2.md", 2000L)
        )
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns mockRecentFiles

        viewModel.recentFiles.test {
            // Skip initial empty state
            awaitItem()

            viewModel.loadRecentFiles(recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(2, updatedState.size)
            assertEquals("file1.md", updatedState[0].fileName)
            assertEquals("file2.md", updatedState[1].fileName)
        }
    }

    @Test
    fun `loadRecentFiles calls validateAndCleanupRecentFiles`() = runTest {
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns emptyList()

        viewModel.loadRecentFiles(recentFilesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { recentFilesRepository.validateAndCleanupRecentFiles() }
    }

    @Test
    fun `loadFile adds file to recent files on success`() = runTest {
        val fileContent = "# Test Markdown"
        val fileName = "test.md"

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.success(fileContent)
        every { fileRepository.getFileName(mockUri) } returns fileName
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns emptyList()

        viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { recentFilesRepository.addRecentFile(mockUri, fileName) }
    }

    @Test
    fun `loadFile refreshes recent files list after adding file`() = runTest {
        val fileContent = "# Test Markdown"
        val fileName = "test.md"
        val updatedRecentFiles = listOf(
            RecentFile(mockUri.toString(), fileName, System.currentTimeMillis())
        )

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.success(fileContent)
        every { fileRepository.getFileName(mockUri) } returns fileName
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns updatedRecentFiles

        viewModel.recentFiles.test {
            // Skip initial empty state
            awaitItem()

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Recent files should be updated
            val updatedState = awaitItem()
            assertEquals(1, updatedState.size)
            assertEquals(fileName, updatedState[0].fileName)
        }
    }

    @Test
    fun `loadFile does not add file to recent files on failure`() = runTest {
        val exception = Exception("File load failed")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { recentFilesRepository.addRecentFile(any(), any()) }
    }

    @Test
    fun `removeRecentFile removes file from repository`() = runTest {
        viewModel.removeRecentFile(mockUri, recentFilesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { recentFilesRepository.removeRecentFile(mockUri) }
    }

    @Test
    fun `removeRecentFile refreshes recent files list`() = runTest {
        val initialRecentFiles = listOf(
            RecentFile("content://test1", "file1.md", 1000L),
            RecentFile("content://test2", "file2.md", 2000L)
        )
        val updatedRecentFiles = listOf(
            RecentFile("content://test2", "file2.md", 2000L)
        )

        every { recentFilesRepository.validateAndCleanupRecentFiles() } returnsMany listOf(
            initialRecentFiles,
            updatedRecentFiles
        )

        viewModel.recentFiles.test {
            // Skip initial empty state
            awaitItem()

            // Load initial recent files
            viewModel.loadRecentFiles(recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = awaitItem()
            assertEquals(2, initial.size)

            // Remove a file
            val uri1 = mockk<Uri>(relaxed = true)
            every { uri1.toString() } returns "content://test1"
            viewModel.removeRecentFile(uri1, recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should update to show only 1 file
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("file2.md", updated[0].fileName)
        }
    }

    @Test
    fun `recent files are ordered by most recent first`() = runTest {
        val mockRecentFiles = listOf(
            RecentFile("content://test3", "file3.md", 3000L),
            RecentFile("content://test2", "file2.md", 2000L),
            RecentFile("content://test1", "file1.md", 1000L)
        )
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns mockRecentFiles

        viewModel.recentFiles.test {
            awaitItem() // Skip initial state

            viewModel.loadRecentFiles(recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(3, updatedState.size)
            // Most recent should be first
            assertEquals(3000L, updatedState[0].lastOpenedTimestamp)
            assertEquals(2000L, updatedState[1].lastOpenedTimestamp)
            assertEquals(1000L, updatedState[2].lastOpenedTimestamp)
        }
    }

    @Test
    fun `loadRecentFiles handles empty list`() = runTest {
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns emptyList()

        viewModel.loadRecentFiles(recentFilesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.recentFiles.test {
            val currentState = awaitItem()
            assertTrue(currentState.isEmpty())
        }
    }

    @Test
    fun `closeFile does not affect recent files state`() = runTest {
        val mockRecentFiles = listOf(
            RecentFile("content://test1", "file1.md", 1000L)
        )
        every { recentFilesRepository.validateAndCleanupRecentFiles() } returns mockRecentFiles

        viewModel.recentFiles.test {
            awaitItem() // Skip initial empty state

            viewModel.loadRecentFiles(recentFilesRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            val recentFilesState = awaitItem()
            assertEquals(1, recentFilesState.size)

            // Close the file
            viewModel.closeFile()
            testDispatcher.scheduler.advanceUntilIdle()

            // Recent files should not change (no new emission)
            expectNoEvents()
        }
    }
}
