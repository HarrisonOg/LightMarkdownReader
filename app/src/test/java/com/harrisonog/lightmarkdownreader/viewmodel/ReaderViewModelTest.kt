package com.harrisonog.lightmarkdownreader.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.harrisonog.lightmarkdownreader.data.FileNotFoundCustomException
import com.harrisonog.lightmarkdownreader.data.FileNotOpenableException
import com.harrisonog.lightmarkdownreader.data.FilePermissionException
import com.harrisonog.lightmarkdownreader.data.FileRepository
import com.harrisonog.lightmarkdownreader.data.FileTooLargeException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReaderViewModel
    private lateinit var fileRepository: FileRepository
    private lateinit var recentFilesRepository: com.harrisonog.lightmarkdownreader.data.RecentFilesRepository
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
    fun `initial state is Empty`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is ReaderUiState.Empty)
        }
    }

    @Test
    fun `loadFile emits Loading then Success when file is read successfully`() = runTest {
        val fileContent = "# Test Markdown"
        val fileName = "test.md"

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.success(fileContent)
        every { fileRepository.getFileName(mockUri) } returns fileName

        viewModel.uiState.test {
            // Skip initial Empty state
            awaitItem()

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            // Should emit Loading
            val loadingState = awaitItem()
            assertTrue(loadingState is ReaderUiState.Loading)

            // Should emit Success
            val successState = awaitItem()
            assertTrue(successState is ReaderUiState.Success)
            assertEquals(fileContent, (successState as ReaderUiState.Success).content)
            assertEquals(fileName, successState.fileName)
        }
    }

    @Test
    fun `loadFile emits Loading then Error when file not found`() = runTest {
        val exception = FileNotFoundCustomException("File not found")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.uiState.test {
            // Skip initial Empty state
            awaitItem()

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            // Should emit Loading
            val loadingState = awaitItem()
            assertTrue(loadingState is ReaderUiState.Loading)

            // Should emit Error with FILE_NOT_FOUND type
            val errorState = awaitItem()
            assertTrue(errorState is ReaderUiState.Error)
            assertEquals(ErrorType.FILE_NOT_FOUND, (errorState as ReaderUiState.Error).errorType)
        }
    }

    @Test
    fun `loadFile emits Error with PERMISSION_DENIED when permission denied`() = runTest {
        val exception = FilePermissionException("Permission denied")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            awaitItem() // Skip Loading

            val errorState = awaitItem()
            assertTrue(errorState is ReaderUiState.Error)
            assertEquals(ErrorType.PERMISSION_DENIED, (errorState as ReaderUiState.Error).errorType)
        }
    }

    @Test
    fun `loadFile emits Error with FILE_TOO_LARGE when file is too large`() = runTest {
        val exception = FileTooLargeException("File too large")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            awaitItem() // Skip Loading

            val errorState = awaitItem()
            assertTrue(errorState is ReaderUiState.Error)
            assertEquals(ErrorType.FILE_TOO_LARGE, (errorState as ReaderUiState.Error).errorType)
        }
    }

    @Test
    fun `loadFile emits Error with COULD_NOT_OPEN when file cannot be opened`() = runTest {
        val exception = FileNotOpenableException("Could not open file")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            awaitItem() // Skip Loading

            val errorState = awaitItem()
            assertTrue(errorState is ReaderUiState.Error)
            assertEquals(ErrorType.COULD_NOT_OPEN, (errorState as ReaderUiState.Error).errorType)
        }
    }

    @Test
    fun `loadFile emits Error with UNKNOWN for other exceptions`() = runTest {
        val exception = Exception("Unknown error")

        every { fileRepository.readMarkdownFile(mockUri) } returns Result.failure(exception)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            viewModel.loadFile(mockUri, fileRepository, recentFilesRepository)

            awaitItem() // Skip Loading

            val errorState = awaitItem()
            assertTrue(errorState is ReaderUiState.Error)
            assertEquals(ErrorType.UNKNOWN, (errorState as ReaderUiState.Error).errorType)
            assertEquals("Unknown error", (errorState as ReaderUiState.Error).message)
        }
    }
}
